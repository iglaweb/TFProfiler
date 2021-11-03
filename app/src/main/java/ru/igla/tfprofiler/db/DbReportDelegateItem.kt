package ru.igla.tfprofiler.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import ru.igla.tfprofiler.core.domain.Device

const val DELEGATE_REPORTS_TABLE = "delegate_reports"

@Entity(
        tableName = DELEGATE_REPORTS_TABLE
)
@Parcelize
class DbReportDelegateItem(

    @ColumnInfo(name = "id_report_delegate")
        @PrimaryKey(autoGenerate = true)
        var idReportDelegate: Long = 0,

    @ColumnInfo(name = "exception")
        var exception: String,

    @ColumnInfo(name = "model_report_id")
        var modelReportId: Long,

    @ColumnInfo(name = "created_at")
        var createdAt: Long,

    @ColumnInfo(name = "device")
        val device: Device,
    @ColumnInfo(name = "threads")
        var threads: Int,
    @ColumnInfo(name = "xnnpack")
        val xnnpack: Boolean,

    @ColumnInfo(name = "imageBatchCount")
        val imageBatchCount: Int,

    @ColumnInfo(name = "fps")
        val fps: Double,

    @ColumnInfo(name = "memory_usage_min")
        val memoryUsageMin: Long,
    @ColumnInfo(name = "memory_usage_max")
        val memoryUsageMax: Long,

    @ColumnInfo(name = "model_init_time")
        val modelInitTime: Long,

    @ColumnInfo(name = "mean_time")
        val meanTime: Double,
    @ColumnInfo(name = "std_time")
        val stdTime: Double,
    @ColumnInfo(name = "percentile_time")
        val percentileTime99: Double,

    @ColumnInfo(name = "min_time")
        val minTime: Long,
    @ColumnInfo(name = "max_time")
        val maxTime: Long,

    @ColumnInfo(name = "interference_runs")
        val interferenceRuns: Int,
    @ColumnInfo(name = "warmup_runs")
        val warmupRuns: Int

) : Parcelable