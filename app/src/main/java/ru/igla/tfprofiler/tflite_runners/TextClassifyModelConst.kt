package ru.igla.tfprofiler.tflite_runners

import ru.igla.tfprofiler.BuildConfig
import ru.igla.tfprofiler.core.ModelOptimizedType
import ru.igla.tfprofiler.core.TextConfig

/***
 * https://github.com/hunglc007/tensorflow-yolov4-tflite
 * https://github.com/hunglc007/tensorflow-yolov4-tflite/blob/master/android/app/src/main/assets/yolov4-416-fp32.tflite
 * Direct url https://raw.githubusercontent.com/hunglc007/tensorflow-yolov4-tflite/master/android/app/src/main/assets/yolov4-416-fp32.tflite
 */
object TextClassifyModelConst : TextConfig() {
    private val TF_OD_API_MODEL_FORMAT = ModelOptimizedType.FLOATING

    override val details: String
        get() = "N/A"

    override val modelFormat: ModelOptimizedType
        get() = TF_OD_API_MODEL_FORMAT
    override val modelFile: String
        get() = BuildConfig.ASSET_TEXT_CLASSIFY_FILE
    override val labelFile: String
        get() = ""
    override val source: String
        get() = ""
}