package ru.igla.tfprofiler.report_details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.igla.tfprofiler.UseCase
import ru.igla.tfprofiler.reports_list.ListReportEntity
import ru.igla.tfprofiler.utils.ExceptionHandler
import ru.igla.tfprofiler.utils.sendValueIfNew


class ReportDetailsViewModel(application: Application) : AndroidViewModel(application) {

    val liveDataSaveDb = MutableLiveData<UseCase.Status>()

    val liveDataCsvReport = MutableLiveData<String>()

    private val exceptionHandler = ExceptionHandler { _, _ -> }

    fun saveReportCsv(data: ListReportEntity) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val useCaseHandler = SaveReportAsCSVUseCase(getApplication())
            val requestValue = SaveReportAsCSVUseCase.RequestValues(data)
            val response = useCaseHandler.executeUseCase(requestValue)
            if (response.status == UseCase.Status.SUCCESS) {
                response.data?.apply {
                    liveDataCsvReport.sendValueIfNew(
                        filePath
                    )
                }
            }
        }
    }

    fun saveReportDb(data: ListReportEntity) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val db = SaveReportDbUseCase(getApplication())
            val result = db.executeUseCase(SaveReportDbUseCase.RequestValues(data))
            liveDataSaveDb.sendValueIfNew(result.status)
        }
    }
}