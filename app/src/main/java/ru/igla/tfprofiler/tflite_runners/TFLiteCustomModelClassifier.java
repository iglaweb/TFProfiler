
package ru.igla.tfprofiler.tflite_runners;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.igla.tfprofiler.utils.TFInterpreterWrapper;

/**
 * Wrapper for frozen detection models trained using the Tensorflow Object Detection API:
 * - https://github.com/tensorflow/models/tree/master/research/object_detection
 * where you can find the training code.
 * <p>
 * To use pretrained models in the API or convert to TF Lite models, please see docs for details:
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/detection_model_zoo.md
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/running_on_mobile_tensorflowlite.md#running-our-model-on-android
 */
public class TFLiteCustomModelClassifier extends TFLiteObjectDetectionAPIModelBase<Classifier.Recognition> {

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
                    interpreter.getInterpreter().getOutputTensor(probabilityTensorIndex).shape(); // {1, NUM_CLASSES}
            // Creates the output tensor and its processor.
            outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);
        }

        Map<Integer, Object> outputs = new HashMap<>();
        outputs.put(0, outputProbabilityBuffer.getBuffer().rewind());
        return outputs;
    }

    @Override
    public List<Recognition> getDetections() {
        return Collections.emptyList();
    }
}