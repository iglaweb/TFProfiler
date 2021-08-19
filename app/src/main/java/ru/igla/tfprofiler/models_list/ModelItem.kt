package ru.igla.tfprofiler.models_list

import org.tensorflow.lite.support.metadata.MetadataExtractor
import ru.igla.tfprofiler.core.ModelInfoConfig
import ru.igla.tfprofiler.core.ModelType

data class ModelItem(
    val id: Long,
    val modelType: ModelType,
    val modelInfoConfig: ModelInfoConfig
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