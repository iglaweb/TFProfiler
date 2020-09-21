package ru.igla.tfprofiler.core;

import java.nio.ByteBuffer;

public class BaseOpNormalizer implements OpNormalizer {

    /**
     * Float model requires additional normalization of the used input.
     */
    private static final float IMAGE_MEAN = 128f;
    private static final float IMAGE_STD = 128f;

    private final boolean isModelQuantized;

    public BaseOpNormalizer(boolean isModelQuantized) {
        this.isModelQuantized = isModelQuantized;
    }

    @Override
    public void convertBitmapToByteBuffer(ByteBuffer imgData, int[] intValues, int inputSize) {
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                } else { // Float model
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                }
            }
        }
    }
}
