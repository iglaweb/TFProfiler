package ru.igla.tfprofiler.db

import ru.igla.tfprofiler.core.ColorSpace
import ru.igla.tfprofiler.core.InputShapeType
import ru.igla.tfprofiler.core.ModelOptimizedType

class RoomModelsDbController(private val appDatabase: AppDatabase) : DbModelsController {
    override fun getModels(): List<DbModelItem> {
        return appDatabase.modelsDao.getModels()
    }

    override fun insertModel(modelItem: DbModelItem): Long {
        return appDatabase.modelsDao.insertModel(modelItem)
    }

    override fun deleteModel(modelId: Long): Int {
        return appDatabase.modelsDao.deleteModel(modelId)
    }

    override fun updateModel(
        modelId: Long,
        inputWidth: Int,
        inputHeight: Int,
        modelFormat: ModelOptimizedType,
        colorSpace: ColorSpace,
        inputShapeType: InputShapeType
    ) {
        return appDatabase.modelsDao.updateModel(
            modelId,
            inputWidth,
            inputHeight,
            modelFormat,
            colorSpace,
            inputShapeType
        )
    }
}