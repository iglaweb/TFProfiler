package ru.igla.tfprofiler.db

import androidx.room.*

@Dao
interface ModelsDao {
    @Transaction
    @Query("SELECT * FROM $MODEL_TABLE")
    fun getModels(): List<DbModelItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertModel(modelItem: DbModelItem): Long

    @Query("DELETE FROM $MODEL_TABLE WHERE id_model = :idModel")
    fun deleteModel(idModel: Long): Int
}