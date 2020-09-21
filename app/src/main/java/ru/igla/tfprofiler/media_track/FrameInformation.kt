package ru.igla.tfprofiler.media_track


data class FrameInformation constructor(
    var framesCount: Int,
    var frameNumber: Int
) {
    val progress =
        if (framesCount == frameNumber) 100 else
            ((frameNumber / framesCount.toFloat()) * 100f).toInt()
}