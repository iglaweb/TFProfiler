package ru.igla.tfprofiler.tflite_runners

import ru.igla.tfprofiler.BuildConfig
import ru.igla.tfprofiler.core.BuiltModel
import ru.igla.tfprofiler.core.ColorSpace
import ru.igla.tfprofiler.core.InputShapeType
import ru.igla.tfprofiler.core.ModelFormat

object CoCoMobileNetV1QuantConst : BuiltModel {
    private const val TF_OD_API_IMAGE_SIZE = 300
    private val TF_OD_API_MODEL_FORMAT = ModelFormat.FLOATING
    private const val TF_OD_API_MODEL_FILE = BuildConfig.ASSET_MOBILENETV1_FILE + "detect.tflite"
    private const val TF_OD_API_LABELS_FILE =
        "file:///android_asset/" + BuildConfig.ASSET_MOBILENETV1_FILE + "labelmap.txt"
    override val details: String
        get() = "Based on depth-wise separable convolutions"

    override val imageHeight: Int
        get() = TF_OD_API_IMAGE_SIZE
    override val imageWidth: Int
        get() = TF_OD_API_IMAGE_SIZE
    override val modelFormat: ModelFormat
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
        get() = "https://arxiv.org/pdf/1704.04861.pdf"
}