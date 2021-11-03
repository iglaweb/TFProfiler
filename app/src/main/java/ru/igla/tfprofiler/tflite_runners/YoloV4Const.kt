package ru.igla.tfprofiler.tflite_runners

import ru.igla.tfprofiler.BuildConfig
import ru.igla.tfprofiler.core.domain.ColorSpace
import ru.igla.tfprofiler.core.domain.ImageConfig
import ru.igla.tfprofiler.core.domain.InputShapeType
import ru.igla.tfprofiler.core.domain.ModelOptimizedType

/***
 * https://github.com/hunglc007/tensorflow-yolov4-tflite
 * https://github.com/hunglc007/tensorflow-yolov4-tflite/blob/master/android/app/src/main/assets/yolov4-416-fp32.tflite
 * Direct url https://raw.githubusercontent.com/hunglc007/tensorflow-yolov4-tflite/master/android/app/src/main/assets/yolov4-416-fp32.tflite
 */
object YoloV4Const : ImageConfig() {
    private const val TF_OD_API_INPUT_SIZE = 416
    private val TF_OD_API_MODEL_FORMAT = ModelOptimizedType.FLOATING

    override val details: String
        get() = "CSP connections with the Darknet-53"

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
        get() = BuildConfig.ASSET_YOLOV4_FILE
    override val labelFile: String
        get() = "file:///android_asset/yolov4_coco.txt"
    override val source: String
        get() = "https://arxiv.org/abs/2004.10934"
}