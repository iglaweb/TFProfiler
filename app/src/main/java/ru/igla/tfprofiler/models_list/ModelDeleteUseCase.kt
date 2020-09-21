package ru.igla.tfprofiler.models_list

import android.app.Application
import ru.igla.tfprofiler.core.UseCase
import ru.igla.tfprofiler.db.AppDatabase
import ru.igla.tfprofiler.db.RoomModelsDbController

class ModelDeleteUseCase(val application: Application) :
    UseCase<ModelDeleteUseCase.RequestValues,
            ModelDeleteUseCase.ResponseValue>() {

    private val roomReportDbController by lazy {
        RoomModelsDbController(
            AppDatabase.getInstance(application)
        )
    }

    override fun executeUseCase(requestValues: RequestValues): Resource<ResponseValue> {
        roomReportDbController.deleteModel(requestValues.data)
        val responseValue = ResponseValue()
        return Resource.success(responseValue)
    }

    class RequestValues(val data: Long) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}