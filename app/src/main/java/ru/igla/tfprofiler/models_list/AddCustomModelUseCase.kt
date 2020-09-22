package ru.igla.tfprofiler.models_list

import android.app.Application
import org.tensorflow.lite.DataType
import ru.igla.tfprofiler.core.UseCase
import ru.igla.tfprofiler.core.ModelType
import ru.igla.tfprofiler.core.Timber
import ru.igla.tfprofiler.db.AppDatabase
import ru.igla.tfprofiler.db.DbModelItem
import ru.igla.tfprofiler.db.RoomModelsDbController
import ru.igla.tfprofiler.core.tflite.TFInterpreterWrapper
import java.io.File
import kotlin.math.min

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

                val probabilityTensorIndex = 0
                val probabilityDataType: DataType =
                    it.interpreter.getOutputTensor(probabilityTensorIndex).dataType()
                val isModelQuantized = probabilityDataType == DataType.UINT8

                val file = File(modelPath)
                val modelId =
                    addModelDb(file.name, modelPath, min(imageSizeX, imageSizeY), isModelQuantized)
                if (modelId != -1L) {
                    val responseValue = ResponseValue()
                    return Resource.success(responseValue)
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            return Resource.error(e.message)
        }
        return Resource.error("Failed to add model")
    }

    private fun addModelDb(
        filename: String,
        modelPath: String,
        inputSize: Int,
        quantized: Boolean
    ): Long {
        val item = DbModelItem(
            idModel = 0,
            modelType = ModelType.CUSTOM,
            title = filename,
            inputSize = inputSize,
            modelPath = modelPath,
            quantized = quantized
        )
        return roomModelsDbController.insertModel(item)
    }

    class RequestValues(val modelPath: String) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}