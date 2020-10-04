package ru.igla.tfprofiler.tflite_runners;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.igla.tfprofiler.core.ColorSpace;
import ru.igla.tfprofiler.core.ops.BaseOpNormalizer;
import ru.igla.tfprofiler.core.ops.OpNormalizer;
import ru.igla.tfprofiler.tflite_runners.blazeface.ssd.Keypoint;
import ru.igla.tfprofiler.core.tflite.TensorFlowUtils;
import ru.igla.tfprofiler.tflite_runners.base.Classifier;
import ru.igla.tfprofiler.tflite_runners.base.TFLiteObjectDetectionAPIModelBase;

/***
 * https://google.github.io/mediapipe/solutions/face_mesh.html
 * Model: https://github.com/google/mediapipe/blob/master/mediapipe/modules/face_detection/face_detection_front.tflite
 */
public class TFLiteAPIModelFacemeshLandmarks extends TFLiteObjectDetectionAPIModelBase<Classifier.Recognition> {

    private static final float THRESHOLD_DETECT = 0.2f;

    private static final int LANDMARKS_LEN = 468 * 3; //1404

    private float[][][][] landmarkPoints;
    private float[][][][] result;

    public TFLiteAPIModelFacemeshLandmarks() {
        super();
    }

    @Override
    public Map<Integer, Object> prepareOutputImage() {
        this.landmarkPoints = new float[1][1][1][LANDMARKS_LEN];
        this.result = new float[1][1][1][1];
        HashMap<Integer, Object> outputs = new HashMap<>();
        outputs.put(0, landmarkPoints);
        outputs.put(1, result);
        return outputs;
    }

    @Override
    public OpNormalizer getNormalizer(boolean isQuantized, ColorSpace colorSpace) {
        return new BaseOpNormalizer(isQuantized, 127.5f, 127.5f);
    }

    @Override
    public List<Recognition> getDetections() {
        final List<Recognition> detections = new ArrayList<>();

        float score = result[0][0][0][0];
        float classScore = TensorFlowUtils.sigmoid(score);

        if (classScore >= THRESHOLD_DETECT) {
            List<Keypoint> output = new ArrayList<>();
            for (int i = 0; i < LANDMARKS_LEN - 3; i += 3) {
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
                    classScore,
                    new RectF(
                            0f,
                            0f,
                            inputWidth - 1,
                            inputHeight - 1
                    ),
                    output
            );
            detections.add(rec);
        }
        return detections;
    }
}
