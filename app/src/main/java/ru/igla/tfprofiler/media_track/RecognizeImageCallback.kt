package ru.igla.tfprofiler.media_track

import android.graphics.Bitmap

interface RecognizeImageCallback {
    fun startRecognizeImage(timestampBitmap: BitmapResult)
    fun onPreview(progress: Bitmap)
}