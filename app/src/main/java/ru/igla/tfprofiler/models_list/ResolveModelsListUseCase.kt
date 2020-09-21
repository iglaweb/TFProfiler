package ru.igla.tfprofiler.models_list

import android.app.Application
import ru.igla.tfprofiler.UseCase
import ru.igla.tfprofiler.core.Timber
import ru.igla.tfprofiler.core.tflite.TensorFlowUtils
import ru.igla.tfprofiler.db.AppDatabase
import ru.igla.tfprofiler.db.NeuralModelsProvider
import ru.igla.tfprofiler.db.RoomModelsDbController
import java.io.File

class ResolveModelsListUseCase(val application: Application) :
    UseCase<ResolveModelsListUseCase.RequestValues,
            ResolveModelsListUseCase.ResponseValue>() {

    private val roomModelsDbController by lazy {
        RoomModelsDbController(
            AppDatabase.getInstance(application)
        )
    }

    override fun executeUseCase(requestValues: RequestValues): Resource<ResponseValue> {
        val list = mutableListOf<ModelEntity>()
        val items = NeuralModelsProvider.resolveBuiltInModels(application)
        var idStart = 0L
        for (item in items) {
            val modelPath = item.modelFile
            if (!TensorFlowUtils.isAssetFileExists(application, modelPath)) {
                Timber.e(Exception("Predefined model assets/$modelPath not exists"))
                continue
            }
            idStart = kotlin.math.max(item.id, idStart)
            list.add(
                ModelEntity(
                    id = item.id,
                    tableId = -1,
                    modelType = item.modelType,
                    name = item.modelType.title,
                    details = item.details,
                    inputSize = item.inputSize,
                    quantized = item.quantized,
                    modelFile = modelPath,
                    labelFile = item.labelFile,
                    source = item.source
                )
            )
        }
        //add custom models
        val customModels = getDbCustomModels(++idStart)
        list.addAll(customModels)

        val responseValue = ResponseValue(list)
        return Resource.success(responseValue)
    }

    private fun getDbCustomModels(idStart: Long): List<ModelEntity> {
        var id = idStart
        return roomModelsDbController.getModels().filter {
            File(it.modelPath).exists()
        }
            .map {
                ModelEntity(
                    id = id++,
                    tableId = it.idModel,
                    modelType = it.modelType,
                    name = it.title,
                    details = "File: " + it.modelPath, //we do not have description
                    inputSize = it.inputSize,
                    quantized = it.quantized,
                    modelFile = it.modelPath,
                    labelFile = ""
                )
            }
    }

    class RequestValues : UseCase.RequestValues
    class ResponseValue(val data: List<ModelEntity>) : UseCase.ResponseValue
}