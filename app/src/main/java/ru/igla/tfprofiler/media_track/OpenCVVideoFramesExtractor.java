package ru.igla.tfprofiler.media_track;

import android.graphics.Bitmap;

import org.jetbrains.annotations.NotNull;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import ru.igla.tfprofiler.core.Timber;
import ru.igla.tfprofiler.video.TakeVideoFrameListener;
import ru.igla.tfprofiler.video.UpdateProgressListener;

public class OpenCVVideoFramesExtractor implements ReadVideoFileInterface {

    @Override
    public boolean readVideoFile(@NotNull String mediaFile,
                                 @NotNull UpdateProgressListener listener,
                                 @NotNull TakeVideoFrameListener takeVideoFrameListener) throws IOException {
        if (!new File(mediaFile).exists()) {
            Timber.e(new Exception("Video file not exists"));
            return false;
        }

        VideoCapture videoCapture = new VideoCapture(mediaFile);
        if (!videoCapture.isOpened()) {
            throw new IOException(new Exception("Video file is not opened!"));
        }

        int fpsRate = (int) videoCapture.get(Videoio.CAP_PROP_FPS);
        int frameCount = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT);
        Timber.i("FPS: " + fpsRate + "; total frames: " + frameCount);

        int frameNumber = 0;
        while (true) {
            Mat matFrame = new Mat();
            try {
                if (videoCapture.read(matFrame)) {
                    frameNumber++;
                    String str = String.format(Locale.US, "Frame %d: %dx%d (original)", frameNumber, matFrame.width(), matFrame.height());
                    Timber.i(str);

                    FrameInformation frameInformation = new FrameInformation(frameCount, frameNumber);
                    listener.onUpdate(frameInformation);

                    Bitmap bitmap = Bitmap.createBitmap(matFrame.width(), matFrame.height(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(matFrame, bitmap);
                    takeVideoFrameListener.onTakeFrame(bitmap);

                } else {
                    Timber.i("Frame is not obtained. Break!");
                    break;
                }
            } finally {
                matFrame.release();
                FrameInformation frameInformation = new FrameInformation(frameCount, frameNumber);
                listener.onUpdate(frameInformation);
            }
        }
        videoCapture.release();
        return true;
    }
}
