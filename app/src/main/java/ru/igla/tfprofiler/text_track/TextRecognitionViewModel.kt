package ru.igla.tfprofiler.text_track

import android.app.Application
import androidx.annotation.WorkerThread
import androidx.arch.core.util.Function
import androidx.lifecycle.*
import kotlinx.android.synthetic.main.fragment_text_layout.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.igla.tfprofiler.core.Resource
import ru.igla.tfprofiler.core.UseCase
import ru.igla.tfprofiler.core.analytics.StatisticsEstimator
import ru.igla.tfprofiler.media_track.ResolveRunDelegatesExtrasUseCase
import ru.igla.tfprofiler.models_list.DelegateRunRequest
import ru.igla.tfprofiler.models_list.domain.ModelEntity
import ru.igla.tfprofiler.prefs.AndroidPreferenceManager
import ru.igla.tfprofiler.reports_list.ListReportEntity
import ru.igla.tfprofiler.tflite_runners.base.Classifier
import ru.igla.tfprofiler.tflite_runners.base.ModelOptions
import ru.igla.tfprofiler.tflite_runners.base.TextClassifierFactory
import ru.igla.tfprofiler.tflite_runners.domain.TextRecognition
import ru.igla.tfprofiler.utils.*
import timber.log.Timber
import java.util.*


class TextRecognitionViewModel(
    application: Application,
    val modelEntity: ModelEntity
) : AndroidViewModel(application) {

    private val resolveRunDelegatesExtrasUseCase by lazy {
        ResolveRunDelegatesExtrasUseCase(application)
    }

    // create detector
    private var selectedModelOptions: ModelOptions = ModelOptions.default

    private var detector: Classifier<String, List<TextRecognition>>? = null

    val liveDataTextOutput = MutableLiveData<Resource<TextOutResult>>()

    val liveDataShowRecognitionError = MutableLiveData<Exception>()

    private val statisticsEstimator by lazy { StatisticsEstimator(application) }

    private val preferenceManager by lazy { AndroidPreferenceManager(application) }

    private val runInterferenceCase by lazy {
        TextRunInterferenceCase(
            statisticsEstimator,
            preferenceManager
        )
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

    @Throws(IllegalStateException::class)
    @WorkerThread
    fun runTextInterference(data: String): TextOutResult {
        if (!viewModelScope.isActive) {
            throw CancellationException()
        }
        val detector = detector ?: throw IllegalStateException("Detector is not yet created")
        return runInterferenceCase.runTextInterference(
            detector,
            modelEntity,
            selectedModelOptions,
            data
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
                detector = TextClassifierFactory.create(
                    getApplication(),
                    modelEntity,
                    modelOptions
                )
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

    @ExperimentalCoroutinesApi
    @FlowPreview
    suspend fun startSearchFlow(stateFlow: Flow<String>) {
        stateFlow
            .debounce(300L)
            .filter { query ->
                if (query.isEmpty()) {
                    liveDataTextOutput.sendValueIfNew(Resource.success(null))
                    return@filter false
                } else {
                    return@filter true
                }
            }
            .distinctUntilChanged()
            .flatMapLatest { query ->
                runInferenceData(query)
                    .catch { e ->
                        emitAll(flowOf(Resource.error(e.message ?: "")))
                    }
            }
            .flowOn(Dispatchers.Default)
            .collect { result ->
                withContext(Dispatchers.Main) {
                    liveDataTextOutput.sendValueIfNew(result)
                }
            }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    suspend fun startDatasetFlow(flow: Flow<String>) {
        flow
            .flatMapLatest { query ->
                runInferenceData(query)
                    .catch { e ->
                        emitAll(flowOf(Resource.error(e.message ?: "")))
                    }
            }
            .flowOn(Dispatchers.Default)
            .collect { result ->
                withContext(Dispatchers.Main) {
                    liveDataTextOutput.sendValueIfNew(result)
                }
            }
    }


    private fun runInferenceData(query: String): Flow<Resource<TextOutResult>> {
        return flow {
            val textOutResult = runTextInterference(query)
            emit(Resource.success(textOutResult))
        }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    suspend fun startRecognitionDataset() {
        val generateFlowString = GenerateFlowString()
        startDatasetFlow(generateFlowString.getFlow())
    }
}

