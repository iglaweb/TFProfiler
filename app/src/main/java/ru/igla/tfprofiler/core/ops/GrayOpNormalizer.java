package ru.igla.tfprofiler.core.ops;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

import ru.igla.tfprofiler.core.Size;

public final class GrayOpNormalizer implements OpNormalizer {

    /**
     * Float model requires additional normalization of the used input.
     */
    private static final float IMAGE_MEAN = 0f;
    private static final float IMAGE_STD = 1f;

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

    public static GrayOpNormalizer createFloat() {
        return new GrayOpNormalizer(0.0f, 1.0f);
    }

    public static GrayOpNormalizer createQuantized() {
        return new GrayOpNormalizer(true);
    }

    @Override
    public void convertBitmapToByteBuffer(
            @NotNull ByteBuffer imgData, @NonNull int[] intValues, Size inputSize) {
        int width = inputSize.getWidth();
        int height = inputSize.getHeight();
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                int pixelValue = intValues[i * width + j];
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
