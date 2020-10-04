package ru.igla.tfprofiler.tflite_runners.base

import android.content.Context
import androidx.annotation.WorkerThread
import ru.igla.tfprofiler.core.ModelType
import ru.igla.tfprofiler.models_list.ModelEntity
import ru.igla.tfprofiler.tflite_runners.*

object ClassifierFactory {
    @Throws(Exception::class)
    @JvmStatic
    @WorkerThread
    fun create(
        context: Context,
        modelEntity: ModelEntity,
        modelOptions: ModelOptions
    ): Classifier<Classifier.Recognition> {
        val classifier = when (modelEntity.modelType) {
            ModelType.CUSTOM -> {
                TFLiteCustomModelClassifier()
            }
            ModelType.MOBILENET_V1_OBJECT_DETECT -> {
                TFLiteObjectDetectionAPIModel_COCOMobileNetV1()
            }
            ModelType.MOBILENET_V2_OBJECT_DETECT -> {
                TFLiteObjectDetectionAPIModel_COCOMobileNetV2()
            }
            ModelType.BLAZEFACE_MEDIAPIPE -> {
                TFLiteObjectDetectionAPIModelBlazeface()
            }
            ModelType.LANDMARKS468_MEDIAPIPE -> {
                TFLiteAPIModelFacemeshLandmarks()
            }
            ModelType.YOLOV4 -> {
                TFLiteObjectDetectionAPIYoloV4Classifier()
            }
        }
        classifier.init(context, modelEntity, modelOptions)
        return classifier
    }
}
