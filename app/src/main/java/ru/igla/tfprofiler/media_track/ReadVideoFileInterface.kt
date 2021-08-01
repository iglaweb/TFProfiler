package ru.igla.tfprofiler.media_track

import kotlinx.coroutines.flow.Flow
import ru.igla.tfprofiler.core.Resource

interface ReadVideoFileInterface {
    fun readVideoFile(
        filePath: String
    ): Flow<Resource<FrameMetaInfo>>
}