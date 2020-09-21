package ru.igla.tfprofiler.media_track


data class FrameInformation constructor(
    var framesCount: Int,
    var frameNumber: Int
) {
    //check if total frames is 0, progress is 50%
    val progress = if (framesCount == 0) 50 else {
        if (framesCount == frameNumber) 100 else
            ((frameNumber / framesCount.toFloat()) * 100f).toInt()
    }
}