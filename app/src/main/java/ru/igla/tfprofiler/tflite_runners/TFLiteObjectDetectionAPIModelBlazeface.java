package ru.igla.tfprofiler.tflite_runners;

import android.graphics.RectF;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.igla.tfprofiler.tflite_runners.base.TFLiteImageDetectAPIModelBase;
import ru.igla.tfprofiler.tflite_runners.blazeface.ssd.Detection;
import ru.igla.tfprofiler.tflite_runners.blazeface.ssd.Keypoint;
import ru.igla.tfprofiler.tflite_runners.blazeface.ssd.SingleShotMultiBoxDetector;
import ru.igla.tfprofiler.tflite_runners.domain.ImRecognition;
import ru.igla.tfprofiler.tflite_runners.domain.ImageResult;

/**
 * Wrapper for frozen detection models trained using the Tensorflow Object Detection API:
 * - https://github.com/tensorflow/models/tree/master/research/object_detection
 * where you can find the training code.
 * <p>
 * To use pretrained models in the API or convert to TF Lite models, please see docs for details:
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/detection_model_zoo.md
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/running_on_mobile_tensorflowlite.md#running-our-model-on-android
 */
public final class TFLiteObjectDetectionAPIModelBlazeface extends
        TFLiteImageDetectAPIModelBase<ImageResult> {

    // Minimum detection confidence to track a detection.
    private static final float THRESHOLD_DETECT = 0.1f;
    private float[][][] boxesResult;
    private float[][][] scoresResult;
    private final SingleShotMultiBoxDetector ssd = new SingleShotMultiBoxDetector();
    private static final String TAG = "tflite_blazeface";

    public TFLiteObjectDetectionAPIModelBlazeface() {
        super();
    }

    @Override
    public Map<Integer, Object> prepareOutputs() {
        int batchImageCount = modelOptions.getNumberOfInputImages();
        this.boxesResult = new float[1][896 * batchImageCount][16];
        this.scoresResult = new float[1][896 * batchImageCount][1];
        HashMap<Integer, Object> outputs = new HashMap<>();
        outputs.put(0, boxesResult);
        outputs.put(1, scoresResult);
        return outputs;
    }

    private List<ImRecognition> extractDetections(float[][][] boxesResult, float[][][] scoresResult) {
        // Calculate detections from model results
        List<Detection> detectionList = ssd.process(boxesResult, scoresResult);
        if (detectionList.isEmpty()) {
            Log.i(TAG, "No detections");
        } else {
            Log.i(TAG, "Detections: " + detectionList.size() + " " + detectionList.get(0).score);
        }

        final List<ImRecognition> detections = new ArrayList<>();
        for (Detection detection : detectionList) {
            if (detection.score > THRESHOLD_DETECT) {
                float x = detection.xMin * inputSize.getWidth();
                float y = detection.yMin * inputSize.getHeight();
                float width = detection.width * inputSize.getWidth();
                float height = detection.height * inputSize.getHeight();

                List<Keypoint> keypoints = detection.keypoints;
                List<Keypoint> output = new ArrayList<>();
                for (Keypoint keypoint : keypoints) {
                    output.add(new Keypoint(
                            keypoint.getX() * inputSize.getWidth(),
                            keypoint.getY() * inputSize.getHeight()
                    ));
                }

                ImRecognition rec = new ImRecognition(
                        "1",
                        "Face",
                        detection.score,
                        new RectF(
                                x,
                                y,
                                Math.min(x + width - 1f, inputSize.getWidth() - 1f),
                                Math.min(y + height - 1f, inputSize.getHeight() - 1f)
                        ),
                        output
                );
                detections.add(rec);
            }
        }
        return detections;
    }

    @Override
    public List<ImageResult> getDetections(@NotNull Map<Integer, ?> outputMap) {
        final int batchImageCount = modelOptions.getNumberOfInputImages();
        if (batchImageCount == 1) {
            List<ImRecognition> detections = extractDetections(boxesResult, scoresResult);
            return Collections.singletonList(new ImageResult(detections));
        }

        float[][] arr = boxesResult[0];
        final int batchCount = arr.length / batchImageCount;
        final int len = arr.length;
        List<ImageResult> imageResults = new ArrayList<>();
        for (int i = 0; i < len - batchCount + 1; i += batchCount) {

            float[][] boxArr = boxesResult[0];
            float[][] box = Arrays.copyOfRange(boxArr, i, i + batchCount);
            float[][][] scaledBoxes = {box};

            float[][] scoreArr = scoresResult[0];
            float[][] score = Arrays.copyOfRange(scoreArr, i, i + batchCount);
            float[][][] scaledScores = {score};

            List<ImRecognition> detections = extractDetections(scaledBoxes, scaledScores);
            imageResults.add(new ImageResult(detections));
        }
        return imageResults;
    }
}
