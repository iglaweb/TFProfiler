
package ru.igla.tfprofiler.tflite_runners;

import android.graphics.RectF;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import ru.igla.tfprofiler.core.domain.ColorSpace;
import ru.igla.tfprofiler.core.ops.BaseOpNormalizer;
import ru.igla.tfprofiler.core.ops.OpNormalizer;
import ru.igla.tfprofiler.tflite_runners.base.TFLiteImageDetectAPIModelBase;
import ru.igla.tfprofiler.tflite_runners.domain.ImRecognition;
import ru.igla.tfprofiler.tflite_runners.domain.ImageResult;
import ru.igla.tfprofiler.tflite_runners.domain.Label;

/**
 * Wrapper for frozen detection models trained using the Tensorflow Object Detection API:
 * - https://github.com/tensorflow/models/tree/master/research/object_detection
 * where you can find the training code.
 * <p>
 * To use pretrained models in the API or convert to TF Lite models, please see docs for details:
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/detection_model_zoo.md
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/running_on_mobile_tensorflowlite.md#running-our-model-on-android
 */
public final class TFLiteObjectDetectionAPIYoloV4Classifier extends
        TFLiteImageDetectAPIModelBase<ImageResult> {

    private static final float THRESHOLD_DETECT = 0.1f;

    private static final float IMAGE_STD = 255.0f;

    // tiny or not
    private static final boolean IS_TINY = true;

    protected float mNmsThresh = 0.6f;

    // config yolov4 tiny
    private static final int[] OUTPUT_WIDTH_TINY = new int[]{2535, 2535};
    private static final int[] OUTPUT_WIDTH_FULL = new int[]{10647, 10647};

    private float[][][] bboxes;
    private float[][][] outScore;

    //1.find max confidence per class
    private final PriorityQueue<ImRecognition> pq =
            new PriorityQueue<>(
                    50,
                    (lhs, rhs) -> {
                        // Intentionally reversed to put high confidence at the head of the queue.
                        return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                    });

    @Override
    public Map<Integer, Object> prepareOutputs() {
        final int batchImageCount = modelOptions.getNumberOfInputImages();
        if (IS_TINY) {
            bboxes = new float[1][OUTPUT_WIDTH_TINY[0] * batchImageCount][4];
            outScore = new float[1][OUTPUT_WIDTH_TINY[1] * batchImageCount][labels.size()];
        } else {
            bboxes = new float[1][OUTPUT_WIDTH_FULL[0] * batchImageCount][4];
            outScore = new float[1][OUTPUT_WIDTH_FULL[1] * batchImageCount][labels.size()];
        }

        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, bboxes);
        outputMap.put(1, outScore);
        return outputMap;
    }

    //non maximum suppression
    protected List<ImRecognition> nms(List<ImRecognition> list) {
        List<ImRecognition> nmsList = new ArrayList<>();

        for (int k = 0; k < labels.size(); k++) {
            //1.find max confidence per class
            pq.clear();

            for (int i = 0; i < list.size(); ++i) {
                if (list.get(i).getLabel().getClassId() == k) {
                    pq.add(list.get(i));
                }
            }

            //2.do non maximum suppression
            while (!pq.isEmpty()) {
                //insert detection with max confidence
                ImRecognition[] a = new ImRecognition[pq.size()];
                ImRecognition[] detections = pq.toArray(a);
                ImRecognition max = detections[0];
                nmsList.add(max);
                pq.clear();

                for (int j = 1; j < detections.length; j++) {
                    ImRecognition detection = detections[j];
                    RectF b = detection.getLocation();
                    if (box_iou(max.getLocation(), b) < mNmsThresh) {
                        pq.add(detection);
                    }
                }
            }
        }
        return nmsList;
    }

    protected float box_iou(RectF a, RectF b) {
        return box_intersection(a, b) / box_union(a, b);
    }

    protected float box_intersection(RectF a, RectF b) {
        float w = overlap((a.left + a.right) / 2, a.right - a.left,
                (b.left + b.right) / 2, b.right - b.left);
        float h = overlap((a.top + a.bottom) / 2, a.bottom - a.top,
                (b.top + b.bottom) / 2, b.bottom - b.top);
        if (w < 0 || h < 0) return 0;
        float area = w * h;
        return area;
    }

    protected float box_union(RectF a, RectF b) {
        float i = box_intersection(a, b);
        float u = (a.right - a.left) * (a.bottom - a.top) + (b.right - b.left) * (b.bottom - b.top) - i;
        return u;
    }

    protected float overlap(float x1, float w1, float x2, float w2) {
        float l1 = x1 - w1 / 2;
        float l2 = x2 - w2 / 2;
        float left = l1 > l2 ? l1 : l2;
        float r1 = x1 + w1 / 2;
        float r2 = x2 + w2 / 2;
        float right = r1 < r2 ? r1 : r2;
        return right - left;
    }


    @Override
    public OpNormalizer getNormalizer(boolean isQuantized, @NotNull ColorSpace colorSpace) {
        return new BaseOpNormalizer(ColorSpace.COLOR, isQuantized, 0, IMAGE_STD);
    }

    /**
     * For yolov4-tiny, the situation would be a little different from the yolov4, it only has two
     * output. Both has three dimenstion. The first one is a tensor with dimension [1, 2535,4], containing all the bounding boxes.
     * The second one is a tensor with dimension [1, 2535, class_num], containing all the classes score.
     *
     * @param bboxes
     * @param outScore
     * @return an array list containing the recognitions
     */
    private List<ImRecognition> getDetectionsForFull(float[][][] bboxes, float[][][] outScore) {
        List<ImRecognition> detections = new ArrayList<>();

        int gridWidth = OUTPUT_WIDTH_FULL[0];
        for (int i = 0; i < gridWidth; i++) {
            float maxClass = 0;
            int detectedClass = -1;
            final float[] classes = new float[labels.size()];
            for (int c = 0; c < labels.size(); c++) {
                classes[c] = outScore[0][i][c];
            }
            for (int c = 0; c < labels.size(); ++c) {
                if (classes[c] > maxClass) {
                    detectedClass = c;
                    maxClass = classes[c];
                }
            }
            final float score = maxClass;
            if (score > THRESHOLD_DETECT) {
                final float xPos = bboxes[0][i][0];
                final float yPos = bboxes[0][i][1];
                final float w = bboxes[0][i][2];
                final float h = bboxes[0][i][3];
                final RectF rectF = new RectF(
                        Math.max(0, xPos - w / 2),
                        Math.max(0, yPos - h / 2),
                        Math.min(inputSize.getWidth() - 1, xPos + w / 2),
                        Math.min(inputSize.getHeight() - 1, yPos + h / 2));
                Label label = new Label(labels.get(detectedClass), detectedClass);
                detections.add(new ImRecognition("" + i, label, score, rectF));
            }
        }
        return detections;
    }

    private List<ImRecognition> getDetectionsForTiny(float[][][] bboxes, float[][][] outScore) {
        List<ImRecognition> detections = new ArrayList<>();

        int gridWidth = OUTPUT_WIDTH_TINY[0];
        for (int i = 0; i < gridWidth; i++) {
            float maxClass = 0;
            int detectedClass = -1;
            final float[] classes = new float[labels.size()];
            for (int c = 0; c < labels.size(); c++) {
                classes[c] = outScore[0][i][c];
            }
            for (int c = 0; c < labels.size(); ++c) {
                if (classes[c] > maxClass) {
                    detectedClass = c;
                    maxClass = classes[c];
                }
            }
            final float score = maxClass;
            if (score > THRESHOLD_DETECT) {
                final float xPos = bboxes[0][i][0];
                final float yPos = bboxes[0][i][1];
                final float w = bboxes[0][i][2];
                final float h = bboxes[0][i][3];
                final RectF rectF = new RectF(
                        Math.max(0, xPos - w / 2),
                        Math.max(0, yPos - h / 2),
                        Math.min(inputSize.getWidth() - 1f, xPos + w / 2),
                        Math.min(inputSize.getHeight() - 1f, yPos + h / 2));
                Label label = new Label(labels.get(detectedClass), detectedClass);
                detections.add(new ImRecognition("" + i, label, score, rectF));
            }
        }
        return detections;
    }

    private List<ImRecognition> extractDetections(float[][][] bboxes, float[][][] outScore) {
        List<ImRecognition> detections;
        if (IS_TINY) {
            detections = getDetectionsForTiny(bboxes, outScore);
        } else {
            detections = getDetectionsForFull(bboxes, outScore);
        }
        return nms(detections);
    }

    @Override
    public List<ImageResult> getDetections(@NotNull Map<Integer, ?> outputMap) {
        final int batchImageCount = modelOptions.getNumberOfInputImages();
        if (batchImageCount == 1) {
            List<ImRecognition> detections = extractDetections(bboxes, outScore);
            return Collections.singletonList(new ImageResult(detections));
        }

        float[][] arr = bboxes[0];
        final int batchCount = arr.length / batchImageCount;
        int len = arr.length;
        List<ImageResult> imageResults = new ArrayList<>();
        for (int i = 0; i < len - batchCount + 1; i += batchCount) {
            float[][] boxArr = bboxes[0];
            float[][] box = Arrays.copyOfRange(boxArr, i, i + batchCount);
            float[][][] scaledBoxes = {box};

            float[][] scoreArr = outScore[0];
            float[][] score = Arrays.copyOfRange(scoreArr, i, i + batchCount);
            float[][][] scaledScores = {score};

            List<ImRecognition> detections = extractDetections(scaledBoxes, scaledScores);
            imageResults.add(new ImageResult(detections));
        }
        return imageResults;
    }
}