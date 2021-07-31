package ru.igla.tfprofiler.tflite_runners.blazeface.ssd;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.igla.tfprofiler.core.Timber;


/**
 * Created by Michael Dietz on 07.11.2019.
 */
public class SingleShotMultiBoxDetector {
    private final AnchorOptions anchorOptions;
    private final CalculatorOptions calculatorOptions;

    private final List<Anchor> anchorList;
    private final Comparator<Detection> detectionComparator;

    public SingleShotMultiBoxDetector() {
        this(new AnchorOptions(), new CalculatorOptions());
    }

    public SingleShotMultiBoxDetector(AnchorOptions anchorOptions, CalculatorOptions calculatorOptions) {
        this.anchorOptions = anchorOptions;
        this.calculatorOptions = calculatorOptions;

        this.anchorList = getAnchors();
        this.detectionComparator = (d1, d2) -> Float.compare(d1.score, d2.score);
    }

    private float calculateScale(float minScale, float maxScale, float strideIndex, int numStrides) {
        return minScale + (maxScale - minScale) * 1.0f * strideIndex / (numStrides - 1.0f);
    }

    /**
     * Calculates anchors.
     *
     * @return
     * @link https://github.com/google/mediapipe/blob/master/mediapipe/calculators/tflite/ssd_anchors_calculator.cc
     */
    private List<Anchor> getAnchors() {
        List<Anchor> anchors = new ArrayList<>();

        if (anchorOptions.strides.length != anchorOptions.numLayers) {
            Timber.e("Stride count and numLayers must be equal!");
            return anchors;
        }

        int layerId = 0;

        while (layerId < anchorOptions.strides.length) {
            List<Float> anchorHeight = new ArrayList<>();
            List<Float> anchorWidth = new ArrayList<>();
            List<Float> aspectRatios = new ArrayList<>();
            List<Float> scales = new ArrayList<>();

            // For same strides, we merge the anchors in the same order.
            int lastSameStrideLayer = layerId;
            while (lastSameStrideLayer < anchorOptions.strides.length && anchorOptions.strides[lastSameStrideLayer] == anchorOptions.strides[layerId]) {
                final float scale = calculateScale(anchorOptions.minScale, anchorOptions.maxScale, lastSameStrideLayer, anchorOptions.strides.length);

                if (lastSameStrideLayer == 0 && anchorOptions.reduceBoxesInLowestLayer) {
                    // For first layer, it can be specified to use predefined anchors.
                    aspectRatios.add(1.0f);
                    aspectRatios.add(2.0f);
                    aspectRatios.add(0.5f);
                    scales.add(0.1f);
                    scales.add(scale);
                    scales.add(scale);
                } else {
                    for (int aspectRatioId = 0; aspectRatioId < anchorOptions.aspectRatios.length; aspectRatioId++) {
                        aspectRatios.add(anchorOptions.aspectRatios[aspectRatioId]);
                        scales.add(scale);
                    }

                    if (anchorOptions.interpolatedScaleAspectRatio > 0.0f) {
                        final float scaleNext = lastSameStrideLayer == anchorOptions.strides.length - 1 ? 1.0f : calculateScale(anchorOptions.minScale, anchorOptions.maxScale, lastSameStrideLayer + 1, anchorOptions.strides.length);

                        aspectRatios.add(anchorOptions.interpolatedScaleAspectRatio);
                        scales.add((float) Math.sqrt(scale * scaleNext));
                    }
                }
                lastSameStrideLayer += 1;
            }

            for (int i = 0; i < aspectRatios.size(); i++) {
                final float ratioSqrts = (float) Math.sqrt(aspectRatios.get(i));
                anchorHeight.add(scales.get(i) / ratioSqrts);
                anchorWidth.add(scales.get(i) * ratioSqrts);
            }

            int stride = anchorOptions.strides[layerId];
            int featureMapHeight = (int) Math.ceil(1.0 * anchorOptions.inputSizeHeight / stride);
            int featureMapWidth = (int) Math.ceil(1.0 * anchorOptions.inputSizeWidth / stride);

            if (anchorOptions.featureMapHeight.length > 0) {
                featureMapHeight = anchorOptions.featureMapHeight[layerId];
                featureMapWidth = anchorOptions.featureMapWidth[layerId];
            }

            for (int y = 0; y < featureMapHeight; y++) {
                for (int x = 0; x < featureMapWidth; x++) {
                    for (int anchorId = 0; anchorId < anchorHeight.size(); anchorId++) {
                        final float xCenter = (x + anchorOptions.anchorOffsetX) * 1.0f / featureMapWidth;
                        final float yCenter = (y + anchorOptions.anchorOffsetY) * 1.0f / featureMapHeight;

                        Anchor anchor = new Anchor();
                        anchor.setXCenter(xCenter);
                        anchor.setYCenter(yCenter);

                        if (anchorOptions.fixedAnchorSize) {
                            anchor.setWidth(1.0f);
                            anchor.setHeight(1.0f);
                        } else {
                            anchor.setWidth(anchorWidth.get(anchorId));
                            anchor.setHeight(anchorHeight.get(anchorId));
                        }

                        anchors.add(anchor);
                    }
                }
            }

            layerId = lastSameStrideLayer;
        }

        return anchors;
    }

    public List<Detection> process(final float[][][] rawBoxes, final float[][][] rawScores) {
        List<Detection> detections = new ArrayList<>();

        int candidateIndex = 0;

        int highestScoreIndex = -1;
        int largestAreaIndex = -1;

        double largestArea = Double.MIN_VALUE;
        double highestScore = Double.MIN_VALUE;

        // Filter scores
        for (int boxIndex = 0; boxIndex < calculatorOptions.numBoxes; boxIndex++) {
            double maxClassScore = Double.MIN_VALUE;
            int classId = -1;

            // Calculate max score across classes
            for (int classIndex = 0; classIndex < calculatorOptions.numClasses; classIndex++) {
                double classScore = rawScores[0][boxIndex][classIndex];

                if (calculatorOptions.sigmoidScore) {
                    if (calculatorOptions.scoreClippingThresh > 0) {
                        classScore = classScore < -calculatorOptions.scoreClippingThresh ? -calculatorOptions.scoreClippingThresh : classScore;
                        classScore = classScore > calculatorOptions.scoreClippingThresh ? calculatorOptions.scoreClippingThresh : classScore;
                    }

                    classScore = 1.0 / (1.0 + Math.exp(-classScore));
                }

                if (maxClassScore < classScore) {
                    maxClassScore = classScore;
                    classId = classIndex;
                }
            }

            if (maxClassScore >= calculatorOptions.minScoreThresh) {
                // Box candidate detected
                Detection detection = decodeBox(candidateIndex, anchorList.get(boxIndex), rawBoxes[0][boxIndex], (float) maxClassScore, classId);

                // For filter purposes
                double area = detection.width * detection.height;

                if (area > largestArea) {
                    largestArea = area;
                    largestAreaIndex = candidateIndex;
                }

                if (detection.score > highestScore) {
                    highestScore = detection.score;
                    highestScoreIndex = candidateIndex;
                }

                // Add detection to candidate list
                detections.add(detection);
                candidateIndex += 1;
            }
        }

        if (detections.size() > 0) {
            switch (calculatorOptions.filterMethod) {
                case HIGHEST_SCORE:
                    Detection highestScoreDetection = detections.get(highestScoreIndex);
                    detections.clear();
                    detections.add(highestScoreDetection);
                    break;
                case LARGEST_AREA:
                    Detection largestAreaDetection = detections.get(largestAreaIndex);
                    detections.clear();
                    detections.add(largestAreaDetection);
                    break;
                case NON_MAX_SUPPRESSION:
                    detections = weightedNonMaxSuppression(detections);
                    break;
            }
        }

        return detections;
    }

    private Detection decodeBox(int id, Anchor anchor, final float[] rawBoxValues, float score, int classId) {
        if (rawBoxValues.length != calculatorOptions.numCoords) {
            Timber.e("rawBoxValues.length != calculatorOptions.numCoords");
        }

        final int boxOffset = calculatorOptions.boxCoordOffset;

        float yCenter = rawBoxValues[boxOffset];
        float xCenter = rawBoxValues[boxOffset + 1];
        float height = rawBoxValues[boxOffset + 2];
        float width = rawBoxValues[boxOffset + 3];

        if (calculatorOptions.reverseOutputOrder) {
            xCenter = rawBoxValues[boxOffset];
            yCenter = rawBoxValues[boxOffset + 1];
            width = rawBoxValues[boxOffset + 2];
            height = rawBoxValues[boxOffset + 3];
        }

        xCenter = xCenter / calculatorOptions.xScale * anchor.getWidth() + anchor.getXCenter();
        yCenter = yCenter / calculatorOptions.yScale * anchor.getHeight() + anchor.getYCenter();

        if (calculatorOptions.applyExponentialOnBoxSize) {
            height = (float) (Math.exp(height / calculatorOptions.hScale) * anchor.getHeight());
            width = (float) (Math.exp(width / calculatorOptions.wScale) * anchor.getWidth());
        } else {
            height = height / calculatorOptions.hScale * anchor.getHeight();
            width = width / calculatorOptions.wScale * anchor.getWidth();
        }

        final float yMin = yCenter - (height / 2.0f);
        final float xMin = xCenter - (width / 2.0f);
        final float yMax = yCenter + (height / 2.0f);
        final float xMax = xCenter + (width / 2.0f);

        Detection detection = new Detection();
        detection.id = id;
        detection.yMin = calculatorOptions.flipVertically ? 1.0f - yMax : yMin;
        detection.xMin = xMin;
        detection.width = xMax - xMin;
        detection.height = yMax - yMin;
        detection.score = score;
        detection.classId = classId;

        if (calculatorOptions.numKeypoints > 0) {
            List<Keypoint> keypoints = new ArrayList<>();

            for (int keypointId = 0; keypointId < calculatorOptions.numKeypoints; keypointId++) {
                final int keypointOffset = boxOffset + calculatorOptions.keypointCoordOffset + keypointId * calculatorOptions.numValuesPerKeypoint;

                float keypointY = rawBoxValues[keypointOffset];
                float keypointX = rawBoxValues[keypointOffset + 1];

                if (calculatorOptions.reverseOutputOrder) {
                    keypointX = rawBoxValues[keypointOffset];
                    keypointY = rawBoxValues[keypointOffset + 1];
                }

                keypointX = keypointX / calculatorOptions.xScale * anchor.getWidth() + anchor.getXCenter();
                keypointY = keypointY / calculatorOptions.yScale * anchor.getHeight() + anchor.getYCenter();

                float x = keypointX;
                float y = calculatorOptions.flipVertically ? 1.0f - keypointY : keypointY;
                Keypoint keypoint = new Keypoint(x, y);
                keypoints.add(keypoint);
            }

            detection.keypoints = keypoints;
        }

        return detection;
    }

    private List<Detection> weightedNonMaxSuppression(List<Detection> detections) {
        List<Detection> remaining = new ArrayList<>(detections);
        List<Detection> picked = new ArrayList<>();
        List<Detection> suppressed = new ArrayList<>();

        // Sort list by score ascending
        Collections.sort(remaining, detectionComparator);

        int lastIndex;
        Detection pick;
        while (!remaining.isEmpty()) {
            // Move last element (with highest score) into picked list
            lastIndex = remaining.size() - 1;

            pick = remaining.get(lastIndex);
            picked.add(pick);
            remaining.remove(pick);

            // Compare overlap with all remaining detections
            for (Detection remainingDetection : remaining) {
                // Remove detection if overlap with pick is bigger than threshold
                if (pick.getOverlap(remainingDetection) > calculatorOptions.nmsThreshold) {
                    suppressed.add(remainingDetection);
                }
            }

            remaining.removeAll(suppressed);
            suppressed.clear();
        }

        return picked;
    }
}










