package ru.igla.tfprofiler.tflite_runners

import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import ru.igla.tfprofiler.tflite_runners.base.TFLiteImageDetectAPIModelBase
import ru.igla.tfprofiler.tflite_runners.domain.ImageResult
import java.util.*

class TFLiteImageCustomModelClassifier :
    TFLiteImageDetectAPIModelBase<ImageResult>() {
    /**
     * Output probability TensorBuffer.
     */
    private var outputProbabilityBuffer: TensorBuffer? = null

    override fun prepareOutputs(): MutableMap<Int, Any> {
        if (outputProbabilityBuffer == null) {
            outputProbabilityBuffer = createOutputProbabilityBuffer()
        }
        val opb = requireNotNull(outputProbabilityBuffer)
        val outputs: MutableMap<Int, Any> = HashMap()
        outputs[0] = opb.buffer.rewind()
        return outputs
    }
}