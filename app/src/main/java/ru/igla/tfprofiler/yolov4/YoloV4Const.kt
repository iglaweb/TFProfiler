package ru.igla.tfprofiler.yolov4

import ru.igla.tfprofiler.BuildConfig

/***
 * https://github.com/hunglc007/tensorflow-yolov4-tflite
 * https://github.com/hunglc007/tensorflow-yolov4-tflite/blob/master/android/app/src/main/assets/yolov4-416-fp32.tflite
 * Direct url https://raw.githubusercontent.com/hunglc007/tensorflow-yolov4-tflite/master/android/app/src/main/assets/yolov4-416-fp32.tflite
 */
class YoloV4Const {
    companion object {
        const val TF_OD_API_INPUT_SIZE = 416
        const val TF_OD_API_IS_QUANTIZED = false
        const val TF_OD_API_MODEL_FILE = BuildConfig.ASSET_YOLOV4_FILE
        const val TF_OD_API_LABELS_FILE = "file:///android_asset/yolov4_coco.txt"
    }
}