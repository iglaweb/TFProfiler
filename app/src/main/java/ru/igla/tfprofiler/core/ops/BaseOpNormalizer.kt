package ru.igla.tfprofiler.core.ops

import ru.igla.tfprofiler.core.domain.ColorSpace
import ru.igla.tfprofiler.core.domain.Size
import java.nio.ByteBuffer

class BaseOpNormalizer : OpNormalizer {
    private val mean: Float
    private val std: Float
    private val isModelQuantized: Boolean
    private val colorSpace: ColorSpace

    constructor(colorSpace: ColorSpace, isModelQuantized: Boolean) {
        this.colorSpace = colorSpace
        this.isModelQuantized = isModelQuantized
        mean = IMAGE_MEAN
        std = IMAGE_STD
    }

    constructor(colorSpace: ColorSpace, isModelQuantized: Boolean, mean: Float, std: Float) {
        this.colorSpace = colorSpace
        this.isModelQuantized = isModelQuantized
        this.mean = mean
        this.std = std
    }

    override fun convertBitmapToByteBuffer(
        imgData: ByteBuffer, intValues: IntArray, inputSize: Size
    ) {
        val width = inputSize.width
        val height = inputSize.height
        
        for (i in 0 until width) {
            for (j in 0 until height) {
                val pixelValue = intValues[i * width + j]
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((pixelValue shr 16 and 0xFF).toByte())
                    if (colorSpace === ColorSpace.COLOR) {
                        imgData.put((pixelValue shr 8 and 0xFF).toByte())
                        imgData.put((pixelValue and 0xFF).toByte())
                    }
                } else { // Float model
                    imgData.putFloat(((pixelValue shr 16 and 0xFF) - mean) / std)
                    if (colorSpace === ColorSpace.COLOR) {
                        imgData.putFloat(((pixelValue shr 8 and 0xFF) - mean) / std)
                        imgData.putFloat(((pixelValue and 0xFF) - mean) / std)
                    }
                }
            }
        }
    }

    companion object {
        private const val IMAGE_MEAN = 0f
        private const val IMAGE_STD = 1f
        fun createFloat(colorSpace: ColorSpace, mean: Float, std: Float): BaseOpNormalizer {
            return BaseOpNormalizer(colorSpace, false, mean, std)
        }

        fun createQuantized(colorSpace: ColorSpace, mean: Float, std: Float): BaseOpNormalizer {
            return BaseOpNormalizer(colorSpace, true, mean, std)
        }

        fun createFloat(colorSpace: ColorSpace): BaseOpNormalizer {
            return BaseOpNormalizer(colorSpace, false, 0.0f, 1.0f)
        }

        fun createQuantized(colorSpace: ColorSpace): BaseOpNormalizer {
            return BaseOpNormalizer(colorSpace, true, 0.0f, 1.0f)
        }
    }
}