package ru.igla.tfprofiler.core.ops

import java.nio.ByteBuffer

interface OpNormalizer {
    fun convertBitmapToByteBuffer(
        imgData: ByteBuffer,
        intValues: IntArray,
        inputWidth: Int,
        inputHeight: Int
    )
}