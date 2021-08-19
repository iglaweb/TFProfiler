package ru.igla.tfprofiler.tflite_runners.base

import android.content.Context
import ru.igla.tfprofiler.core.Size
import ru.igla.tfprofiler.models_list.ModelEntity
import ru.igla.tfprofiler.tflite_runners.domain.TextRecognition


open class TFLiteTextDetectAPIModelBase :
    TFLiteAPIModelBase<String, List<TextRecognition>>(),
    Classifier<String, List<TextRecognition>> {

    protected lateinit var modelOptions: ModelOptions
    protected lateinit var inputSize: Size

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param context     The asset manager to be used to load assets.
     * @param modelEntity The filepath of the model GraphDef protocol buffer.
     */
    @Throws(Exception::class)
    override fun init(
        context: Context,
        modelEntity: ModelEntity,
        modelOptions: ModelOptions
    ) {
        super.init(context, modelEntity, modelOptions)
        this.modelOptions = modelOptions
        this.inputSize = modelEntity.modelConfig.inputSize
    }

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