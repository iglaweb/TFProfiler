package ru.igla.tfprofiler.media_track

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.Videoio
import ru.igla.tfprofiler.core.Resource
import ru.igla.tfprofiler.utils.logI
import java.io.File
import java.io.IOException
import java.util.*

class OpenCVVideoFramesExtractor : ReadVideoFileInterface {
    override fun readVideoFile(
        filePath: String
    ): Flow<Resource<FrameMetaInfo>> {
        return flow {
            if (!File(filePath).exists()) {
                throw IOException("Video file not exists")
            }
            val videoCapture = VideoCapture(filePath)
            if (!videoCapture.isOpened) {
                throw IOException(Exception("Video file is not opened!"))
            }
            val fpsRate = videoCapture[Videoio.CAP_PROP_FPS].toInt()
            val totalFrames = videoCapture[Videoio.CAP_PROP_FRAME_COUNT].toInt()
            logI { "FPS: $fpsRate; total frames: $totalFrames" }
            var frameNumber = 0
            while (true) {
                val matFrame = Mat()
                try {
                    if (videoCapture.read(matFrame)) {
                        frameNumber++
                        logI {
                            String.format(
                                Locale.US,
                                "Frame %d: %dx%d (original)",
                                frameNumber,
                                matFrame.width(),
                                matFrame.height()
                            )
                        }
                        val bitmap = Bitmap.createBitmap(
                            matFrame.width(),
                            matFrame.height(),
                            Bitmap.Config.ARGB_8888
                        )
                        Utils.matToBitmap(matFrame, bitmap)
                        emit(Resource.loading(FrameMetaInfo(bitmap, frameNumber, totalFrames)))
                    } else {
                        logI { "Frame is not obtained. Break!" }
                        break
                    }
                } finally {
                    matFrame.free()
                }
            }
            videoCapture.free()
            emit(Resource.success(FrameMetaInfo(null, frameNumber, totalFrames)))
        }
    }
}