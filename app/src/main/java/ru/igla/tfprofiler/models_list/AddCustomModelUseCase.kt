package ru.igla.tfprofiler.models_list

import android.app.Application
import org.tensorflow.lite.DataType
import ru.igla.tfprofiler.core.ColorSpace
import ru.igla.tfprofiler.core.ModelType
import ru.igla.tfprofiler.core.Timber
import ru.igla.tfprofiler.core.UseCase
import ru.igla.tfprofiler.core.tflite.TFInterpreterWrapper
import ru.igla.tfprofiler.db.AppDatabase
import ru.igla.tfprofiler.db.DbModelItem
import ru.igla.tfprofiler.db.RoomModelsDbController
import java.io.File

class AddCustomModelUseCase(val application: Application) :
    UseCase<AddCustomModelUseCase.RequestValues,
            AddCustomModelUseCase.ResponseValue>() {

    private val roomModelsDbController by lazy {
        RoomModelsDbController(
            AppDatabase.getInstance(application)
        )
    }

    override fun executeUseCase(requestValues: RequestValues): Resource<ResponseValue> {
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
                val isModelQuantized = probabilityDataType == DataType.UINT8

                val file = File(modelPath)
                val modelId =
                    addModelDb(
                        file.name,
                        modelPath,
                        imageSizeX,
                        imageSizeY,
                        isModelQuantized,
                        colorSpace
                    )
                if (modelId != -1L) {
                    val responseValue = ResponseValue()
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
        quantized: Boolean,
        colorSpace: ColorSpace
    ): Long {
        val item = DbModelItem(
            idModel = 0,
            modelType = ModelType.CUSTOM,
            title = filename,
            inputWidth = inputWidth,
            inputHeight = inputHeight,
            modelPath = modelPath,
            labelPath = "", //default
            source = "",
            details = "",
            quantized = quantized,
            colorSpace = colorSpace
        )
        return roomModelsDbController.insertModel(item)
    }

    class RequestValues(val modelPath: String) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}