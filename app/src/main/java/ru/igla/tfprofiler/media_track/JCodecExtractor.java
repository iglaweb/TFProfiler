package ru.igla.tfprofiler.media_track;

import android.graphics.Bitmap;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.MediaInfo;
import org.jcodec.common.AndroidUtil;
import org.jcodec.common.Demuxer;
import org.jcodec.common.DemuxerTrack;
import org.jcodec.common.DemuxerTrackMeta;
import org.jcodec.common.Format;
import org.jcodec.common.JCodecUtil;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.IOUtils;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import ru.igla.tfprofiler.core.Timber;
import ru.igla.tfprofiler.video.TakeVideoFrameListener;
import ru.igla.tfprofiler.video.UpdateProgressListener;

public final class JCodecExtractor implements ReadVideoFileInterface {

    @Override
    public boolean readVideoFile(@NotNull String filePath,
                                 @NotNull UpdateProgressListener listener,
                                 @NotNull TakeVideoFrameListener takeVideoFrameListener) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("File not exists!");
        }

        int frameCount = 1;

        Format format;
        Demuxer demuxer = null;
        try {
            format = JCodecUtil.detectFormat(file);
            if (format == null) {
                throw new IOException("Video format is not supported. Try other video");
            }

            demuxer = JCodecUtil.createDemuxer(format, file);
            DemuxerTrack vt = demuxer.getVideoTracks().get(0);
            DemuxerTrackMeta dtm = vt.getMeta();

            frameCount = dtm.getTotalFrames();
            int fps = (int) (dtm.getTotalFrames() / dtm.getTotalDuration());

            Timber.i("Frames: " + frameCount + ", fps: " + fps);
        } catch (IOException e) {
            Timber.e(e);
        } finally {
            IOUtils.closeQuietly(demuxer);
        }

        if (frameCount == 0) {
            throw new IOException("Video duration is 0");
        }


        FrameGrab frameGrab = null;
        FileChannelWrapper fileChannelWrapper = null;
        try {
            fileChannelWrapper = NIOUtils.readableChannel(file);
            frameGrab = FrameGrab.createFrameGrab(fileChannelWrapper);
        } catch (IOException | JCodecException e) {
            Timber.e(e);
        }

        if (frameGrab == null) {
            IOUtils.closeQuietly(fileChannelWrapper);
            throw new IOException("Cannot create frame grabber");
        }

        MediaInfo mi = frameGrab.getMediaInfo();
        Bitmap bitmap = Bitmap.createBitmap(mi.getDim().getWidth(), mi.getDim().getHeight(), Bitmap.Config.ARGB_8888);

        Picture picture = null;
        int frameNumber = 0;

        while (true) {
            try {
                if (null == (picture = frameGrab.getNativeFrame())) break;
            } catch (IOException e) {
                Timber.e(e);
            }
            assert (picture != null);
            frameNumber++;

            FrameInformation frameInformation = new FrameInformation(frameCount, frameNumber);
            frameInformation.setFrameNumber(frameNumber);
            listener.onUpdate(frameInformation);

            AndroidUtil.toBitmap(picture, bitmap);
            Timber.i(picture.getWidth() + "x" + picture.getHeight() + " " + picture.getColor());

            // IOUtils.writeBitmapExternalStorage("bmp_video_" + frameNumber, bitmap);
            takeVideoFrameListener.onTakeFrame(bitmap);
        }

        IOUtils.closeQuietly(fileChannelWrapper);
        return true;
    }
}
