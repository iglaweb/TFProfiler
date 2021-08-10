package ru.igla.tfprofiler.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import ru.igla.tfprofiler.core.ColorSpace
import ru.igla.tfprofiler.core.InputShapeType
import ru.igla.tfprofiler.core.ModelOptimizedType
import ru.igla.tfprofiler.core.ModelType

const val MODEL_TABLE = "models"

@Entity(
    tableName = MODEL_TABLE
)
@Parcelize
class DbModelItem(
    @ColumnInfo(name = "id_model")
    @PrimaryKey(autoGenerate = true)
    var idModel: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "model_path")
    val modelPath: String,

    @ColumnInfo(name = "label_path")
    val labelPath: String,

    @ColumnInfo(name = "source")
    val source: String,

    @ColumnInfo(name = "details")
    val details: String,

    @ColumnInfo(name = "model_type")
    val modelType: ModelType,

    @ColumnInfo(name = "input_width")
    val inputWidth: Int,
    @ColumnInfo(name = "input_height")
    val inputHeight: Int,

    @ColumnInfo(name = "model_format")
    val modelFormat: ModelOptimizedType,

    @ColumnInfo(name = "color_space")
    val colorSpace: ColorSpace,

    @ColumnInfo(name = "input_shape_type")
    val inputShapeType: InputShapeType
) : Parcelable