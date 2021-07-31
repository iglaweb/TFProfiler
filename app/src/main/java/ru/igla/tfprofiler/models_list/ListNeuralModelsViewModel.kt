package ru.igla.tfprofiler.models_list

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.igla.tfprofiler.TFProfilerApp
import ru.igla.tfprofiler.core.Device
import ru.igla.tfprofiler.core.RequestMode
import ru.igla.tfprofiler.core.UseCase
import ru.igla.tfprofiler.prefs.AndroidPreferenceManager
import ru.igla.tfprofiler.reports_list.RefreshLiveData
import ru.igla.tfprofiler.reports_list.RefreshLiveData.RefreshAction
import ru.igla.tfprofiler.utils.ExceptionHandler
import ru.igla.tfprofiler.utils.extension
import ru.igla.tfprofiler.utils.logI
import ru.igla.tfprofiler.video.FileUtils
import java.io.File


class ListNeuralModelsViewModel(application: Application) :
    AndroidViewModel(application) {

    private val supportedModels = listOf("caffemodel", "pb", "t7", "onnx", "bin")

    companion object {
        private val VIDEO_EXT = listOf("mp4", "avi", "mov", "mpeg", "flv", "wmv")
    }

    private val modelDeleteUseCase by lazy {
        ModelDeleteUseCase(application)
    }

    private val modelConfigUpdateUseCase by lazy {
        ModelConfigUpdateUseCase(application)
    }

    private val preferenceManager by lazy { AndroidPreferenceManager(TFProfilerApp.instance).defaultPrefs }

    private val modelsListUseCase by lazy {
        ResolveModelsListUseCase(application)
    }

    private val addTfliteModelUseCase by lazy {
        AddTfliteModelUseCase(application)
    }

    private val addOpenCVModelUseCase by lazy {
        AddCustomOpenCVModelUseCase(application)
    }

    val liveDataAddNewModel =
        MutableLiveData<UseCase.Resource<AddModelUseCase.ResponseValue>>()

    private val exceptionHandler = ExceptionHandler { _, _ -> }

    var modelsListLiveData: RefreshLiveData<List<ModelEntity>> = getModelList().apply {
        refresh()
    }

    fun deleteCustomModel(item: ModelEntity) {
        modelDeleteUseCase.executeUseCase(
            ModelDeleteUseCase.RequestValues(item.modelConfig.tableId)
        )
    }

    fun updateCustomModel(item: ModelEntity) {
        modelConfigUpdateUseCase.executeUseCase(
            ModelConfigUpdateUseCase.RequestValues(item.modelConfig.tableId, item.modelConfig)
        )
    }

    fun getCameraType(): CameraType {
        return preferenceManager.cameraType
    }

    @Throws(Exception::class)
    suspend fun getMediaRequest(
        selectedImageUri: Uri,
        selectedModelOptionsVideo: ModelEntity?
    ): MediaRequest {
        val selectedImagePath =
            FileUtils.getRealFilePath(getApplication(), selectedImageUri).trim()
        withContext(Dispatchers.Main) {
            logI { "Selected file: $selectedImagePath" }
        }

        val fileExt = selectedImagePath.substringAfterLast('.', "")
        if (!VIDEO_EXT.contains(fileExt)) {
            throw Exception("Selected file is not a video")
        }

        val modelEntity =
            selectedModelOptionsVideo ?: throw Exception("Passed model entity is null")

        return MediaRequest(RequestMode.VIDEO, selectedImagePath, modelEntity)
    }

    fun getDelegateRequest(): DelegateRunRequest {
        val modelList = mutableListOf<Device>().apply {
            if (preferenceManager.cpuDelegateEnabled) {
                add(Device.CPU)
            }
            if (preferenceManager.gpuDelegateEnabled) {
                add(Device.GPU)
            }
            if (preferenceManager.nnapiDelegateEnabled) {
                add(Device.NNAPI)
            }
            if (preferenceManager.hexagonDelegateEnabled) {
                add(Device.HEXAGON)
            }
        }

        return DelegateRunRequest(
            IntRange(
                preferenceManager.threadRangeMin,
                preferenceManager.threadRangeMax
            ),
            modelList,
            preferenceManager.xnnpackEnabled,
            preferenceManager.batchImageCount
        )
    }

    private suspend fun copyModelFileToDestination(path: String): String {
        return withContext(Dispatchers.IO + exceptionHandler) {
            val file = File(path)
            val filename = file.name
            val customModelPath = FileUtils.getRootPath(getApplication())
            val destinationFilename = File(customModelPath, filename).absolutePath
            FileUtils.copyFile(path, destinationFilename)
            destinationFilename
        }
    }

    private fun addCustomOpenCVModel(path: String) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val response =
                addOpenCVModelUseCase.executeUseCase(AddModelUseCase.RequestValues(path))
            if (response.isSuccess()) {
                modelsListLiveData.refresh()
            }
            liveDataAddNewModel.postValue(response)
        }
    }

    private fun addCustomTfliteModel(path: String) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val destFile = copyModelFileToDestination(path)
            val response =
                addTfliteModelUseCase.executeUseCase(AddModelUseCase.RequestValues(destFile))
            if (response.isSuccess()) {
                modelsListLiveData.refresh()
            }
            liveDataAddNewModel.postValue(response)
        }
    }

    private fun resolveNeuralModels(): List<ModelEntity> {
        val useCaseResponse: UseCase.Resource<ResolveModelsListUseCase.ResponseValue> =
            modelsListUseCase.executeUseCase(ResolveModelsListUseCase.RequestValues())
        if (useCaseResponse.isSuccess()) {
            val response: ResolveModelsListUseCase.ResponseValue? = useCaseResponse.data
            return response?.data ?: emptyList()
        }
        return emptyList()
    }

    private fun getModelList(): RefreshLiveData<List<ModelEntity>> {
        return RefreshLiveData { callback: RefreshAction.Callback<List<ModelEntity>> ->
            viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
                callback.onDataLoaded(resolveNeuralModels())
            }
        }
    }

    enum class ModelFormat {
        TFLITE,
        OPENCV
    }

    class SelectModelStatus(
        val success: Boolean,
        val modelType: ModelFormat,
        val modelPath: String
    )

    suspend fun onSelectNeuralModelFile(
        uri: Uri
    ): SelectModelStatus = withContext(Dispatchers.IO) {
        val filePath =
            FileUtils.getRealFilePath(getApplication(), uri).trim()
        return@withContext if (filePath.endsWith(".tflite")) {
            addCustomTfliteModel(filePath)
            SelectModelStatus(true, ModelFormat.TFLITE, filePath)
        } else {
            /***
             * https://docs.opencv.org/4.5.2/d6/d0f/group__dnn.html#ga3b34fe7a29494a6a4295c169a7d32422
             */
            /***
             * https://docs.opencv.org/4.5.2/d6/d0f/group__dnn.html#ga3b34fe7a29494a6a4295c169a7d32422
             */
            val opencvSupported = supportedModels.contains(File(filePath).extension)
            if (opencvSupported) {
                addCustomOpenCVModel(filePath)
                SelectModelStatus(true, ModelFormat.OPENCV, filePath)
            } else {
                SelectModelStatus(false, ModelFormat.OPENCV, filePath)
            }
        }
    }
}