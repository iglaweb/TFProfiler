package ru.igla.tfprofiler.models_list

import android.app.Application
import ru.igla.tfprofiler.core.*
import ru.igla.tfprofiler.core.tflite.OpenCVInterpreterWrapper
import ru.igla.tfprofiler.db.AppDatabase
import ru.igla.tfprofiler.db.DbModelItem
import ru.igla.tfprofiler.db.RoomModelsDbController
import java.io.File

class AddCustomOpenCVModelUseCase(val application: Application) :
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
            OpenCVInterpreterWrapper.testCreateDnnModel(
                modelPath
            ).let {
                val imageSizeX = 100
                val imageSizeY = 100
                val file = File(modelPath)
                val modelId =
                    addModelDb(
                        file.name,
                        modelPath,
                        imageSizeX,
                        imageSizeY,
                        ModelFormat.FLOATING,
                        ColorSpace.GRAYSCALE,
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
        modelFormat: ModelFormat,
        colorSpace: ColorSpace,
        inputShapeType: InputShapeType
    ): Long {
        val item = DbModelItem(
            idModel = 0,
            modelType = ModelType.CUSTOM_OPENCV,
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