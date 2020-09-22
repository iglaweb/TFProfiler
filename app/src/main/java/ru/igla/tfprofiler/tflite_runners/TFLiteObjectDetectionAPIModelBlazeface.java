package ru.igla.tfprofiler.tflite_runners;

import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.igla.tfprofiler.core.blazeface.ssd.Detection;
import ru.igla.tfprofiler.core.blazeface.ssd.Keypoint;
import ru.igla.tfprofiler.core.blazeface.ssd.SingleShotMultiBoxDetector;

/**
 * Wrapper for frozen detection models trained using the Tensorflow Object Detection API:
 * - https://github.com/tensorflow/models/tree/master/research/object_detection
 * where you can find the training code.
 * <p>
 * To use pretrained models in the API or convert to TF Lite models, please see docs for details:
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/detection_model_zoo.md
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/running_on_mobile_tensorflowlite.md#running-our-model-on-android
 */
public class TFLiteObjectDetectionAPIModelBlazeface extends TFLiteObjectDetectionAPIModelBase<Classifier.Recognition> {

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
    public Map<Integer, Object> prepareOutputImage() {
        this.boxesResult = new float[1][896][16];
        this.scoresResult = new float[1][896][1];
        HashMap<Integer, Object> outputs = new HashMap<>();
        outputs.put(0, boxesResult);
        outputs.put(1, scoresResult);
        return outputs;
    }

    @Override
    public List<Recognition> getDetections() {
        // Calculate detections from model results
        List<Detection> detectionList = ssd.process(boxesResult, scoresResult);
        if (detectionList.isEmpty()) {
            Log.i(TAG, "No detections");
        } else {
            Log.i(TAG, "Detections: " + detectionList.size() + " " + detectionList.get(0).score);
        }

        final List<Recognition> detections = new ArrayList<>();
        for (Detection detection : detectionList) {
            if (detection.score > THRESHOLD_DETECT) {
                float x = detection.xMin * inputSize;
                float y = detection.yMin * inputSize;
                float width = detection.width * inputSize;
                float height = detection.height * inputSize;

                List<Keypoint> keypoints = detection.keypoints;
                List<Keypoint> output = new ArrayList<>();
                for (Keypoint keypoint : keypoints) {
                    output.add(new Keypoint(
                            keypoint.x * inputSize,
                            keypoint.y * inputSize
                    ));
                }

                Recognition rec = new Recognition(
                        "1",
                        "Face",
                        detection.score,
                        new RectF(
                                x,
                                y,
                                Math.min(x + width - 1, inputSize - 1),
                                Math.min(y + height - 1, inputSize - 1)
                        ),
                        output
                );
                detections.add(rec);
            }
        }
        return detections;
    }
}
