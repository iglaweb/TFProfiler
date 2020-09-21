package ru.igla.tfprofiler.core.tflite;

import android.graphics.RectF;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * An immutable result returned by a Classifier describing what was recognized.
 */
public class Recognition {
    /**
     * A unique identifier for what has been recognized. Specific to the class, not the instance of
     * the object.
     */
    private final String id;

    /**
     * Display name for the recognition.
     */
    private final String title;

    /**
     * A sortable score for how good the recognition is relative to others. Higher should be better.
     */
    private final Float confidence;

    /**
     * Optional location within the source image for the location of the recognized object.
     */
    private RectF location;
    public int detectedClass;

    public Recognition(
            final String id, final String title, final Float confidence, final RectF location, int detectedClass) {
        this.id = id;
        this.title = title;
        this.confidence = confidence;
        this.location = location;
        this.detectedClass = detectedClass;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Float getConfidence() {
        return confidence;
    }

    public RectF getLocation() {
        return new RectF(location);
    }

    public void setLocation(RectF location) {
        this.location = location;
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