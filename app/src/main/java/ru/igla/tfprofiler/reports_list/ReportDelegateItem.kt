package ru.igla.tfprofiler.reports_list

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.igla.tfprofiler.core.Device

@Parcelize
class ReportDelegateItem(
    val idReportDelegate: Long = 0L,

    val exception: String?,

    val device: Device,
    val threads: Int,
    val useXnnpack: Boolean,
    val batchImageCount: Int,

    val fps: Double,
    val memoryUsageMin: Long,
    val memoryUsageMax: Long,

    val modelInitTime: Long,

    val meanTime: Double,
    val stdTime: Double,
    val percentile99Time: Double,

    val minTime: Long,
    val maxTime: Long,

    val inference: Int,
    val warmupRuns: Int
) : Parcelable {
    fun getDeviceConfigStr(): String {
        val threadStr = if (device == Device.CPU) (if (threads == 1) {
            " $threads Thread"
        } else {
            " $threads Threads"
        }) else ""
        return "" + this.device + threadStr
    }
}