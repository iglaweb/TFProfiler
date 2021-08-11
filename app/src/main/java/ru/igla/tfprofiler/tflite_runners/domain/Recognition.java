package ru.igla.tfprofiler.tflite_runners.domain;

import android.graphics.RectF;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.igla.tfprofiler.tflite_runners.blazeface.ssd.Keypoint;

/**
 * An immutable result returned by a Classifier describing what was recognized.
 */
public final class Recognition {
    /**
     * A unique identifier for what has been recognized. Specific to the class, not the instance of
     * the object.
     */
    private final String id;

    /**
     * Display name for the recognition.
     */
    private final Label title;

    /**
     * A sortable score for how good the recognition is relative to others. Higher should be better.
     */
    private final Float confidence;

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

    public Recognition(
            final String id,
            final Label label,
            final Float confidence,
            final RectF location,
            @NonNull final List<Keypoint> keypoints) {
        this.id = id;
        this.title = label;
        this.confidence = confidence;
        this.location = location;
        this.keypoints = keypoints;
    }

    public Recognition(
            final String id,
            final Label title,
            final float confidence,
            @NonNull final RectF location) {
        this(id, title, confidence, location, new ArrayList<>());
    }

    public Recognition(
            final String id,
            final String title,
            final float confidence,
            @NonNull final RectF location) {
        this(id, new Label(title, 0), confidence, location, new ArrayList<>());
    }

    public Recognition(
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

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title == null ? "" : title.getLabel();
    }

    public Label getLabel() {
        return title;
    }

    public float getConfidence() {
        return confidence;
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
        String resultString = "";
        if (id != null) {
            resultString += "[" + id + "] ";
        }

        if (title != null) {
            resultString += title + " ";
        }

        if (confidence != null) {
            resultString += String.format(Locale.US, "(%.1f%%) ", confidence * 100.0f);
        }

        if (location != null) {
            resultString += location + " ";
        }

        return resultString.trim();
    }
}