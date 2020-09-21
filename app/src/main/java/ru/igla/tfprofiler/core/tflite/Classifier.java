package ru.igla.tfprofiler.core.tflite;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Generic interface for interacting with different recognition engines.
 */
public interface Classifier {
    List<Recognition> recognizeImage(Bitmap bitmap);

    void close();
}