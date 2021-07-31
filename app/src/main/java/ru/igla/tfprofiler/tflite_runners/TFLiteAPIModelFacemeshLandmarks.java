package ru.igla.tfprofiler.tflite_runners;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.igla.tfprofiler.core.ColorSpace;
import ru.igla.tfprofiler.core.ops.BaseOpNormalizer;
import ru.igla.tfprofiler.core.ops.OpNormalizer;
import ru.igla.tfprofiler.core.tflite.TensorFlowUtils;
import ru.igla.tfprofiler.tflite_runners.base.ImageBatchProcessing;
import ru.igla.tfprofiler.tflite_runners.base.TFLiteObjectDetectionAPIModelBase;
import ru.igla.tfprofiler.tflite_runners.blazeface.ssd.Keypoint;
import ru.igla.tfprofiler.tflite_runners.domain.Recognition;

/***
 * https://google.github.io/mediapipe/solutions/face_mesh.html
 * Model: https://github.com/google/mediapipe/blob/master/mediapipe/modules/face_detection/face_detection_front.tflite
 */
public class TFLiteAPIModelFacemeshLandmarks extends TFLiteObjectDetectionAPIModelBase<ImageBatchProcessing.ImageResult> {

    private static final float THRESHOLD_DETECT = 0.2f;

    private static final int LANDMARKS_LEN = 468 * 3; //1404

    private float[][][][] landmarkPoints;
    private float[][][][] result;

    public TFLiteAPIModelFacemeshLandmarks() {
        super();
    }

    @Override
    public Map<Integer, Object> prepareOutputImage() {
        final int batchImageCount = modelOptions.getNumberOfInputImages();
        this.landmarkPoints = new float[batchImageCount][1][1][LANDMARKS_LEN];
        this.result = new float[batchImageCount][1][1][1];
        HashMap<Integer, Object> outputs = new HashMap<>();
        outputs.put(0, landmarkPoints);
        outputs.put(1, result);
        return outputs;
    }

    @Override
    public OpNormalizer getNormalizer(boolean isQuantized, ColorSpace colorSpace) {
        return new BaseOpNormalizer(isQuantized, 127.5f, 127.5f);
    }

    private List<Recognition> extractDetections(float[][][] result, float[][][] landmarkPoints) {
        final List<Recognition> detections = new ArrayList<>();

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
            Recognition rec = new Recognition(
                    "1",
                    "Landmarks",
                    classScore,
                    new RectF(
                            0f,
                            0f,
                            mInputSize.getWidth() - 1f,
                            mInputSize.getHeight() - 1f
                    ),
                    output
            );
            detections.add(rec);
        }
        return detections;
    }

    @Override
    public List<ImageBatchProcessing.ImageResult> getDetections() {
        final int batchImageCount = modelOptions.getNumberOfInputImages();
        if (batchImageCount == 1) {
            List<Recognition> detections = extractDetections(result[0], landmarkPoints[0]);
            return Collections.singletonList(new ImageBatchProcessing.ImageResult(detections));
        }

        List<ImageBatchProcessing.ImageResult> imageResults = new ArrayList<>();
        int len = result.length;
        for (int i = 0; i < len; i++) {
            List<Recognition> detections = extractDetections(result[i], landmarkPoints[i]);
            imageResults.add(new ImageBatchProcessing.ImageResult(detections));
        }
        return imageResults;
    }
}
