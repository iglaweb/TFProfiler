package ru.igla.tfprofiler.tflite_runners.domain;

import android.graphics.RectF;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import ru.igla.tfprofiler.tflite_runners.blazeface.ssd.Keypoint;

/**
 * An immutable result returned by a Classifier describing what was recognized.
 */
public final class ImRecognition extends TextRecognition {

    /**
     * Optional location within the source image for the location of the recognized object.
     */
    private RectF location;

    @NonNull
    public List<Keypoint> keypoints;

    /***
     * Auxiliary array for keypoints
     */
    public float[] points;

    public ImRecognition(
            final String id,
            final Label label,
            final Float confidence,
            final RectF location,
            @NonNull final List<Keypoint> keypoints) {
        super(id, label, confidence);
        this.location = location;
        this.keypoints = keypoints;
    }

    public ImRecognition(
            final String id,
            final Label title,
            final float confidence,
            @NonNull final RectF location) {
        this(id, title, confidence, location, new ArrayList<>());
    }

    public ImRecognition(
            final String id,
            final String title,
            final float confidence,
            @NonNull final RectF location) {
        this(id, new Label(title, 0), confidence, location, new ArrayList<>());
    }

    public ImRecognition(
            final String id,
            final String title,
            final float confidence,
            @NonNull final RectF location,
            @NonNull final List<Keypoint> keypoints) {
        this(id, new Label(title, 0), confidence, location, keypoints);
    }

    public float[] getPoints() {
        return points;
    }

    @NonNull
    public RectF getLocation() {
        return new RectF(location);
    }

    public void setLocation(RectF location) {
        this.location = location;
    }

    public void setPoints(float[] points) {
        this.points = points;
    }

    @NotNull
    @Override
    public String toString() {
        String resultString = super.toString();
        if (location != null) {
            resultString += location + " ";
        }
        return resultString.trim();
    }
}