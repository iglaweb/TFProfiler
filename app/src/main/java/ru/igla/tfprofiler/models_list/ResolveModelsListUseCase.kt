package ru.igla.tfprofiler.models_list

import android.app.Application
import ru.igla.tfprofiler.core.*
import ru.igla.tfprofiler.core.domain.*
import ru.igla.tfprofiler.core.tflite.TensorFlowUtils
import ru.igla.tfprofiler.db.AppDatabase
import ru.igla.tfprofiler.db.DbModelItem
import ru.igla.tfprofiler.db.NeuralModelsProvider
import ru.igla.tfprofiler.db.RoomModelsDbController
import ru.igla.tfprofiler.models_list.domain.ModelConfig
import ru.igla.tfprofiler.models_list.domain.ModelEntity
import ru.igla.tfprofiler.utils.forEachNoIterator
import timber.log.Timber
import java.io.File

class ResolveModelsListUseCase(val application: Application) :
    UseCase<ResolveModelsListUseCase.RequestValues,
            ResolveModelsListUseCase.ResponseValue>() {

    private val roomModelsDbController by lazy {
        RoomModelsDbController(
            AppDatabase.getInstance(application)
        )
    }

    private fun resolveBuiltInModels() {
        val dbModels = roomModelsDbController.getModels()
        if (dbModels.isEmpty()) { //first start
            val items = NeuralModelsProvider.resolveBuiltInModels(application)
            items.forEachNoIterator {
                val modelPath = it.modelInfoConfig.modelFile
                if (!TensorFlowUtils.isAssetFileExists(application, modelPath)) {
                    Timber.e(Exception("Predefined model assets/$modelPath not exists"))
                } else {
                    when (it.modelInfoConfig) {
                        is TextConfig -> {
                            val item = DbModelItem(
                                idModel = it.id,
                                modelType = it.modelType,
                                title = it.modelType.title,
                                inputWidth = -1,
                                inputHeight = -1,
                                modelPath = it.modelInfoConfig.modelFile,
                                labelPath = it.modelInfoConfig.labelFile,
                                source = it.modelInfoConfig.source,
                                details = it.modelInfoConfig.details,
                                modelFormat = it.modelInfoConfig.modelFormat,
                                colorSpace = ColorSpace.COLOR,
                                inputShapeType = InputShapeType.NCHW
                            )
                            roomModelsDbController.insertModel(item)
                        }
                        is ImageConfig -> {
                            val item = DbModelItem(
                                idModel = it.id,
                                modelType = it.modelType,
                                title = it.modelType.title,
                                inputWidth = it.modelInfoConfig.imageWidth,
                                inputHeight = it.modelInfoConfig.imageHeight,
                                modelPath = it.modelInfoConfig.modelFile,
                                labelPath = it.modelInfoConfig.labelFile,
                                source = it.modelInfoConfig.source,
                                details = it.modelInfoConfig.details,
                                modelFormat = it.modelInfoConfig.modelFormat,
                                colorSpace = it.modelInfoConfig.colorSpace,
                                inputShapeType = it.modelInfoConfig.inputShapeType
                            )
                            roomModelsDbController.insertModel(item)
                        }
                    }
                }
            }
        }
    }

    override fun executeUseCase(requestValues: RequestValues): Resource<ResponseValue> {
        resolveBuiltInModels()

        var id = 1L
        val list = roomModelsDbController.getModels().filter {
            !it.modelType.isCustomModel() || File(it.modelPath).exists() //filter out non existing custom models
        }
            .map { item ->
                val modelConfig = ModelConfig(
                    item.idModel,
                    Size(item.inputWidth, item.inputHeight),
                    item.modelFormat,
                    item.colorSpace,
                    item.inputShapeType
                )
                if (item.modelType.isCustomModel()) {
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