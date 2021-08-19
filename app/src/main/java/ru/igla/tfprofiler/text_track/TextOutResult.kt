package ru.igla.tfprofiler.text_track

import ru.igla.tfprofiler.tflite_runners.domain.TextRecognition

class TextOutResult(
    val text: List<TextRecognition>,
    val statOutResult: StatOutResult?
)