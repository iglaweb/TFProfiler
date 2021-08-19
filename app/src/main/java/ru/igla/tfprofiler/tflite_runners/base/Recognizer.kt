package ru.igla.tfprofiler.tflite_runners.base


interface Recognizer<Input, Output> {
    fun runInference(data: Input): Output
}