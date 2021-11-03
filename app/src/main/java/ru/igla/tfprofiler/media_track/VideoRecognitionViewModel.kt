package ru.igla.tfprofiler.media_track

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.arch.core.util.Function
import androidx.lifecycle.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import ru.igla.tfprofiler.core.Resource
import ru.igla.tfprofiler.core.Status
import ru.igla.tfprofiler.core.UseCase
import ru.igla.tfprofiler.core.analytics.StatisticsEstimator
import ru.igla.tfprofiler.core.cpu_load.CPUIntenseLoadWorker
import ru.igla.tfprofiler.models_list.DelegateRunRequest
import ru.igla.tfprofiler.models_list.domain.ModelEntity
import ru.igla.tfprofiler.prefs.AndroidPreferenceManager
import ru.igla.tfprofiler.reports_list.ListReportEntity
import ru.igla.tfprofiler.tflite_runners.base.Classifier
import ru.igla.tfprofiler.tflite_runners.base.ImageClassifierFactory
import ru.igla.tfprofiler.tflite_runners.base.ModelOptions
import ru.igla.tfprofiler.tflite_runners.domain.ImageResult
import ru.igla.tfprofiler.utils.*
import timber.log.Timber
import java.util.*


class VideoRecognitionViewModel(
    application: Application,
    val modelEntity: ModelEntity
) : AndroidViewModel(application) {

    private val cpuIntenseLoadWorker by lazy {
        CPUIntenseLoadWorker()
    }

    private val resolveRunDelegatesExtrasUseCase by lazy {
        ResolveRunDelegatesExtrasUseCase(application)
    }

    // create detector
    private var selectedModelOptions: ModelOptions = ModelOptions.default

    private var detector: Classifier<List<Bitmap>, List<ImageResult>>? = null

    val liveDataBitmapOutput = MutableLiveData<BitmapResult>()

    val liveDataShowRecognitionError = MutableLiveData<Exception>()

    private val statisticsEstimator by lazy { StatisticsEstimator(application) }

    private val preferenceManager by lazy { AndroidPreferenceManager(application) }

    //on Xiaomi A1 standard MediaMetadataRetriever not works (black or first duplicate frames), use other
    private val jCodecExtractor by lazy { JCodecExtractor() }

    private val openCVVideoFramesExtractor by lazy { OpenCVVideoFramesExtractor() }

    val livedataProcessFrameInfo = MutableLiveData<FrameInformation>()

    val previewImageLiveData = MutableLiveData<Bitmap>()

    private fun onTakeVideoFrame(bitmap: Bitmap) {
        if (!viewModelScope.isActive) {
            throw CancellationException()
        }
        runImageInterference(bitmap)
    }

    private val runInterferenceCase by lazy {
        BitmapRunInterferenceCase(
            statisticsEstimator,
            preferenceManager,
            object : RecognizeImageCallback {
                override fun startRecognizeImage(timestampBitmap: BitmapResult) {
                    liveDataBitmapOutput.sendValueIfNew(timestampBitmap)
                }

                override fun onPreview(progress: Bitmap) {
                    previewImageLiveData.sendValueIfNew(progress)
                }
            })
    }

    // create classifier
    private val classifierFunc = Function<ModelOptions, LiveData<Resource<ModelOptions>>> { model ->
        createLiveDataClassifier(model)
    }

    // observe model options
    private val liveDataModelOptions: MutableLiveData<ModelOptions> = MutableLiveData()

    // switcher from options to classifier maker
    val liveDataCreateDelegate = Transformations.switchMap(
        liveDataModelOptions,
        classifierFunc
    )

    suspend fun recognizeImageDataset(imagesFolder: String) {
        val recognizeAssetFolderCase = RecognizeAssetFolderCase()
        try {
            recognizeAssetFolderCase.iterateAssetsFiles(getApplication(), imagesFolder)
                .onStart {
                    logI { "Start read video frames" }
                    if (selectedModelOptions.useCpuStress) {
                        cpuIntenseLoadWorker.startWork()
                    }
                }
                .onEach { value ->
                    value.data?.let { valueData ->
                        if (value.status == Status.LOADING) {
                            if (valueData.bitmap != null) {
                                runImageInterference(valueData.bitmap)
                            }
                            livedataProcessFrameInfo.sendValueIfNew(
                                FrameInformation(valueData.totalFrames, valueData.frameNumber)
                            )
                        }
                    }
                }
                .catch { e ->
                    Timber.e(e)
                    liveDataShowRecognitionError.postValue(Exception(e))
                }
                .onCompletion {
                    //cancel
                    logI { "End read video frames" }
                    if (selectedModelOptions.useCpuStress) {
                        cpuIntenseLoadWorker.stop()
                    }
                }
                .flowOn(Dispatchers.IO)
                .collect()
        } catch (e: Exception) {
            liveDataShowRecognitionError.postValue(e)
        }
    }

    fun isVideoFile(mimeType: String?): Boolean {
        return mimeType?.startsWith("video") ?: false
    }

    suspend fun recognizeVideo(filePath: String) {
        val timeWatchClockOS = TimeWatchClockOS()
        timeWatchClockOS.start()
        logI { "Start recognize video $filePath" }
        try {
            val framesExtractor: ReadVideoFileInterface =
                if (filePath.endsWith(".avi")) //we can use opencv
                // https://stackoverflow.com/questions/43382359/andriod-studio-opencv-3-2-cannot-open-video-file-or-android-camera-with-native
                    openCVVideoFramesExtractor
                else //e.g. .mp4
                    jCodecExtractor
            framesExtractor.readVideoFile(filePath)
                .onStart {
                    logI { "Start read video frames" }
                    if (selectedModelOptions.useCpuStress) {
                        cpuIntenseLoadWorker.startWork()
                    }
                }
                .onEach { value ->
                    value.data?.let { valueData ->
                        if (value.status == Status.SUCCESS || value.status == Status.LOADING) {
                            livedataProcessFrameInfo.sendValueIfNew(
                                FrameInformation(valueData.totalFrames, valueData.frameNumber)
                            )

                            if (value.status == Status.LOADING) {
                                valueData.bitmap?.apply {
                                    onTakeVideoFrame(this)
                                }
                            }
                        }
                    }
                }
                .catch { e ->
                    Timber.e(e)
                    liveDataShowRecognitionError.postValue(Exception(e))
                }
                .onCompletion {
                    //cancel
                    logI { "End read video frames" }
                    if (selectedModelOptions.useCpuStress) {
                        cpuIntenseLoadWorker.stop()
                    }
                }
                .flowOn(Dispatchers.IO)
                .collect()
        } catch (e: Exception) {
            Timber.e(e)
            liveDataShowRecognitionError.postValue(e)
        } finally {
            logI { "Finish recognize video. Time elapsed: " + timeWatchClockOS.stop() + " ms" }
        }
    }

    fun recognizePhoto(context: Context, selectedImageUri: Uri): Boolean {
        val maxImageSize = preferenceManager.defaultPrefs.maxImageSize
        val bitmap = CameraUtils.handleSamplingAndRotationBitmap(
            context,
            selectedImageUri,
            maxImageSize,
            maxImageSize
        )
        if (bitmap == null) {
            Timber.e(Exception("Bitmap null"))
            return false
        }

        logI { "START recognize image" }
        runImageInterference(bitmap)
        logI { "END recognize image" }
        return true
    }

    @WorkerThread
    fun runImageInterference(bitmap: Bitmap) {
        val detector = detector ?: return
        runInterferenceCase.runImageInterference(
            detector,
            modelEntity,
            selectedModelOptions,
            bitmap
        )
    }

    private fun createLiveDataClassifier(modelOptions: ModelOptions): LiveData<Resource<ModelOptions>> {
        return liveData(
            context = viewModelScope.coroutineContext + Dispatchers.IO
        ) {
            val timeWatchClockOS = TimeWatchClockOS()
            timeWatchClockOS.start()
            try {
                logD { "Creating classifier $modelOptions" }
                emit(Resource.loading(modelOptions))
                detector =
                    ImageClassifierFactory.create(getApplication(), modelEntity, modelOptions)
                emit(Resource.success(modelOptions))
            } catch (e: Exception) {
                statisticsEstimator.setError(modelOptions, e)
                Timber.e(e)
                emit(Resource.error(e.message ?: "", modelOptions))
            } finally {
                statisticsEstimator.setInitTime(modelOptions, timeWatchClockOS.stop())
            }
        }
    }

    fun setModelOptions(modelOptions: ModelOptions) {
        this.selectedModelOptions = modelOptions
        statisticsEstimator.setWarmupRuns(modelOptions, preferenceManager.defaultPrefs.warmupRuns)
        this.liveDataModelOptions.value = modelOptions
    }

    fun getReportData(): ListReportEntity {
        return statisticsEstimator.createReport(modelEntity)
    }

    override fun onCleared() {
        super.onCleared()
        IOUtils.closeQuietly(detector)
        detector = null
    }

    fun resolveRunDelegatesExtra(
        delegateRunRequest: DelegateRunRequest?,
        modelEntity: ModelEntity
    ): Queue<ModelOptions> {
        val useCaseResponse: UseCase.Resource<ResolveRunDelegatesExtrasUseCase.ResponseValue> =
            resolveRunDelegatesExtrasUseCase.executeUseCase(
                ResolveRunDelegatesExtrasUseCase.RequestValues(
                    delegateRunRequest,
                    modelEntity
                )
            )
        if (useCaseResponse.isSuccess()) {
            val response: ResolveRunDelegatesExtrasUseCase.ResponseValue? = useCaseResponse.data
            return response?.queue ?: ArrayDeque()
        }
        return ArrayDeque()
    }

    fun getDelegateDetails(modelOptions: ModelOptions): String {
        return modelOptions.getReadableStr()
    }
}