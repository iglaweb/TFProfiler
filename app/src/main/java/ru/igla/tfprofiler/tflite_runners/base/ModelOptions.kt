package ru.igla.tfprofiler.tflite_runners.base

import ru.igla.tfprofiler.core.domain.Device

data class ModelOptions constructor(
    val device: Device = Device.CPU,
    val numThreads: Int = 4,
    val useXnnpack: Boolean = false,
    val numberOfInputImages: Int = 1,
    val useCpuStress: Boolean
) {

    companion object {
        val default by lazy {
            ModelOptions(
                device = Device.CPU,
                numThreads = 4,
                useXnnpack = false,
                numberOfInputImages = 1,
                false
            )
        }
    }

    override fun toString(): String {
        return String.format(
            "(device=%s, numThreads=%d, useXnnpack=%s, numberOfInputImages=%d)",
            device, numThreads, useXnnpack.toString(), numberOfInputImages
        )
    }

    fun getReadableStr(): String {
        val strDetails = StringBuilder(device.name).apply {
            val threads = numThreads
            val useXnnpack = useXnnpack

            if (isNotEmpty()) {
                append(", ")
            }
            append(threads)
            if (threads == 1) {
                append(" Thread")
            } else {
                append(" Threads")
            }
            if (useXnnpack) {
                append(", XNNPACK")
            }
        }
        return strDetails.toString()
    }
}
