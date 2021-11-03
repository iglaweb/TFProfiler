package ru.igla.tfprofiler.models_list.domain

class SelectModelStatus(
    val success: Boolean,
    val modelType: ModelFormat,
    val modelPath: String
)