package ru.igla.tfprofiler.tflite_runners;

import android.content.Context;
import android.graphics.RectF;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.igla.tfprofiler.core.blazeface.ssd.Keypoint;
import ru.igla.tfprofiler.models_list.ModelEntity;

/**
 * Generic interface for interacting with different recognition engines.
 */
public interface Classifier<T> extends
        ImageRecognizer<T>,
        Closeable {

    void init(Context context, ModelEntity modelEntity, ModelOptions modelOptions) throws Exception;

    class Label {
        private final String label;
        private final int classId;

        public Label(String label, int classId) {
            this.label = label;
            this.classId = classId;
        }

        public String getLabel() {
            return label;
        }

        public int getClassId() {
            return classId;
        }
    }

    /**
     * An immutable result returned by a Classifier describing what was recognized.
     */
    class Recognition {
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

        public List<Keypoint> keypoints;

        public Recognition(
                final String id,
                final Label label,
                final Float confidence,
                final RectF location,
                final List<Keypoint> keypoints) {
            this.id = id;
            this.title = label;
            this.confidence = confidence;
            this.location = location;
            this.keypoints = keypoints;
        }

        public Recognition(
                final String id,
                final Label title,
                final Float confidence,
                final RectF location) {
            this(id, title, confidence, location, new ArrayList<>());
        }

        public Recognition(
                final String id,
                final String title,
                final Float confidence,
                final RectF location) {
            this(id, new Label(title, 0), confidence, location, new ArrayList<>());
        }

        public Recognition(
                final String id,
                final String title,
                final Float confidence,
                final RectF location,
                final List<Keypoint> keypoints) {
            this(id, new Label(title, 0), confidence, location, keypoints);
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title == null ? "" : title.label;
        }

        public Label getLabel() {
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
}
