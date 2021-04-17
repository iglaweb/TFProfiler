package ru.igla.tfprofiler.tflite_runners

import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import ru.igla.tfprofiler.tflite_runners.base.ImageBatchProcessing.ImageResult
import ru.igla.tfprofiler.tflite_runners.base.TFLiteObjectDetectionAPIModelBase
import java.util.*

class TFLiteCustomModelClassifier : TFLiteObjectDetectionAPIModelBase<ImageResult>() {
    /**
     * Output probability TensorBuffer.
     */
    private var outputProbabilityBuffer: TensorBuffer? = null

    override fun prepareOutputImage(): Map<Int, Any> {
        val interpreter = tfLiteExecutor.tfLite
            ?: throw IllegalStateException("Interpreter is not configured")

        val probabilityTensorIndex = 0
        val probabilityDataType =
            interpreter.interpreter.getOutputTensor(probabilityTensorIndex).dataType()
        if (outputProbabilityBuffer == null) {
            val probabilityShape =
                interpreter.interpreter.getOutputTensor(probabilityTensorIndex).shape()
            // Creates the output tensor and its processor.
            outputProbabilityBuffer =
                TensorBuffer.createFixedSize(probabilityShape, probabilityDataType)
        }

        val outputs: MutableMap<Int, Any> = HashMap()
        outputs[0] = outputProbabilityBuffer!!.buffer.rewind()
        return outputs
    }

    override fun getDetections(): List<ImageResult> {
        return emptyList()
    }
}