package ru.igla.tfprofiler.tflite_runners.base

import android.content.Context
import android.os.Trace
import androidx.annotation.CallSuper
import org.checkerframework.checker.nullness.qual.NonNull
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import ru.igla.tfprofiler.core.ModelOptimizedType
import ru.igla.tfprofiler.core.ModelType
import ru.igla.tfprofiler.core.tflite.FailedCreateTFDelegate
import ru.igla.tfprofiler.core.tflite.TFInterpeterThreadExecutor
import ru.igla.tfprofiler.core.tflite.TensorFlowUtils
import ru.igla.tfprofiler.models_list.ModelEntity
import ru.igla.tfprofiler.utils.StringUtils
import java.io.Closeable
import java.util.*

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


abstract class TFLiteAPIModelBase<Input, Output> :
    Closeable {

    abstract fun preprocess(data: Input): Array<Any>
    abstract fun prepareOutputs(): MutableMap<Int, Any>
    abstract fun getDetections(outputMap: Map<Int, Any>): Output

    @JvmField
    protected var isModelQuantized: Boolean = false

    private var tfLiteExecutor: TFInterpeterThreadExecutor? = null

    @JvmField
    protected var labels: List<String> = ArrayList()

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param context     The asset manager to be used to load assets.
     * @param modelEntity The filepath of the model GraphDef protocol buffer.
     */
    @Throws(Exception::class)
    @CallSuper
    open fun init(
        context: Context,
        modelEntity: ModelEntity,
        modelOptions: ModelOptions
    ) {
        val modelFilename = modelEntity.modelFile
        tfLiteExecutor = TFInterpeterThreadExecutor(context, modelFilename).apply {
            init(modelEntity, modelOptions)
        }

        isModelQuantized = if (modelEntity.modelType === ModelType.CUSTOM_TFLITE) {
            val interpreter = tfLiteExecutor!!.tfLite
                ?: throw FailedCreateTFDelegate(
                    modelOptions.device,
                    "Interpreter is not configured"
                )
            val probabilityTensorIndex = 0
            // Creates the output tensor and its processor.
            val probabilityDataType =
                interpreter.interpreter.getOutputTensor(probabilityTensorIndex).dataType()
            probabilityDataType == DataType.UINT8
        } else {
            modelEntity.modelConfig.modelFormat === ModelOptimizedType.QUANTIZED
        }

        val labelFilename = modelEntity.labelFile
        if (!StringUtils.isNullOrEmpty(labelFilename)) {
            val actualFilename = labelFilename.split("file:///android_asset/").toTypedArray()[1]
            labels = TensorFlowUtils.loadLabelList(context.assets, actualFilename)
        }
    }

    fun createOutputProbabilityBuffer(): TensorBuffer {
        val interpreter = tfLiteExecutor?.tfLite
            ?: throw IllegalStateException("TFLite Interpreter is not configured")

        val probabilityTensorIndex = 0
        val probabilityDataType =
            interpreter.interpreter.getOutputTensor(probabilityTensorIndex).dataType()
        val probabilityShape =
            interpreter.interpreter.getOutputTensor(probabilityTensorIndex).shape()
        // Creates the output tensor and its processor.
        return TensorBuffer.createFixedSize(probabilityShape, probabilityDataType)
    }

    fun runInference(data: Input): Output {
        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("recognize")

        Trace.beginSection("preprocess")
        val inputArray = preprocess(data)
        Trace.endSection() // preprocessBitmap

        // Copy the input data into TensorFlow.
        Trace.beginSection("feed")
        val outputMap = prepareOutputs()
        Trace.endSection()

        runInferenceCall(inputArray, outputMap)
        val recognitions = getDetections(outputMap)
        Trace.endSection() // "recognizeImage"
        return recognitions
    }

    override fun close() {
        tfLiteExecutor?.close()
        tfLiteExecutor = null
    }

    private fun runInferenceCall(inputArray: Array<Any>, outputMap: MutableMap<Int, Any>) {
        val executor = requireNotNull(tfLiteExecutor) {
            "TFLite executor is not initialized!"
        }
        // Run the inference call.
        Trace.beginSection("run")
        executor.runForMultipleInputsOutputs(inputArray, outputMap)
        Trace.endSection()
    }
}