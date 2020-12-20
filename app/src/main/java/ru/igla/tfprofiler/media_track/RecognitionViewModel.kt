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
import kotlinx.coroutines.isActive
import ru.igla.tfprofiler.core.Device
import ru.igla.tfprofiler.core.Resource
import ru.igla.tfprofiler.core.Timber
import ru.igla.tfprofiler.core.UseCase
import ru.igla.tfprofiler.core.analytics.StatisticsEstimator
import ru.igla.tfprofiler.models_list.DelegateRunRequest
import ru.igla.tfprofiler.models_list.ModelEntity
import ru.igla.tfprofiler.prefs.AndroidPreferenceManager
import ru.igla.tfprofiler.reports_list.ListReportEntity
import ru.igla.tfprofiler.tflite_runners.base.Classifier
import ru.igla.tfprofiler.tflite_runners.base.ClassifierFactory
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
    private var selectedModelOptions: ModelOptions = ModelOptions.Builder()
        .device(Device.CPU)
        .numThreads(4)
        .xnnpack(false)
        .build()

    private var detector: Classifier<Classifier.Recognition>? = null

    val liveDataBitmapOutput = MutableLiveData<BitmapResult>()

    val liveDataShowRecognitionError = MutableLiveData<Exception>()

    private val statisticsEstimator by lazy { StatisticsEstimator(application) }

    private val preferenceManager by lazy { AndroidPreferenceManager(application) }

    //on Xiaomi A1 standard MediaMetadataRetriever not works (black or first duplicate frames), use other
    private val jCodecExtractor by lazy { JCodecExtractor() }

    private val openCVVideoFramesExtractor by lazy { OpenCVVideoFramesExtractor() }

    val livedataProcessFrameInfo = MutableLiveData<FrameInformation>()

    val previewImageLiveData = MutableLiveData<Bitmap>()

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

    fun recognizeVideo(filePath: String) {
        val timeWatchClockOS = TimeWatchClockOS()
        timeWatchClockOS.start()
        logI { "START recognize video $filePath" }
        try {
            if (filePath.endsWith(".avi")) { //we can use opencv
                // https://stackoverflow.com/questions/43382359/andriod-studio-opencv-3-2-cannot-open-video-file-or-android-camera-with-native
                openCVVideoFramesExtractor.readVideoFile(
                    filePath,
                    progressProcessImageListener,
                    takeVideoFramesListener
                )
            } else { //e.g. .mp4
                jCodecExtractor.readVideoFile(
                    filePath,
                    progressProcessImageListener,
                    takeVideoFramesListener
                )
            }
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
                Timber.d(
                    "Creating classifier %s",
                    modelOptions.toString()
                )
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

    fun resolveRunDelegatesExtra(delegateRunRequest: DelegateRunRequest?): Queue<ModelOptions> {
        val useCaseResponse: UseCase.Resource<ResolveRunDelegatesExtrasUseCase.ResponseValue> =
            resolveRunDelegatesExtrasUseCase.executeUseCase(
                ResolveRunDelegatesExtrasUseCase.RequestValues(
                    delegateRunRequest
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
}