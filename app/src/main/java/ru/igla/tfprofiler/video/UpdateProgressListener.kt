package ru.igla.tfprofiler.video

import ru.igla.tfprofiler.media_track.FrameInformation


interface UpdateProgressListener {
    fun onUpdate(information: FrameInformation)
}

interface TakeVideoFrameListener {
    fun onTakeFrame(bitmap: TimestampBitmap)
}