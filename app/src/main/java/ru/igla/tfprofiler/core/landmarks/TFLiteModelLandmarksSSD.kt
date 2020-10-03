package ru.igla.tfprofiler.core.landmarks

import ru.igla.tfprofiler.BuildConfig
import ru.igla.tfprofiler.core.BuiltModel
import ru.igla.tfprofiler.core.ColorSpace

/***
 * https://raw.githubusercontent.com/google/mediapipe/master/mediapipe/models/face_landmark.tflite
 */
object TFLiteModelLandmarksSSD : BuiltModel {
    private const val TF_OD_API_INPUT_SIZE = 192
    private const val TF_OD_API_MODEL_FILE = BuildConfig.ASSET_LANDMARK_FILE
    private const val TF_OD_API_IS_QUANTIZED = false
    override val details: String
        get() = ""

    override val imageHeight: Int
        get() = TF_OD_API_INPUT_SIZE
    override val imageWidth: Int
        get() = TF_OD_API_INPUT_SIZE
    override val quantized: Boolean
        get() = TF_OD_API_IS_QUANTIZED
    override val colorSpace: ColorSpace
        get() = ColorSpace.COLOR
    override val modelFile: String
        get() = TF_OD_API_MODEL_FILE
    override val labelFile: String
        get() = ""
    override val source: String
        get() = "https://arxiv.org/abs/1907.06724"
}