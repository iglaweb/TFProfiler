package ru.igla.tfprofiler.text_track

class StatOutResult(
    val initTime: Long,
    val inferenceTime: Long,
    val meanTime: Double,
    val stdTime: Double,
    val fps: Double,
    val memoryUsage: Long
)