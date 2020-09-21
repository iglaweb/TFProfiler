package ru.igla.tfprofiler.db

import androidx.lifecycle.LiveData
import ru.igla.tfprofiler.reports_list.ListReportEntity
import ru.igla.tfprofiler.utils.forEachNoIterator

class RoomModelReportsDbController(private val appDatabase: AppDatabase) : DbReportsController {

    override fun getReports(): LiveData<List<ModelReportWithDelegates>> {
        return appDatabase.modelReportsDao.getReportWithDelegates()
    }

    override fun addReportItem(params: ModelReportWithDelegates): Boolean {
        val id = appDatabase.modelReportsDao.insertReport(params.modelReportItem)
        params.reportDelegateItems.forEachNoIterator { item ->
            item.modelReportId = id
        }
        return appDatabase.modelReportsDao.insertReports(params.reportDelegateItems).isNotEmpty()
    }

    override fun deleteReport(list: List<ListReportEntity>) {
        appDatabase.runInTransaction {
            list.forEachNoIterator { item ->
                appDatabase.modelReportsDao.deleteReportById(item.idReport)
                item.reportDelegateItems.forEachNoIterator { delegate ->
                    appDatabase.modelReportsDao.deleteReportDelegateById(delegate.idReportDelegate)
                }
            }
        }
    }
}