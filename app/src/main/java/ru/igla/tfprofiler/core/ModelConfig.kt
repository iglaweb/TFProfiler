package ru.igla.tfprofiler.core


interface ModelConfig {
    val details: String
    val imageHeight: Int
    val imageWidth: Int
    val modelFormat: ModelOptimizedType
    val colorSpace: ColorSpace
    val inputShapeType: InputShapeType
    val modelFile: String
    val labelFile: String
    val source: String
}