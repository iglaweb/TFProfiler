package ru.igla.tfprofiler.media_track

import ru.igla.tfprofiler.video.TakeVideoFrameListener
import ru.igla.tfprofiler.video.UpdateProgressListener
import java.io.IOException

interface ReadVideoFileInterface {
    @Throws(IOException::class)
    fun readVideoFile(
        filePath: String,
        listener: UpdateProgressListener,
        takeVideoFrameListener: TakeVideoFrameListener
    ): Boolean
}