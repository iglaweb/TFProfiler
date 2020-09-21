package ru.igla.tfprofiler.yolov4;

import java.nio.ByteBuffer;

import ru.igla.tfprofiler.core.OpNormalizer;

public class YoloNormalizer implements OpNormalizer {

    private static final float IMAGE_STD = 255.0f;

    @Override
    public void convertBitmapToByteBuffer(ByteBuffer imgData, int[] intValues, int inputSize) {
        int pixel = 0;
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                final int pixelValue = intValues[pixel++];
                imgData.putFloat(((pixelValue >> 16) & 0xFF) / IMAGE_STD);
                imgData.putFloat(((pixelValue >> 8) & 0xFF) / IMAGE_STD);
                imgData.putFloat((pixelValue & 0xFF) / IMAGE_STD);
            }
        }
    }
}
