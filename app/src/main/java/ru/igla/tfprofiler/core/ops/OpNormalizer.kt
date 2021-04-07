package ru.igla.tfprofiler.core.ops

import java.nio.ByteBuffer

interface OpNormalizer {
    fun convertBitmapToByteBuffer(
        batchSize: Int,
        imgData: ByteBuffer,
        intValues: IntArray,
        inputWidth: Int,
        inputHeight: Int
    )
}