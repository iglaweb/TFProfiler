package ru.igla.tfprofiler.tflite_runners.base

import android.graphics.Bitmap


interface ImageRecognizer<T> {
    fun recognizeImage(bitmap: List<Bitmap>): List<T>
}