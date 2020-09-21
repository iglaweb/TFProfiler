package ru.igla.tfprofiler.db

import androidx.lifecycle.LiveData
import ru.igla.tfprofiler.reports_list.ListReportEntity

interface DbReportsController {
    fun getReports(): LiveData<List<ModelReportWithDelegates>>

    fun addReportItem(params: ModelReportWithDelegates): Boolean

    fun deleteReport(list: List<ListReportEntity>)
}