package ru.igla.tfprofiler.tflite_runners;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.igla.tfprofiler.core.blazeface.ssd.Keypoint;

/***
 * https://google.github.io/mediapipe/solutions/face_mesh.html
 * Model: https://github.com/google/mediapipe/blob/master/mediapipe/modules/face_detection/face_detection_front.tflite
 */
public class TFLiteAPIModelFacemeshLandmarks extends TFLiteObjectDetectionAPIModelBase<Classifier.Recognition> {

    private float[][][][] landmarkPoints;
    private float[][][][] result;

    public TFLiteAPIModelFacemeshLandmarks() {
        super();
    }

    @Override
    public Map<Integer, Object> prepareOutputImage() {
        this.landmarkPoints = new float[1][1][1][1404];
        this.result = new float[1][1][1][1];
        HashMap<Integer, Object> outputs = new HashMap<>();
        outputs.put(0, landmarkPoints);
        outputs.put(1, result);
        return outputs;
    }

    @Override
    public List<Recognition> getDetections() {
        final List<Recognition> detections = new ArrayList<>();
        List<Keypoint> output = new ArrayList<>();
        for (int i = 0; i < 1404 - 3; i += 3) {
            float x = landmarkPoints[0][0][0][i];
            float y = landmarkPoints[0][0][0][i + 1];
            output.add(new Keypoint(
                    x,
                    y
            ));
        }
        Recognition rec = new Recognition(
                "1",
                "Landmarks",
                1f,
                new RectF(
                        0f,
                        0f,
                        inputSize - 1,
                        inputSize - 1
                ),
                output
        );
        detections.add(rec);
        return detections;
    }
}
