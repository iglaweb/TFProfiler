package ru.igla.tfprofiler.db

import android.content.Context
import ru.igla.tfprofiler.core.ModelType
import ru.igla.tfprofiler.core.blazeface.TFLiteModelBlazefaceFloatWrapper
import ru.igla.tfprofiler.core.landmarks.TFLiteModelLandmarksSSD
import ru.igla.tfprofiler.core.tflite.TensorFlowUtils
import ru.igla.tfprofiler.models_list.ModelItem
import ru.igla.tfprofiler.tflite_runners.CoCoMobileNetV1QuantConst
import ru.igla.tfprofiler.tflite_runners.CoCoMobileNetV2QuantConst
import ru.igla.tfprofiler.yolov4.YoloV4Const

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
                "Based on Single Shot Multibox Detector (SSD)",
                TFLiteModelBlazefaceFloatWrapper.TF_OD_API_INPUT_SIZE,
                TFLiteModelBlazefaceFloatWrapper.TF_OD_API_IS_QUANTIZED,
                TFLiteModelBlazefaceFloatWrapper.TF_OD_API_MODEL_FLOATING_FILE_FRONT_CAMERA,
                "",
                "https://arxiv.org/abs/1907.05047"
            ).apply {
                setMeta(
                    TensorFlowUtils.getMetaData(
                        context,
                        TFLiteModelBlazefaceFloatWrapper.TF_OD_API_MODEL_FLOATING_FILE_FRONT_CAMERA
                    )
                )
            },
            ModelItem(
                2,
                ModelType.MOBILENET_V1_OBJECT_DETECT,
                "Based on depth-wise separable convolutions",
                CoCoMobileNetV1QuantConst.TF_OD_API_IMAGE_SIZE,
                CoCoMobileNetV1QuantConst.TF_OD_API_IS_QUANTIZED,
                CoCoMobileNetV1QuantConst.TF_OD_API_MODEL_FILE,
                CoCoMobileNetV1QuantConst.TF_OD_API_LABELS_FILE,
                "https://arxiv.org/pdf/1704.04861.pdf"
            ).apply {
                setMeta(
                    TensorFlowUtils.getMetaData(
                        context,
                        CoCoMobileNetV1QuantConst.TF_OD_API_MODEL_FILE
                    )
                )
            },
            ModelItem(
                3,
                ModelType.MOBILENET_V2_OBJECT_DETECT,
                "Convolutional neural network architecture",
                CoCoMobileNetV2QuantConst.TF_OD_API_IMAGE_SIZE,
                CoCoMobileNetV2QuantConst.TF_OD_API_IS_QUANTIZED,
                CoCoMobileNetV2QuantConst.TF_OD_API_MODEL_FILE,
                CoCoMobileNetV2QuantConst.TF_OD_API_LABELS_FILE,
                "https://arxiv.org/abs/1801.04381"
            ).apply {
                setMeta(
                    TensorFlowUtils.getMetaData(
                        context,
                        CoCoMobileNetV2QuantConst.TF_OD_API_MODEL_FILE
                    )
                )
            },
            ModelItem(
                4,
                ModelType.LANDMARKS468_MEDIAPIPE,
                "",
                TFLiteModelLandmarksSSD.TF_OD_API_INPUT_SIZE,
                TFLiteModelLandmarksSSD.TF_OD_API_IS_QUANTIZED,
                TFLiteModelLandmarksSSD.TF_OD_API_MODEL_FILE,
                "",
                "https://arxiv.org/abs/1907.06724"
            ),
            ModelItem(
                5,
                ModelType.YOLOV4,
                "CSP connections with the Darknet-53",
                YoloV4Const.TF_OD_API_INPUT_SIZE,
                YoloV4Const.TF_OD_API_IS_QUANTIZED,
                YoloV4Const.TF_OD_API_MODEL_FILE,
                YoloV4Const.TF_OD_API_LABELS_FILE,
                "https://arxiv.org/abs/2004.10934"
            )
        )
    }
}