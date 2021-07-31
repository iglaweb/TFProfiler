package ru.igla.tfprofiler.core.jni

import org.opencv.core.Mat

interface DnnModelExecutor {
    fun init(): Boolean
    fun deInit()
    fun executeModel(images: List<Mat>)
}