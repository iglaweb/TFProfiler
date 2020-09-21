package ru.igla.tfprofiler.core;

import java.nio.ByteBuffer;

public interface OpNormalizer {
    void convertBitmapToByteBuffer(ByteBuffer imgData, int[] intValues, int inputSize);
}
