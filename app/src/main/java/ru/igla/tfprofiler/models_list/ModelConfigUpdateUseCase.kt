package ru.igla.tfprofiler.models_list

import android.app.Application
import ru.igla.tfprofiler.core.UseCase
import ru.igla.tfprofiler.db.AppDatabase
import ru.igla.tfprofiler.db.RoomModelsDbController

class ModelConfigUpdateUseCase(val application: Application) :
    UseCase<ModelConfigUpdateUseCase.RequestValues,
            ModelConfigUpdateUseCase.ResponseValue>() {

    private val roomReportDbController by lazy {
        RoomModelsDbController(
            AppDatabase.getInstance(application)
        )
    }

    override fun executeUseCase(requestValues: RequestValues): Resource<ResponseValue> {
        roomReportDbController.updateModel(
            requestValues.modelId,
            requestValues.modelConfig.inputWidth,
            requestValues.modelConfig.inputHeight,
            requestValues.modelConfig.modelFormat,
            requestValues.modelConfig.colorSpace,
            requestValues.modelConfig.inputShapeType
        )
        val responseValue = ResponseValue()
        return Resource.success(responseValue)
    }

    class RequestValues(val modelId: Long, val modelConfig: ModelConfig) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}