package ru.igla.tfprofiler.tflite_runners

import ru.igla.tfprofiler.core.Device

data class ModelOptions constructor(
    val device: Device,
    val numThreads: Int,
    val useXnnpack: Boolean
) {
    data class Builder(
        private var device: Device = Device.CPU,
        private var numThreads: Int = 4,
        private var useXnnpack: Boolean = false
    ) {
        fun device(device: Device) = apply { this.device = device }
        fun numThreads(numThreads: Int) = apply { this.numThreads = numThreads }
        fun xnnpack(useXnnpack: Boolean) = apply { this.useXnnpack = useXnnpack }
        fun build() = ModelOptions(device, numThreads, useXnnpack)
    }

    override fun toString(): String {
        return String.format(
            "(device=%s, numThreads=%d, useXnnpack=%s)",
            device, numThreads, useXnnpack.toString()
        )
    }
}
