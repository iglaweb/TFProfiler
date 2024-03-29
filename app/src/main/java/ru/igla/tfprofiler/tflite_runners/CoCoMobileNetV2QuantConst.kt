package ru.igla.tfprofiler.tflite_runners

import ru.igla.tfprofiler.BuildConfig
import ru.igla.tfprofiler.core.domain.ColorSpace
import ru.igla.tfprofiler.core.domain.ImageConfig
import ru.igla.tfprofiler.core.domain.InputShapeType
import ru.igla.tfprofiler.core.domain.ModelOptimizedType

/***
 * https://tfhub.dev/google/lite-model/object_detection/mobile_object_localizer_v1/1/default/1
 */
object CoCoMobileNetV2QuantConst : ImageConfig() {
    private const val TF_OD_API_IMAGE_SIZE = 192
    private val TF_OD_API_MODEL_FORMAT = ModelOptimizedType.QUANTIZED
    private const val TF_OD_API_MODEL_FILE = BuildConfig.ASSET_MOBILENETV2_FILE
    private const val TF_OD_API_LABELS_FILE = ""
    override val details: String
        get() = "Convolutional neural network architecture"

    override val imageHeight: Int
        get() = TF_OD_API_IMAGE_SIZE
    override val imageWidth: Int
        get() = TF_OD_API_IMAGE_SIZE
    override val modelFormat: ModelOptimizedType
        get() = TF_OD_API_MODEL_FORMAT
    override val inputShapeType: InputShapeType
        get() = InputShapeType.NHWC
    override val colorSpace: ColorSpace
        get() = ColorSpace.COLOR
    override val modelFile: String
        get() = TF_OD_API_MODEL_FILE
    override val labelFile: String
        get() = TF_OD_API_LABELS_FILE
    override val source: String
        get() = "https://arxiv.org/abs/1801.04381"
}