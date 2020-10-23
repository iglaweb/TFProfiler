package ru.igla.tfprofiler.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

const val MODEL_REPORTS_TABLE = "model_reports"

@Entity(
    tableName = MODEL_REPORTS_TABLE
)
@Parcelize
class DbModelReportItem(
    @ColumnInfo(name = "id_model_report")
    @PrimaryKey(autoGenerate = true)
    var idModelReport: Long = 0,

    @ColumnInfo(name = "model_id")
    val modelId: Long,

    @ColumnInfo(name = "created_at")
    var createdAt: Long

) : Parcelable