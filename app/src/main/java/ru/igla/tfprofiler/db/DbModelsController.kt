package ru.igla.tfprofiler.db

import ru.igla.tfprofiler.core.domain.ColorSpace
import ru.igla.tfprofiler.core.domain.InputShapeType
import ru.igla.tfprofiler.core.domain.ModelOptimizedType

interface DbModelsController {

    fun getModels(): List<DbModelItem>

    fun insertModel(modelItem: DbModelItem): Long

    fun deleteModel(modelId: Long): Int

    fun updateModel(
        modelId: Long,
        inputWidth: Int,
        inputHeight: Int,
        modelFormat: ModelOptimizedType,
        colorSpace: ColorSpace,
        inputShapeType: InputShapeType
    )
}