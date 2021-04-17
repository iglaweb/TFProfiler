package ru.igla.tfprofiler.tflite_runners.base

import android.graphics.Bitmap


interface ImageRecognizer<T> {
    fun recognizeImage(bitmaps: List<Bitmap>): List<T>
}