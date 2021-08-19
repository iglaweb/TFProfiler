package ru.igla.tfprofiler.models_list

import android.app.Application
import org.tensorflow.lite.DataType
import ru.igla.tfprofiler.core.*
import ru.igla.tfprofiler.core.tflite.TFInterpreterWrapper
import ru.igla.tfprofiler.db.AppDatabase
import ru.igla.tfprofiler.db.DbModelItem
import ru.igla.tfprofiler.db.RoomModelsDbController
import timber.log.Timber
import java.io.File

class AddTfliteModelUseCase(val application: Application) :
    UseCase<AddModelUseCase.RequestValues,
            AddModelUseCase.ResponseValue>() {

    private val roomModelsDbController by lazy {
        RoomModelsDbController(
            AppDatabase.getInstance(application)
        )
    }

    override fun executeUseCase(requestValues: AddModelUseCase.RequestValues): Resource<AddModelUseCase.ResponseValue> {
        val modelPath = requestValues.modelPath
        try {
            TFInterpreterWrapper.createTestInterpeter(
                application,
                modelPath
            ).use {
                val imageTensorIndex = 0
                val imageShape = it.interpreter.getInputTensor(imageTensorIndex)
                    .shape() // {1, height, width, 3}
                val imageSizeY = imageShape[1]
                val imageSizeX = imageShape[2]
                val colorSpace = if (imageShape[3] == 1) ColorSpace.GRAYSCALE else ColorSpace.COLOR

                val probabilityTensorIndex = 0
                val probabilityDataType: DataType =
                    it.interpreter.getOutputTensor(probabilityTensorIndex).dataType()
                val modelFormat =
                    if (probabilityDataType == DataType.UINT8) ModelOptimizedType.QUANTIZED else ModelOptimizedType.FLOATING

                val file = File(modelPath)
                val modelId =
                    addModelDb(
                        file.name,
                        modelPath,
                        imageSizeX,
                        imageSizeY,
                        modelFormat,
                        colorSpace,
                        InputShapeType.NHWC
                    )
                if (modelId != -1L) {
                    val responseValue = AddModelUseCase.ResponseValue()
                    return Resource.success(responseValue)
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            return Resource.error(e.message, e)
        }
        return Resource.error("Unknown error occurred")
    }

    private fun addModelDb(
        filename: String,
        modelPath: String,
        inputWidth: Int,
        inputHeight: Int,
        modelFormat: ModelOptimizedType,
        colorSpace: ColorSpace,
        inputShapeType: InputShapeType
    ): Long {
        val item = DbModelItem(
            idModel = 0,
            modelType = ModelType.CUSTOM_TFLITE,
            title = filename,
            inputWidth = inputWidth,
            inputHeight = inputHeight,
            modelPath = modelPath,
            labelPath = "", //default
            source = "",
            details = "",
            modelFormat = modelFormat,
            colorSpace = colorSpace,
            inputShapeType = inputShapeType
        )
        return roomModelsDbController.insertModel(item)
    }
}