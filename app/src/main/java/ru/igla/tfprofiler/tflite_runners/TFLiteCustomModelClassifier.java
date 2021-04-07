
package ru.igla.tfprofiler.tflite_runners;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.igla.tfprofiler.core.tflite.TFInterpreterWrapper;
import ru.igla.tfprofiler.tflite_runners.base.ImageBatchProcessing;
import ru.igla.tfprofiler.tflite_runners.base.TFLiteObjectDetectionAPIModelBase;


public class TFLiteCustomModelClassifier extends TFLiteObjectDetectionAPIModelBase<ImageBatchProcessing.ImageResult> {

    /**
     * Output probability TensorBuffer.
     */
    private TensorBuffer outputProbabilityBuffer;

    public TFLiteCustomModelClassifier() {
    }

    @Override
    public Map<Integer, Object> prepareOutputImage() {
        TFInterpreterWrapper interpreter = tfLiteExecutor.getTfLite();
        if (interpreter == null) {
            throw new RuntimeException("Interpreter is not configured");
        }

        int probabilityTensorIndex = 0;
        DataType probabilityDataType = interpreter.getInterpreter().getOutputTensor(probabilityTensorIndex).dataType();

        if (outputProbabilityBuffer == null) {
            int[] probabilityShape =
                    interpreter.getInterpreter().getOutputTensor(probabilityTensorIndex).shape();
            // Creates the output tensor and its processor.
            outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);
        }

        Map<Integer, Object> outputs = new HashMap<>();
        outputs.put(0, outputProbabilityBuffer.getBuffer().rewind());
        return outputs;
    }

    @Override
    public List<ImageBatchProcessing.ImageResult> getDetections() {
        return Collections.emptyList();
    }
}