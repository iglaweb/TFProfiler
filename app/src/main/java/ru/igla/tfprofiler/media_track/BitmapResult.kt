package ru.igla.tfprofiler.media_track

class BitmapResult(
        val previewWidth: Int, val previewHeight: Int,
        val croppedWidth: Int, val croppedHeight: Int,
        val initTime: Long,
        val inferenceTime: Long,
        val meanTime: Double,
        val stdTime: Double,
        val fps: Double,
        val memoryUsage: Long
)