package ru.igla.tfprofiler.media_track

import android.graphics.Bitmap

interface RecgonizeImageCallback {
    fun startRecognizeImage(timestampBitmap: BitmapResult)
    fun onPreview(progress: Bitmap)
}