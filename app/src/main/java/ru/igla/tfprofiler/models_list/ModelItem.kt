package ru.igla.tfprofiler.models_list

import org.tensorflow.lite.support.metadata.MetadataExtractor
import ru.igla.tfprofiler.core.BuiltModel
import ru.igla.tfprofiler.core.ModelType

data class ModelItem(
    val id: Long,
    val modelType: ModelType,
    val model: BuiltModel
) {
    private var tensorInputCount = 0

    private var tensorOutputCount = 0

    fun setMeta(meta: MetadataExtractor?) {
        meta?.apply {
            tensorInputCount = meta.inputTensorCount
            tensorOutputCount = meta.outputTensorCount
        }
    }
}