package ru.igla.tfprofiler.media_track

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.NonNull
import androidx.annotation.WorkerThread
import androidx.arch.core.util.Function
import androidx.lifecycle.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import ru.igla.tfprofiler.core.*
import ru.igla.tfprofiler.core.analytics.StatisticsEstimator
import ru.igla.tfprofiler.models_list.DelegateRunRequest
import ru.igla.tfprofiler.models_list.ModelEntity
import ru.igla.tfprofiler.prefs.AndroidPreferenceManager
import ru.igla.tfprofiler.reports_list.ListReportEntity
import ru.igla.tfprofiler.tflite_runners.base.Classifier
import ru.igla.tfprofiler.tflite_runners.base.ClassifierFactory
import ru.igla.tfprofiler.tflite_runners.base.ImageBatchProcessing
import ru.igla.tfprofiler.tflite_runners.base.ModelOptions
import ru.igla.tfprofiler.utils.*
import ru.igla.tfprofiler.video.TakeVideoFrameListener
import ru.igla.tfprofiler.video.UpdateProgressListener
import java.util.*


class RecognitionViewModel(
    application: Application,
    val modelEntity: ModelEntity
) : AndroidViewModel(application) {

    private val resolveRunDelegatesExtrasUseCase by lazy {
        ResolveRunDelegatesExtrasUseCase(application)
    }

    // create detector
    private var selectedModelOptions: ModelOptions =
        ModelOptions(
            device = Device.CPU,
            numThreads = 4,
            useXnnpack = false,
            numberOfInputImages = 1
        )

    private var detector: Classifier<ImageBatchProcessing.ImageResult>? = null

    val liveDataBitmapOutput = MutableLiveData<BitmapResult>()

    val liveDataShowRecognitionError = MutableLiveData<Exception>()

    private val statisticsEstimator by lazy { StatisticsEstimator(application) }

    private val preferenceManager by lazy { AndroidPreferenceManager(application) }

    //on Xiaomi A1 standard MediaMetadataRetriever not works (black or first duplicate frames), use other
    private val jCodecExtractor by lazy { JCodecExtractor() }

    private val openCVVideoFramesExtractor by lazy { OpenCVVideoFramesExtractor() }

    val livedataProcessFrameInfo = MutableLiveData<FrameInformation>()

    val previewImageLiveData = MutableLiveData<Bitmap>()

    private val progressProcessImageListener: UpdateProgressListener by lazy {
        object : UpdateProgressListener {
            @SuppressLint("SetTextI18n")
            override fun onUpdate(information: FrameInformation) {
                livedataProcessFrameInfo.sendValueIfNew(information)
            }
        }
    }

    private val takeVideoFramesListener by lazy {
        object : TakeVideoFrameListener {
            override fun onTakeFrame(bitmap: Bitmap) {
                if (!viewModelScope.isActive) {
                    throw CancellationException()
                }
                runImageInterference(bitmap)
            }
        }
    }

    private suspend fun iterateAssetsFiles(context: Context, imagesFolder: String) {
        val recognizeAssetFolderCase = RecognizeAssetFolderCase(
            object : RecognizeAssetFolderCase.OnReadAssetImageCallback {
                override fun onReadAssetImage(bitmap: Bitmap) {
                    runImageInterference(bitmap)
                }

                override fun onProgress(progress: FrameInformation) {
                    livedataProcessFrameInfo.sendValueIfNew(progress)
                }
            }
        )
        try {
            recognizeAssetFolderCase.iterateAssetsFiles(context, imagesFolder)
        } catch (e: Exception) {
            liveDataShowRecognitionError.postValue(e)
        }
    }

    suspend fun recognizeImageDataset(imagesFolder: String) {
        iterateAssetsFiles(getApplication(), imagesFolder)
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
                .onEach { value ->
                    value.data?.let { valueData ->
                        if (value.status == Status.SUCCESS || value.status == Status.LOADING) {
                            progressProcessImageListener.onUpdate(
                                FrameInformation(valueData.totalFrames, valueData.frameNumber)
                            )
                        }
                        if (value.status == Status.LOADING) {
                            valueData.bitmap?.apply {
                                takeVideoFramesListener.onTakeFrame(this)
                            }
                        }
                    }
                }
                .catch { e ->
                    Timber.e(e)
                    liveDataShowRecognitionError.postValue(Exception(e))
                }
                .flowOn(Dispatchers.IO)
                .collect()
        } catch (e: Exception) {
            Timber.e(e)
            liveDataShowRecognitionError.postValue(e)
        } finally {
            logI {
                "Finish recognize video. Time elapsed: " + timeWatchClockOS.stop() + " ms"
            }
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

    private val runInterferenceCase by lazy {
        RunInterferenceCase(
            statisticsEstimator,
            preferenceManager,
            object : RecgonizeImageCallback {
                override fun startRecognizeImage(timestampBitmap: BitmapResult) {
                    liveDataBitmapOutput.sendValueIfNew(timestampBitmap)
                }

                override fun onPreview(progress: Bitmap) {
                    previewImageLiveData.sendValueIfNew(progress)
                }
            })
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
                detector = ClassifierFactory.create(getApplication(), modelEntity, modelOptions)
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

    /**
     * A creator is used to inject the product ID into the ViewModel
     *
     *
     * This creator is to showcase how to inject dependencies into ViewModels. It's not
     * actually necessary in this case, as the product ID can be passed in a public method.
     */
    @Suppress("UNCHECKED_CAST")
    class Factory(
        @field:NonNull @param:NonNull private val mApplication: Application,
        private val entity: ModelEntity
    ) :
        ViewModelProvider.NewInstanceFactory() {

        @NonNull
        override fun <T : ViewModel?> create(@NonNull modelClass: Class<T>): T {
            return RecognitionViewModel(mApplication, entity) as T
        }
    }

    fun getDelegateDetails(modelOptions: ModelOptions): String {
        val strDetails = StringBuilder(modelOptions.device.name).apply {
            val threads = modelOptions.numThreads
            val useXnnpack = modelOptions.useXnnpack

            if (isNotEmpty()) {
                append(", ")
            }
            append(threads)
            if (threads == 1) {
                append(" Thread")
            } else {
                append(" Threads")
            }
            if (useXnnpack) {
                append(", XNNPACK")
            }
        }
        return strDetails.toString()
    }
}