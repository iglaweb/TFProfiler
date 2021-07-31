package ru.igla.tfprofiler.core.ops;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

import ru.igla.tfprofiler.core.Size;

public final class BaseOpNormalizer implements OpNormalizer {

    private final float mean;
    private final float std;
    private final boolean isModelQuantized;

    private static final float IMAGE_MEAN = 0f;
    private static final float IMAGE_STD = 1f;

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

    public static BaseOpNormalizer createFloat(float mean, float std) {
        return new BaseOpNormalizer(false, mean, std);
    }

    public static BaseOpNormalizer createQuantized(float mean, float std) {
        return new BaseOpNormalizer(true, mean, std);
    }

    public static BaseOpNormalizer createFloat() {
        return new BaseOpNormalizer(false, 0.0f, 1.0f);
    }

    public static BaseOpNormalizer createQuantized() {
        return new BaseOpNormalizer(true, 0.0f, 1.0f);
    }

    @Override
    public void convertBitmapToByteBuffer(
            @NotNull ByteBuffer imgData, @NonNull int[] intValues, Size inputSize) {
        final int width = inputSize.getWidth();
        final int height = inputSize.getHeight();
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                int pixelValue = intValues[i * width + j];
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

