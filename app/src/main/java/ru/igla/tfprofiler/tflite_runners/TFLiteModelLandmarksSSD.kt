package ru.igla.tfprofiler.tflite_runners

import ru.igla.tfprofiler.BuildConfig
import ru.igla.tfprofiler.core.domain.ColorSpace
import ru.igla.tfprofiler.core.domain.ImageConfig
import ru.igla.tfprofiler.core.domain.InputShapeType
import ru.igla.tfprofiler.core.domain.ModelOptimizedType

/***
 * https://raw.githubusercontent.com/google/mediapipe/master/mediapipe/models/face_landmark.tflite
 */
object TFLiteModelLandmarksSSD : ImageConfig() {
    private const val TF_OD_API_INPUT_SIZE = 192
    private const val TF_OD_API_MODEL_FILE = BuildConfig.ASSET_LANDMARK_FILE
    private val TF_OD_API_IS_QUANTIZED = ModelOptimizedType.FLOATING
    override val details: String
        get() = "3D mesh representation of a human face with 468 vertices"

    override val imageHeight: Int
        get() = TF_OD_API_INPUT_SIZE
    override val imageWidth: Int
        get() = TF_OD_API_INPUT_SIZE
    override val modelFormat: ModelOptimizedType
        get() = TF_OD_API_IS_QUANTIZED
    override val colorSpace: ColorSpace
        get() = ColorSpace.COLOR
    override val inputShapeType: InputShapeType
        get() = InputShapeType.NHWC
    override val modelFile: String
        get() = TF_OD_API_MODEL_FILE
    override val labelFile: String
        get() = ""
    override val source: String
        get() = "https://arxiv.org/abs/1907.06724"
}