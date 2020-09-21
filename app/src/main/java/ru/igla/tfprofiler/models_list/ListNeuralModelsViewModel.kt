package ru.igla.tfprofiler.models_list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.igla.tfprofiler.core.UseCase
import ru.igla.tfprofiler.media_track.MediaPathProvider
import ru.igla.tfprofiler.reports_list.RefreshLiveData
import ru.igla.tfprofiler.reports_list.RefreshLiveData.RefreshAction
import ru.igla.tfprofiler.utils.ExceptionHandler
import ru.igla.tfprofiler.video.FileUtils
import java.io.File


class ListNeuralModelsViewModel(application: Application) : AndroidViewModel(application) {

    private val modelsListUseCase by lazy {
        ResolveModelsListUseCase(application)
    }

    private val addCustomModelUseCase by lazy {
        AddCustomModelUseCase(application)
    }

    val liveDataAddNewModel = MutableLiveData<UseCase.Status>()

    private val exceptionHandler = ExceptionHandler { _, _ -> }

    var modelsListLiveData: RefreshLiveData<List<ModelEntity>> = getModelList().apply {
        refresh()
    }

    fun addCustomTfliteModel(path: String) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val file = File(path)
            val filename = file.name
            val destinationFilename =
                MediaPathProvider.getCustomModelsPath(getApplication()) + "/" + filename
            val destFile = FileUtils.copyFile(path, destinationFilename)

            val response =
                addCustomModelUseCase.executeUseCase(AddCustomModelUseCase.RequestValues(destFile))
            if (response.isSuccess()) {
                modelsListLiveData.refresh()
            }
            liveDataAddNewModel.postValue(response.status)
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
}