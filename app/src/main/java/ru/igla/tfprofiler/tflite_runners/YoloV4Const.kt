package ru.igla.tfprofiler.tflite_runners

import ru.igla.tfprofiler.BuildConfig
import ru.igla.tfprofiler.core.BuiltModel
import ru.igla.tfprofiler.core.ColorSpace

/***
 * https://github.com/hunglc007/tensorflow-yolov4-tflite
 * https://github.com/hunglc007/tensorflow-yolov4-tflite/blob/master/android/app/src/main/assets/yolov4-416-fp32.tflite
 * Direct url https://raw.githubusercontent.com/hunglc007/tensorflow-yolov4-tflite/master/android/app/src/main/assets/yolov4-416-fp32.tflite
 */
object YoloV4Const : BuiltModel {
    private const val TF_OD_API_INPUT_SIZE = 416
    private const val TF_OD_API_IS_QUANTIZED = false
    override val details: String
        get() = "CSP connections with the Darknet-53"

    override val imageHeight: Int
        get() = TF_OD_API_INPUT_SIZE
    override val imageWidth: Int
        get() = TF_OD_API_INPUT_SIZE
    override val quantized: Boolean
        get() = TF_OD_API_IS_QUANTIZED
    override val colorSpace: ColorSpace
        get() = ColorSpace.COLOR
    override val modelFile: String
        get() = BuildConfig.ASSET_YOLOV4_FILE
    override val labelFile: String
        get() = "file:///android_asset/yolov4_coco.txt"
    override val source: String
        get() = "https://arxiv.org/abs/2004.10934"
}