package ru.igla.tfprofiler.tflite_runners.base

import ru.igla.tfprofiler.core.Device

data class ModelOptions constructor(
    val device: Device = Device.CPU,
    val numThreads: Int = 4,
    val useXnnpack: Boolean = false,
    val numberOfInputImages: Int = 1
) {
    override fun toString(): String {
        return String.format(
            "(device=%s, numThreads=%d, useXnnpack=%s, numberOfInputImages=%d)",
            device, numThreads, useXnnpack.toString(), numberOfInputImages
        )
    }
}
