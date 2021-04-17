package ru.igla.tfprofiler.core


interface BuiltModel {
    val details: String
    val imageHeight: Int
    val imageWidth: Int
    val modelFormat: ModelFormat
    val colorSpace: ColorSpace
    val inputShapeType: InputShapeType
    val modelFile: String
    val labelFile: String
    val source: String
}