package ru.igla.tfprofiler.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import ru.igla.tfprofiler.core.ModelType

const val MODEL_REPORTS_TABLE = "model_reports"

@Entity(
    tableName = MODEL_REPORTS_TABLE
)
@Parcelize
class DbModelReportItem(
    @ColumnInfo(name = "id_model_report")
    @PrimaryKey(autoGenerate = true)
    var idModelReport: Long = 0,

    @ColumnInfo(name = "model_type")
    val modelType: ModelType,
    @ColumnInfo(name = "input_width")
    val inputWidth: Int,
    @ColumnInfo(name = "input_height")
    val inputHeight: Int,
    @ColumnInfo(name = "quantized")
    val quantized: Boolean

) : Parcelable