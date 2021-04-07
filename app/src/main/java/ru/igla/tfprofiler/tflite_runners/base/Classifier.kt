package ru.igla.tfprofiler.tflite_runners.base

import android.content.Context
import ru.igla.tfprofiler.models_list.ModelEntity
import java.io.Closeable

/**
 * Generic interface for interacting with different recognition engines.
 */
interface Classifier<T> : ImageRecognizer<T>, Closeable {
    @Throws(Exception::class)
    fun init(context: Context, modelEntity: ModelEntity, modelOptions: ModelOptions)
}