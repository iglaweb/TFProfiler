package ru.igla.tfprofiler.video


enum class VideoExtractType(val id: Int) {
    ANY(0),
    JCODEC(1),
    ANDROID(2),
    OPENCV(3)
}