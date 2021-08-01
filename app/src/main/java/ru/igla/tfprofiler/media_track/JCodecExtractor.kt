package ru.igla.tfprofiler.media_track

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jcodec.api.FrameGrab
import org.jcodec.api.JCodecException
import org.jcodec.common.AndroidUtil
import org.jcodec.common.Demuxer
import org.jcodec.common.Format
import org.jcodec.common.JCodecUtil
import org.jcodec.common.io.FileChannelWrapper
import org.jcodec.common.io.IOUtils
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.model.Picture
import ru.igla.tfprofiler.core.Resource
import ru.igla.tfprofiler.core.Timber
import ru.igla.tfprofiler.utils.logI
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class JCodecExtractor : ReadVideoFileInterface {
    @Throws(IOException::class)
    override fun readVideoFile(
        filePath: String
    ): Flow<Resource<FrameMetaInfo>> {
        return flow {
            val file = File(filePath)
            if (!file.exists()) {
                throw FileNotFoundException("File not exists!")
            }
            var totalFrames = 0
            val format: Format?
            var demuxer: Demuxer? = null
            try {
                format = JCodecUtil.detectFormat(file)
                if (format == null) {
                    throw IOException("Video format is not supported. Try other video")
                }
                demuxer = JCodecUtil.createDemuxer(format, file)
                val vt = demuxer.videoTracks[0]
                val dtm = vt.meta
                totalFrames = dtm.totalFrames
                logI {
                    val fps = (totalFrames / dtm.totalDuration).toInt()
                    "Frames: $totalFrames, fps: $fps"
                }
            } catch (e: IOException) {
                Timber.e(e)
            } finally {
                IOUtils.closeQuietly(demuxer)
            }
            if (totalFrames == 0) {
                throw IOException("Video duration is 0")
            }
            var frameGrab: FrameGrab? = null
            var fileChannelWrapper: FileChannelWrapper? = null
            try {
                fileChannelWrapper = NIOUtils.readableChannel(file)
                frameGrab = FrameGrab.createFrameGrab(fileChannelWrapper)
            } catch (e: IOException) {
                Timber.e(e)
            } catch (e: JCodecException) {
                Timber.e(e)
            }
            if (frameGrab == null) {
                IOUtils.closeQuietly(fileChannelWrapper)
                throw IOException("Cannot create frame grabber")
            }
            val mi = frameGrab.mediaInfo
            val bitmap = Bitmap.createBitmap(mi.dim.width, mi.dim.height, Bitmap.Config.ARGB_8888)
            var picture: Picture? = null
            var frameNumber = 0
            while (true) {
                try {
                    if (null == frameGrab.nativeFrame.also { picture = it }) break
                } catch (e: IOException) {
                    Timber.e(e)
                }
                val jcodecPic = checkNotNull(picture)

                frameNumber++
                val frameInformation = FrameInformation(totalFrames, frameNumber)
                frameInformation.frameNumber = frameNumber
                AndroidUtil.toBitmap(jcodecPic, bitmap)
                logI {
                    jcodecPic.width.toString() + "x" + jcodecPic.height + " " + jcodecPic.color
                }

                // IOUtils.writeBitmapExternalStorage("bmp_video_" + frameNumber, bitmap);
                emit(Resource.loading(FrameMetaInfo(bitmap, frameNumber, totalFrames)))
            }
            emit(Resource.success(FrameMetaInfo(null, frameNumber, totalFrames)))
            IOUtils.closeQuietly(fileChannelWrapper)
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }
}