package ru.igla.tfprofiler.core.ops;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public final class GrayOpNormalizer implements OpNormalizer {

    /**
     * Float model requires additional normalization of the used input.
     */
    private static final float IMAGE_MEAN = 128f;
    private static final float IMAGE_STD = 128f;

    private final float mean;
    private final float std;

    private final boolean isModelQuantized;

    public GrayOpNormalizer(boolean isModelQuantized) {
        this.isModelQuantized = isModelQuantized;
        this.mean = IMAGE_MEAN;
        this.std = IMAGE_STD;
    }

    public GrayOpNormalizer(float mean, float std) {
        this.isModelQuantized = false;
        this.mean = mean;
        this.std = std;
    }

    @Override
    public void convertBitmapToByteBuffer(int batchSize, @NotNull ByteBuffer imgData, @NotNull int[] intValues, int inputWidth, int inputHeight) {
        for (int b = 0; b < batchSize; ++b) {
            for (int i = 0; i < inputWidth; ++i) {
                for (int j = 0; j < inputHeight; ++j) {
                    int pixelValue = intValues[i * inputWidth + j];
                    if (isModelQuantized) {
                        // Quantized model
                        imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    } else { // Float model
                        imgData.putFloat((((pixelValue >> 16) & 0xFF) - mean) / std);
                    }
                }
            }
        }
    }
}
