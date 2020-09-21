package ru.igla.tfprofiler.tflite_runners

import ru.igla.tfprofiler.BuildConfig

class CoCoMobileNetV1QuantConst {
    companion object {
        const val TF_OD_API_IMAGE_SIZE = 300
        const val TF_OD_API_IS_QUANTIZED = true
        const val TF_OD_API_MODEL_FILE = BuildConfig.ASSET_MOBILENETV1_FILE + "detect.tflite"
        const val TF_OD_API_LABELS_FILE =
            "file:///android_asset/" + BuildConfig.ASSET_MOBILENETV1_FILE + "labelmap.txt"
    }
}