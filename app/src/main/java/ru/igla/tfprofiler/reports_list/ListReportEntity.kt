package ru.igla.tfprofiler.reports_list

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.igla.tfprofiler.core.ModelType
import ru.igla.tfprofiler.models_list.domain.ModelConfig
import ru.igla.tfprofiler.utils.forEachNoIterator

@Parcelize
data class ListReportEntity(

    var idReport: Long = 0,
    var createdAt: Long,

    val modelType: ModelType,
    val modelName: String,
    val modelConfig: ModelConfig,

    val reportDelegateItems: List<ReportDelegateItem>

) : Parcelable {
    /**
     * Check whether it contains invalid values
     */
    fun hasInvalidValues(): Boolean {
        reportDelegateItems.forEachNoIterator {
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