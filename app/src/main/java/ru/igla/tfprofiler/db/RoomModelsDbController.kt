package ru.igla.tfprofiler.db

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
}