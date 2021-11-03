package ru.igla.tfprofiler.core.domain


sealed class ModelInfoConfig {
    abstract val details: String
    abstract val modelFormat: ModelOptimizedType
    abstract val modelFile: String
    abstract val labelFile: String
    abstract val source: String
}

abstract class TextConfig : ModelInfoConfig()

abstract class ImageConfig : ModelInfoConfig() {
    abstract val inputShapeType: InputShapeType
    abstract val colorSpace: ColorSpace
    abstract val imageHeight: Int
    abstract val imageWidth: Int
}