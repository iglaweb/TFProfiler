package ru.igla.tfprofiler.tflite_runners.base

import android.content.Context
import androidx.annotation.WorkerThread
import ru.igla.tfprofiler.core.ModelType
import ru.igla.tfprofiler.models_list.domain.ModelEntity
import ru.igla.tfprofiler.tflite_runners.domain.TextRecognition
import ru.igla.tfprofiler.tflite_runners.text_classification.TFLiteAPIModelTextClassification

object TextClassifierFactory {
    @Throws(Exception::class)
    @JvmStatic
    @WorkerThread
    fun create(
        context: Context,
        modelEntity: ModelEntity,
        modelOptions: ModelOptions
    ): Classifier<String, List<TextRecognition>> {
        val classifier: Classifier<String, List<TextRecognition>> =
            when (modelEntity.modelType) {
                ModelType.TEXT_CLASSIFICATION -> {
                    TFLiteAPIModelTextClassification()
                }
                else -> {
                    throw IllegalStateException("Model ${modelEntity.modelType.title} not supported here")
                }
            }
        classifier.init(context, modelEntity, modelOptions)
        return classifier
    }
}
