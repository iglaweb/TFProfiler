package ru.igla.tfprofiler.tflite_runners;

import android.graphics.RectF;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.igla.tfprofiler.core.ColorSpace;
import ru.igla.tfprofiler.core.ops.BaseOpNormalizer;
import ru.igla.tfprofiler.core.ops.OpNormalizer;
import ru.igla.tfprofiler.core.tflite.TensorFlowUtils;
import ru.igla.tfprofiler.tflite_runners.base.TFLiteImageDetectAPIModelBase;
import ru.igla.tfprofiler.tflite_runners.blazeface.ssd.Keypoint;
import ru.igla.tfprofiler.tflite_runners.domain.ImRecognition;
import ru.igla.tfprofiler.tflite_runners.domain.ImageResult;

/***
 * https://google.github.io/mediapipe/solutions/face_mesh.html
 * Model: https://github.com/google/mediapipe/blob/master/mediapipe/modules/face_detection/face_detection_front.tflite
 */
public class TFLiteAPIModelFacemeshLandmarks extends
        TFLiteImageDetectAPIModelBase<ImageResult> {

    private static final float THRESHOLD_DETECT = 0.2f;

    private static final int LANDMARKS_LEN = 468 * 3; //1404

    private float[][][][] landmarkPoints;
    private float[][][][] result;

    @NotNull
    @Override
    public Map<Integer, Object> prepareOutputs() {
        final int batchImageCount = modelOptions.getNumberOfInputImages();
        this.landmarkPoints = new float[batchImageCount][1][1][LANDMARKS_LEN];
        this.result = new float[batchImageCount][1][1][1];
        HashMap<Integer, Object> outputs = new HashMap<>();
        outputs.put(0, landmarkPoints);
        outputs.put(1, result);
        return outputs;
    }

    @Override
    public OpNormalizer getNormalizer(boolean isQuantized, @NotNull ColorSpace colorSpace) {
        return new BaseOpNormalizer(isQuantized, 127.5f, 127.5f);
    }

    private List<ImRecognition> extractDetections(float[][][] result, float[][][] landmarkPoints) {
        final List<ImRecognition> detections = new ArrayList<>();

        float score = result[0][0][0];
        float classScore = TensorFlowUtils.sigmoid(score);

        if (classScore >= THRESHOLD_DETECT) {
            List<Keypoint> output = new ArrayList<>();
            for (int i = 0; i < LANDMARKS_LEN - 3; i += 3) {
                float x = landmarkPoints[0][0][i];
                float y = landmarkPoints[0][0][i + 1];
                output.add(new Keypoint(
                        x,
                        y
                ));
            }
            ImRecognition rec = new ImRecognition(
                    "1",
                    "Landmarks",
                    classScore,
                    new RectF(
                            0f,
                            0f,
                            inputSize.getWidth() - 1f,
                            inputSize.getHeight() - 1f
                    ),
                    output
            );
            detections.add(rec);
        }
        return detections;
    }

    @NotNull
    @Override
    public List<ImageResult> getDetections(@NotNull Map<Integer, ?> outputMap) {
        final int batchImageCount = modelOptions.getNumberOfInputImages();
        if (batchImageCount == 1) {
            List<ImRecognition> detections = extractDetections(result[0], landmarkPoints[0]);
            return Collections.singletonList(new ImageResult(detections));
        }

        List<ImageResult> imageResults = new ArrayList<>();
        int len = result.length;
        for (int i = 0; i < len; i++) {
            List<ImRecognition> detections = extractDetections(result[i], landmarkPoints[i]);
            imageResults.add(new ImageResult(detections));
        }
        return imageResults;
    }
}
