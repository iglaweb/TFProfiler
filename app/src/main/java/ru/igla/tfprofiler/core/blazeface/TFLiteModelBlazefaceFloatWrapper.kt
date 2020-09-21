package ru.igla.tfprofiler.core.blazeface

import ru.igla.tfprofiler.BuildConfig


/***
 * Selfie camera https://github.com/google/mediapipe/blob/master/mediapipe/models/face_detection_front.tflite
 * Back camera https://github.com/google/mediapipe/blob/master/mediapipe/models/face_detection_back.tflite
 */
object TFLiteModelBlazefaceFloatWrapper {

    const val TF_OD_API_INPUT_SIZE = 128
    const val TF_OD_API_MODEL_FLOATING_FILE_FRONT_CAMERA = BuildConfig.ASSET_BLAZEFACE_FILE
    const val TF_OD_API_MODEL_FLOATING_FILE_BACK_CAMERA = "face_detection_blazeface_back.tflite"
    const val TF_OD_API_IS_QUANTIZED = false
}