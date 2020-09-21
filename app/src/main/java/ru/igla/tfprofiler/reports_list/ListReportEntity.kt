package ru.igla.tfprofiler.reports_list

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.igla.tfprofiler.core.ModelType

@Parcelize
data class ListReportEntity(

    var idReport: Long = 0,
    var createdAt: Long,

    val modelType: ModelType,
    val inputSize: Int,
    val quantized: Boolean,

    val reportDelegateItems: List<ReportDelegateItem>

) : Parcelable {
    /**
     * Check whether it contains invalid values
     */
    fun hasInvalidValues(): Boolean {
        reportDelegateItems.forEach {
            if (it.meanTime.isNaN()) {
                return true
            }
            if (it.stdTime.isNaN()) {
                return true
            }
            if (it.percentile99Time.isNaN()) {
                return true
            }
        }
        return false
    }
}