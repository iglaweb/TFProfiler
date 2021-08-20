package ru.igla.tfprofiler.tflite_runners.base

import ru.igla.tfprofiler.tflite_runners.domain.TextRecognition


open class TFLiteTextDetectAPIModelBase :
    TFLiteAPIModelBase<String, List<TextRecognition>>(),
    Classifier<String, List<TextRecognition>> {

    /***
     * Bitmaps of equal size and the size matches the neural network input
     * @param data bitmaps to normalize
     */
    override fun preprocess(data: String): Array<Any> {
        return emptyArray()
    }

    override fun prepareOutputs(): MutableMap<Int, Any> {
        return mutableMapOf()
    }

    override fun getDetections(outputMap: Map<Int, Any>): List<TextRecognition> {
        return emptyList()
    }
}