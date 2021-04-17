package ru.igla.tfprofiler.tflite_runners.base

import android.content.Context
import android.graphics.Bitmap
import android.os.Trace
import ru.igla.tfprofiler.core.tflite.OpenCVInterpeterThreadExecutor
import ru.igla.tfprofiler.models_list.ModelEntity
import ru.igla.tfprofiler.tflite_runners.base.ImageBatchProcessing.ImageResult

/**
 * Wrapper for frozen detection models trained using the Tensorflow Object Detection API:
 * - https://github.com/tensorflow/models/tree/master/research/object_detection
 * where you can find the training code.
 *
 *
 * To use pretrained models in the API or convert to TF Lite models, please see docs for details:
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/detection_model_zoo.md
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/running_on_mobile_tensorflowlite.md#running-our-model-on-android
 */
class OpenCVObjectDetectionAPIModelBase : Classifier<ImageResult> {
    private var opencvExecutor: OpenCVInterpeterThreadExecutor? = null

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param context     The asset manager to be used to load assets.
     * @param modelEntity The filepath of the model GraphDef protocol buffer.
     */
    @Throws(Exception::class)
    override fun init(context: Context, modelEntity: ModelEntity, modelOptions: ModelOptions) {
        val modelFilename = modelEntity.modelFile
        opencvExecutor = OpenCVInterpeterThreadExecutor(context, modelFilename).apply {
            init(modelEntity, modelOptions)
        }
    }

    override fun recognizeImage(bitmaps: List<Bitmap>): List<ImageResult> {
        Trace.beginSection("recognizeImage")
        for (bmp in bitmaps) {
            opencvExecutor?.runBitmapProcessing(bmp)
        }
        Trace.endSection() // "recognizeImage"
        return emptyList()
    }

    override fun close() {
        opencvExecutor?.close()
        opencvExecutor = null
    }
}