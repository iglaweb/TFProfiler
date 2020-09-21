package ru.igla.tfprofiler.core.blazeface.ssd;

import java.util.ArrayList;
import java.util.List;

public class Detection {
    public int id;
    public float yMin;
    public float xMin;
    public float width;
    public float height;
    public float score;
    public int classId;
    public List<Keypoint> keypoints = new ArrayList<>();

    public float getArea() {
        return width * height;
    }

    public float getOverlap(Detection other) {
        float overlap = 0;

        // Get top left coordinate of overlapping area
        float overlapX1 = Math.max(xMin, other.xMin);
        float overlapY1 = Math.max(yMin, other.yMin);

        // Get bottom right coordinate of overlapping area
        float overlapX2 = Math.min(xMin + width, other.xMin + other.width);
        float overlapY2 = Math.min(yMin + height, other.yMin + other.height);

        float overlapWidth = Math.max(0, overlapX2 - overlapX1);
        float overlapHeight = Math.max(0, overlapY2 - overlapY1);

        float overlapArea = overlapWidth * overlapHeight;

        if (overlapArea > 0) {
            // Overlap value is calculated by dividing the intersection through the total area
            overlap = overlapArea / (getArea() + other.getArea() - overlapArea);
        }

        return overlap;
    }
}