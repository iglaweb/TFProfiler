
package ru.igla.tfprofiler.tflite_runners;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import ru.igla.tfprofiler.core.ColorSpace;
import ru.igla.tfprofiler.core.ops.BaseOpNormalizer;
import ru.igla.tfprofiler.core.ops.OpNormalizer;
import ru.igla.tfprofiler.tflite_runners.base.Classifier;
import ru.igla.tfprofiler.tflite_runners.base.TFLiteObjectDetectionAPIModelBase;

/**
 * Wrapper for frozen detection models trained using the Tensorflow Object Detection API:
 * - https://github.com/tensorflow/models/tree/master/research/object_detection
 * where you can find the training code.
 * <p>
 * To use pretrained models in the API or convert to TF Lite models, please see docs for details:
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/detection_model_zoo.md
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/running_on_mobile_tensorflowlite.md#running-our-model-on-android
 */
public class TFLiteObjectDetectionAPIYoloV4Classifier extends TFLiteObjectDetectionAPIModelBase<Classifier.Recognition> {

    private static final float THRESHOLD_DETECT = 0.1f;

    private static final float IMAGE_STD = 255.0f;

    // tiny or not
    private static final boolean IS_TINY = true;

    protected float mNmsThresh = 0.6f;

    // config yolov4 tiny
    private static final int[] OUTPUT_WIDTH_TINY = new int[]{2535, 2535};
    private static final int[] OUTPUT_WIDTH_FULL = new int[]{10647, 10647};

    private float[][][] bboxes;
    private float[][][] out_score;

    public TFLiteObjectDetectionAPIYoloV4Classifier() {
    }

    @Override
    public Map<Integer, Object> prepareOutputImage() {
        Map<Integer, Object> outputMap = new HashMap<>();

        if (IS_TINY) {
            bboxes = new float[1][OUTPUT_WIDTH_TINY[0]][4];
            out_score = new float[1][OUTPUT_WIDTH_TINY[1]][labels.size()];
        } else {
            bboxes = new float[1][OUTPUT_WIDTH_FULL[0]][4];
            out_score = new float[1][OUTPUT_WIDTH_FULL[1]][labels.size()];
        }

        outputMap.put(0, bboxes);
        outputMap.put(1, out_score);
        return outputMap;
    }

    //non maximum suppression
    protected ArrayList<Recognition> nms(List<Recognition> list) {
        ArrayList<Recognition> nmsList = new ArrayList<>();

        for (int k = 0; k < labels.size(); k++) {
            //1.find max confidence per class
            PriorityQueue<Recognition> pq =
                    new PriorityQueue<>(
                            50,
                            new Comparator<Recognition>() {
                                @Override
                                public int compare(final Recognition lhs, final Recognition rhs) {
                                    // Intentionally reversed to put high confidence at the head of the queue.
                                    return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                                }
                            });

            for (int i = 0; i < list.size(); ++i) {
                if (list.get(i).getLabel().getClassId() == k) {
                    pq.add(list.get(i));
                }
            }

            //2.do non maximum suppression
            while (pq.size() > 0) {
                //insert detection with max confidence
                Recognition[] a = new Recognition[pq.size()];
                Recognition[] detections = pq.toArray(a);
                Recognition max = detections[0];
                nmsList.add(max);
                pq.clear();

                for (int j = 1; j < detections.length; j++) {
                    Recognition detection = detections[j];
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
    public OpNormalizer getNormalizer(boolean isQuantized, ColorSpace colorSpace) {
        return new BaseOpNormalizer(isQuantized, 0, IMAGE_STD);
    }

    /**
     * For yolov4-tiny, the situation would be a little different from the yolov4, it only has two
     * output. Both has three dimenstion. The first one is a tensor with dimension [1, 2535,4], containing all the bounding boxes.
     * The second one is a tensor with dimension [1, 2535, class_num], containing all the classes score.
     *
     * @return an array list containing the recognitions
     */
    private ArrayList<Recognition> getDetectionsForFull() {
        ArrayList<Recognition> detections = new ArrayList<>();

        int gridWidth = OUTPUT_WIDTH_FULL[0];

        for (int i = 0; i < gridWidth; i++) {
            float maxClass = 0;
            int detectedClass = -1;
            final float[] classes = new float[labels.size()];
            for (int c = 0; c < labels.size(); c++) {
                classes[c] = out_score[0][i][c];
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
                        Math.min(inputWidth - 1, xPos + w / 2),
                        Math.min(inputHeight - 1, yPos + h / 2));
                Label label = new Label(labels.get(detectedClass), detectedClass);
                detections.add(new Recognition("" + i, label, score, rectF));
            }
        }
        return detections;
    }

    private ArrayList<Recognition> getDetectionsForTiny() {
        ArrayList<Recognition> detections = new ArrayList<>();

        int gridWidth = OUTPUT_WIDTH_TINY[0];

        for (int i = 0; i < gridWidth; i++) {
            float maxClass = 0;
            int detectedClass = -1;
            final float[] classes = new float[labels.size()];
            for (int c = 0; c < labels.size(); c++) {
                classes[c] = out_score[0][i][c];
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
                        Math.min(inputWidth - 1, xPos + w / 2),
                        Math.min(inputHeight - 1, yPos + h / 2));
                Label label = new Label(labels.get(detectedClass), detectedClass);
                detections.add(new Recognition("" + i, label, score, rectF));
            }
        }
        return detections;
    }

    @Override
    public List<Recognition> getDetections() {
        List<Recognition> detections;
        if (IS_TINY) {
            detections = getDetectionsForTiny();
        } else {
            detections = getDetectionsForFull();
        }
        return nms(detections);
    }
}