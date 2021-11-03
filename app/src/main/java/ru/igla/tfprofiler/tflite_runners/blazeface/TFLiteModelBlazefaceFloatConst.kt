package ru.igla.tfprofiler.tflite_runners.blazeface

import ru.igla.tfprofiler.BuildConfig
import ru.igla.tfprofiler.core.domain.ColorSpace
import ru.igla.tfprofiler.core.domain.ImageConfig
import ru.igla.tfprofiler.core.domain.InputShapeType
import ru.igla.tfprofiler.core.domain.ModelOptimizedType


/***
 * Selfie camera https://github.com/google/mediapipe/blob/master/mediapipe/models/face_detection_front.tflite
 * Back camera https://github.com/google/mediapipe/blob/master/mediapipe/models/face_detection_back.tflite
 */
object TFLiteModelBlazefaceFloatConst : ImageConfig() {

    private const val TF_OD_API_INPUT_SIZE = 128
    const val TF_OD_API_MODEL_FLOATING_FILE_FRONT_CAMERA = BuildConfig.ASSET_BLAZEFACE_FILE
    const val TF_OD_API_MODEL_FLOATING_FILE_BACK_CAMERA = "face_detection_blazeface_back.tflite"
    private val TF_OD_API_MODEL_FORMAT = ModelOptimizedType.FLOATING
    override val details: String
        get() = "Based on Single Shot Multibox Detector (SSD)"

    override val imageHeight: Int
        get() = TF_OD_API_INPUT_SIZE
    override val imageWidth: Int
        get() = TF_OD_API_INPUT_SIZE
    override val modelFormat: ModelOptimizedType
        get() = TF_OD_API_MODEL_FORMAT
    override val inputShapeType: InputShapeType
        get() = InputShapeType.NHWC
    override val colorSpace: ColorSpace
        get() = ColorSpace.COLOR
    override val modelFile: String
        get() = TF_OD_API_MODEL_FLOATING_FILE_FRONT_CAMERA
    override val labelFile: String
        get() = ""
    override val source: String
        get() = "https://arxiv.org/abs/1907.05047"
}