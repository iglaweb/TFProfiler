package ru.igla.tfprofiler.db

import android.content.Context
import ru.igla.tfprofiler.core.ModelType
import ru.igla.tfprofiler.core.tflite.TensorFlowUtils
import ru.igla.tfprofiler.models_list.domain.ModelItem
import ru.igla.tfprofiler.tflite_runners.*
import ru.igla.tfprofiler.tflite_runners.blazeface.TFLiteModelBlazefaceFloatConst
import ru.igla.tfprofiler.tflite_runners.text_classification.TextClassifyModelConst

/***
 * For other models look here:
 * https://www.tensorflow.org/lite/guide/hosted_models
 * https://www.tensorflow.org/lite/models
 * https://github.com/google/mediapipe/tree/master/mediapipe/models
 */
object NeuralModelsProvider {
    fun resolveBuiltInModels(context: Context): List<ModelItem> {
        return listOf(
            ModelItem(
                1,
                ModelType.BLAZEFACE_MEDIAPIPE,
                TFLiteModelBlazefaceFloatConst
            ).apply {
                setMeta(
                    TensorFlowUtils.getMetaData(
                        context,
                        TFLiteModelBlazefaceFloatConst.TF_OD_API_MODEL_FLOATING_FILE_FRONT_CAMERA
                    )
                )
            },
            ModelItem(
                2,
                ModelType.MOBILENET_V1_OBJECT_DETECT,
                CoCoMobileNetV1QuantConst
            ).apply {
                setMeta(
                    TensorFlowUtils.getMetaData(
                        context,
                        CoCoMobileNetV1QuantConst.modelFile
                    )
                )
            },
            ModelItem(
                3,
                ModelType.MOBILENET_V2_OBJECT_DETECT,
                CoCoMobileNetV2QuantConst
            ).apply {
                setMeta(
                    TensorFlowUtils.getMetaData(
                        context,
                        CoCoMobileNetV2QuantConst.modelFile
                    )
                )
            },
            ModelItem(
                4,
                ModelType.LANDMARKS468_MEDIAPIPE,
                TFLiteModelLandmarksSSD
            ),
            ModelItem(
                5,
                ModelType.YOLOV4,
                YoloV4Const
            ),
            ModelItem(
                6,
                ModelType.TEXT_CLASSIFICATION,
                TextClassifyModelConst
            )
        )
    }
}