package ru.igla.tfprofiler.db

import androidx.room.*
import ru.igla.tfprofiler.core.ColorSpace
import ru.igla.tfprofiler.core.InputShapeType
import ru.igla.tfprofiler.core.ModelFormat

@Dao
interface ModelsDao {
    @Transaction
    @Query("SELECT * FROM $MODEL_TABLE")
    fun getModels(): List<DbModelItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertModel(modelItem: DbModelItem): Long

    @Query("DELETE FROM $MODEL_TABLE WHERE id_model = :idModel")
    fun deleteModel(idModel: Long): Int

    /**
     * Updating only amount and price
     * By order id
     */
    @Query("UPDATE $MODEL_TABLE SET input_width = :modelWidth, input_height = :modelHeight, color_space = :colorSpace, model_format = :modelFormat, input_shape_type = :inputShapeType WHERE id_model =:modelId")
    fun updateModel(
        modelId: Long,
        modelWidth: Int,
        modelHeight: Int,
        modelFormat: ModelFormat,
        colorSpace: ColorSpace,
        inputShapeType: InputShapeType
    )
}