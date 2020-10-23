package ru.igla.tfprofiler.models_list

import android.app.Application
import ru.igla.tfprofiler.core.ModelType
import ru.igla.tfprofiler.core.Timber
import ru.igla.tfprofiler.core.UseCase
import ru.igla.tfprofiler.core.tflite.TensorFlowUtils
import ru.igla.tfprofiler.db.AppDatabase
import ru.igla.tfprofiler.db.DbModelItem
import ru.igla.tfprofiler.db.NeuralModelsProvider
import ru.igla.tfprofiler.db.RoomModelsDbController
import ru.igla.tfprofiler.utils.forEachNoIterator
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

        val dbModels = roomModelsDbController.getModels()
        if (dbModels.isEmpty()) { //first start
            val items = NeuralModelsProvider.resolveBuiltInModels(application)
            items.forEachNoIterator {
                val modelPath = it.model.modelFile
                if (!TensorFlowUtils.isAssetFileExists(application, modelPath)) {
                    Timber.e(Exception("Predefined model assets/$modelPath not exists"))
                } else {
                    val item = DbModelItem(
                        idModel = it.id,
                        modelType = it.modelType,
                        title = it.modelType.title,
                        inputWidth = it.model.imageWidth,
                        inputHeight = it.model.imageHeight,
                        modelPath = it.model.modelFile,
                        labelPath = it.model.labelFile,
                        source = it.model.source,
                        details = it.model.details,
                        quantized = it.model.quantized,
                        colorSpace = it.model.colorSpace
                    )
                    roomModelsDbController.insertModel(item)
                }
            }
        }

        var id = 1L
        val list = roomModelsDbController.getModels().filter {
            it.modelType != ModelType.CUSTOM || File(it.modelPath).exists() //filter out non existing custom models
        }
            .map { item ->
                val modelConfig = ModelConfig(
                    item.idModel,
                    item.inputWidth,
                    item.inputHeight,
                    item.quantized,
                    item.colorSpace
                )
                if (item.modelType == ModelType.CUSTOM) {
                    ModelEntity(
                        id = id++,
                        modelType = item.modelType,
                        name = item.title,
                        details = "File: " + item.modelPath, //we do not have description

                        modelConfig = modelConfig,

                        modelFile = item.modelPath,
                        labelFile = ""
                    )
                } else {
                    ModelEntity(
                        id = id++,
                        modelType = item.modelType,
                        name = item.modelType.title,
                        details = item.details,

                        modelConfig = modelConfig,

                        modelFile = item.modelPath,
                        labelFile = item.labelPath,
                        source = item.source
                    )
                }
            }


        val responseValue = ResponseValue(list)
        return Resource.success(responseValue)
    }

    class RequestValues : UseCase.RequestValues
    class ResponseValue(val data: List<ModelEntity>) : UseCase.ResponseValue
}