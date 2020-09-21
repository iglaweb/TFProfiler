package ru.igla.tfprofiler.tflite_runners

import ru.igla.tfprofiler.BuildConfig

/***
 * https://tfhub.dev/google/lite-model/object_detection/mobile_object_localizer_v1/1/default/1
 */
class CoCoMobileNetV2QuantConst {
    companion object {
        const val TF_OD_API_IMAGE_SIZE = 192
        const val TF_OD_API_IS_QUANTIZED = true
        const val TF_OD_API_MODEL_FILE = BuildConfig.ASSET_MOBILENETV2_FILE
        const val TF_OD_API_LABELS_FILE = ""
    }
}