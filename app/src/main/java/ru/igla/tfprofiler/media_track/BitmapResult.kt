package ru.igla.tfprofiler.media_track

import ru.igla.tfprofiler.text_track.StatOutResult

class BitmapResult(
    val previewWidth: Int, val previewHeight: Int,
    val croppedWidth: Int, val croppedHeight: Int,
    val statOutResult: StatOutResult
)