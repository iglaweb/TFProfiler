package ru.igla.tfprofiler.tflite_runners;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * https://tfhub.dev/google/object_detection/mobile_object_localizer_v1/1
 * <p>
 * This model uses a MobileNet V2 backbone with a 0.75 width-multiplier, and a SSDLite detection head.
 * <p>
 * Use case:
 * This model is suitable for localizing the most prominent objects in an image.
 * <p>
 * Limitations:
 * This is a class-agnostic object detector. Object classification is not provided.
 * This model may not perform well on very small objects.
 */
public class TFLiteObjectDetectionAPIModel_COCOMobileNetV2 extends TFLiteObjectDetectionAPIModelBase<Classifier.Recognition> {

    // Only return this many results.
    private static final int NUM_DETECTIONS = 100;

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

    public TFLiteObjectDetectionAPIModel_COCOMobileNetV2() {
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
    public List<Recognition> getDetections() {
        // Show the best detections.
        // after scaling them back to the input size.

        // You need to use the number of detections from the output and not the NUM_DETECTONS variable declared on top
        // because on some models, they don't always output the same total number of detections
        // For example, your model's NUM_DETECTIONS = 20, but sometimes it only outputs 16 predictions
        // If you don't use the output's numDetections, you'll get nonsensical data
        int numDetectionsOutput = Math.min(NUM_DETECTIONS, (int) numDetections[0]); // cast from float to integer, use min for safety

        final List<Recognition> recognitions = new ArrayList<>(numDetectionsOutput);
        for (int i = 0; i < numDetectionsOutput; ++i) {
            final RectF detection =
                    new RectF(
                            outputLocations[0][i][1] * inputSize,
                            outputLocations[0][i][0] * inputSize,
                            outputLocations[0][i][3] * inputSize,
                            outputLocations[0][i][2] * inputSize);
            // SSD Mobilenet V1 Model assumes class 0 is background class
            // in label file and class labels start from 1 to number_of_classes+1,
            // while outputClasses correspond to class index from 0 to number_of_classes
            recognitions.add(
                    new Recognition(
                            "" + i,
                            "Entity",
                            outputScores[0][i],
                            detection));
        }
        return recognitions;
    }
}
