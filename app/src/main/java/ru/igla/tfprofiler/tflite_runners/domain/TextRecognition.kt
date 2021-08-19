package ru.igla.tfprofiler.tflite_runners.domain

import java.util.*

/**
 * An immutable result returned by a Classifier describing what was recognized.
 */
open class TextRecognition(
    /**
     * A unique identifier for what has been recognized. Specific to the class, not the instance of
     * the object.
     */
    val id: String,
    /**
     * Display name for the recognition.
     */
    val label: Label,
    /**
     * A sortable score for how good the recognition is relative to others. Higher should be better.
     */
    val confidence: Float
) : Comparable<TextRecognition> {

    fun getTitle(): String {
        return label.label
    }

    override fun toString(): String {
        var resultString = ""
        resultString += "[$id] "
        resultString += "$label "
        resultString += String.format(Locale.US, "(%.1f%%) ", confidence * 100.0f)
        return resultString.trim { it <= ' ' }
    }

    override fun compareTo(other: TextRecognition): Int {
        return other.confidence.compareTo(confidence)
    }
}