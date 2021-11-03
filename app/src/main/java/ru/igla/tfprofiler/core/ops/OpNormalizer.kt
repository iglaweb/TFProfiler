package ru.igla.tfprofiler.core.ops

import ru.igla.tfprofiler.core.domain.Size
import java.nio.ByteBuffer

interface OpNormalizer {
    fun convertBitmapToByteBuffer(
        imgData: ByteBuffer,
        intValues: IntArray,
        inputSize: Size
    )
}