package ru.igla.tfprofiler.core.tflite

import ru.igla.tfprofiler.core.Device

class FailedCreateTFDelegate(
    val device: Device,
    message: String
) : Exception(message)