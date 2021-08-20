package ru.igla.tfprofiler.tflite_runners.base

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import ru.igla.tfprofiler.core.ModelType
import ru.igla.tfprofiler.models_list.ModelEntity
import ru.igla.tfprofiler.tflite_runners.*
import ru.igla.tfprofiler.tflite_runners.blazeface.TFLiteObjectDetectionAPIModelBlazeface
import ru.igla.tfprofiler.tflite_runners.domain.ImageResult

object ImageClassifierFactory {
    @Throws(Exception::class)
    @JvmStatic
    @WorkerThread
    fun create(
        context: Context,
        modelEntity: ModelEntity,
        modelOptions: ModelOptions
    ): Classifier<List<Bitmap>, List<ImageResult>> {
        val classifier: Classifier<List<Bitmap>, List<ImageResult>> =
            when (modelEntity.modelType) {
                ModelType.CUSTOM_OPENCV -> {
                    OpenCVImageObjectDetectionAPIModelBase()
                }
                ModelType.CUSTOM_TFLITE -> {
                    TFLiteImageCustomModelClassifier()
                }
                ModelType.MOBILENET_V1_OBJECT_DETECT -> {
                    TFLiteObjectDetectionModelCOCOMobileNetV1()
                }
                ModelType.MOBILENET_V2_OBJECT_DETECT -> {
                    TFLiteObjectDetectionModelCOCOMobileNetV2()
                }
                ModelType.BLAZEFACE_MEDIAPIPE -> {
                    TFLiteObjectDetectionAPIModelBlazeface()
                }
                ModelType.LANDMARKS468_MEDIAPIPE -> {
                    TFLiteImageModelFacemeshLandmarks()
                }
                ModelType.YOLOV4 -> {
                    TFLiteObjectDetectionAPIYoloV4Classifier()
                }
                else -> {
                    throw IllegalStateException("Model ${modelEntity.modelType.title} not supported here")
                }
            }
        classifier.init(context, modelEntity, modelOptions)
        return classifier
    }
}
