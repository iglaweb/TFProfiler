package ru.igla.tfprofiler.db

interface DbModelsController {

    fun getModels(): List<DbModelItem>

    fun insertModel(modelItem: DbModelItem): Long

    fun deleteModel(modelId: Long): Int
}