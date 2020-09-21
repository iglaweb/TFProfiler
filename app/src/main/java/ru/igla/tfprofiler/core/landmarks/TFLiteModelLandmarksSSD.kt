package ru.igla.tfprofiler.core.landmarks

import ru.igla.tfprofiler.BuildConfig

/***
 * https://raw.githubusercontent.com/google/mediapipe/master/mediapipe/models/face_landmark.tflite
 */
object TFLiteModelLandmarksSSD {
    const val TF_OD_API_INPUT_SIZE = 192
    const val TF_OD_API_MODEL_FILE = BuildConfig.ASSET_LANDMARK_FILE
    const val TF_OD_API_IS_QUANTIZED = false
}