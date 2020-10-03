package ru.igla.tfprofiler.core


interface BuiltModel {
    val details: String
    val imageHeight: Int
    val imageWidth: Int
    val quantized: Boolean
    val colorSpace: ColorSpace
    val modelFile: String
    val labelFile: String
    val source: String
}