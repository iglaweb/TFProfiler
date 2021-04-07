package ru.igla.tfprofiler.tflite_runners;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.igla.tfprofiler.tflite_runners.base.ImageBatchProcessing;
import ru.igla.tfprofiler.tflite_runners.base.TFLiteObjectDetectionAPIModelBase;
import ru.igla.tfprofiler.tflite_runners.domain.Recognition;

/**
 * https://www.tensorflow.org/lite/guide/hosted_models
 * <p>
 * Wrapper for frozen detection models trained using the Tensorflow Object Detection API:
 * - https://github.com/tensorflow/models/tree/master/research/object_detection
 * where you can find the training code.
 * <p>
 * To use pretrained models in the API or convert to TF Lite models, please see docs for details:
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/detection_model_zoo.md
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/running_on_mobile_tensorflowlite.md#running-our-model-on-android
 */
public class TFLiteObjectDetectionAPIModel_COCOMobileNetV1 extends TFLiteObjectDetectionAPIModelBase<ImageBatchProcessing.ImageResult> {

    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;

    // Only return this many results.
    private static final int NUM_DETECTIONS = 10;

    // outputLocations: array of shape [Batchsize, NUM_DETECTIONS,4]
    // contains the location of detected boxes
    private float[][][] outputLocations;
    // outputClasses: array of shape [Batchsize, NUM_DETECTIONS]
    // contains the classes of detected boxes
    private float[][] outputClasses;
    // outputScores: array of shape [Batchsize, NUM_DETECTIONS]
    // contains the scores of detected boxes
    private float[][] outputScores;
    // numDetections: array of shape [Batchsize]
    // contains the number of detected boxes
    private float[] numDetections;

    public TFLiteObjectDetectionAPIModel_COCOMobileNetV1() {
        super();
    }

    @Override
    public Map<Integer, Object> prepareOutputImage() {
        outputLocations = new float[1][NUM_DETECTIONS][4];
        outputClasses = new float[1][NUM_DETECTIONS];
        outputScores = new float[1][NUM_DETECTIONS];
        numDetections = new float[1];

        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, outputLocations);
        outputMap.put(1, outputClasses);
        outputMap.put(2, outputScores);
        outputMap.put(3, numDetections);
        return outputMap;
    }

    @Override
    public List<ImageBatchProcessing.ImageResult> getDetections() {
        // Show the best detections.
        // after scaling them back to the input size.

        // You need to use the number of detections from the output and not the NUM_DETECTONS variable declared on top
        // because on some models, they don't always output the same total number of detections
        // For example, your model's NUM_DETECTIONS = 20, but sometimes it only outputs 16 predictions
        // If you don't use the output's numDetections, you'll get nonsensical data
        int numDetectionsOutput = Math.min(NUM_DETECTIONS, (int) numDetections[0]); // cast from float to integer, use min for safety

        final List<Recognition> recognitions = new ArrayList<>(numDetectionsOutput);
        for (int i = 0; i < numDetectionsOutput; ++i) {
            final float confidence = outputScores[0][i];
            if (confidence > MINIMUM_CONFIDENCE_TF_OD_API) {
                final RectF detection =
                        new RectF(
                                outputLocations[0][i][1] * inputWidth,
                                outputLocations[0][i][0] * inputHeight,
                                outputLocations[0][i][3] * inputWidth,
                                outputLocations[0][i][2] * inputHeight);
                // SSD Mobilenet V1 Model assumes class 0 is background class
                // in label file and class labels start from 1 to number_of_classes+1,
                // while outputClasses correspond to class index from 0 to number_of_classes
                int labelOffset = 1;
                recognitions.add(
                        new Recognition(
                                "" + i,
                                labels.get((int) outputClasses[0][i] + labelOffset),
                                outputScores[0][i],
                                detection));
            }
        }
        return List.of(new ImageBatchProcessing.ImageResult(recognitions));
    }
}
