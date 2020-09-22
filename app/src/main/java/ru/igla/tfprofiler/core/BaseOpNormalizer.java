package ru.igla.tfprofiler.core;

import java.nio.ByteBuffer;

public class BaseOpNormalizer implements OpNormalizer {

    /**
     * Float model requires additional normalization of the used input.
     */
    private static final float IMAGE_MEAN = 128f;
    private static final float IMAGE_STD = 128f;

    private final float mean;
    private final float std;

    private final boolean isModelQuantized;

    public BaseOpNormalizer(boolean isModelQuantized) {
        this.isModelQuantized = isModelQuantized;
        this.mean = IMAGE_MEAN;
        this.std = IMAGE_STD;
    }

    public BaseOpNormalizer(boolean isModelQuantized, float mean, float std) {
        this.isModelQuantized = isModelQuantized;
        this.mean = mean;
        this.std = std;
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
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - mean) / std);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - mean) / std);
                    imgData.putFloat(((pixelValue & 0xFF) - mean) / std);
                }
            }
        }
    }
}
