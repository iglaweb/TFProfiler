package ru.igla.tfprofiler.reports_list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.igla.tfprofiler.core.UseCase
import ru.igla.tfprofiler.utils.ExceptionHandler


class ReportListViewModel(application: Application) : AndroidViewModel(application) {

    private val exceptionHandler = ExceptionHandler { _, _ -> }

    val reportsLiveData: LiveData<List<ListReportEntity>> by lazy { resolveDbReports() }

    private val modelsListUseCase by lazy {
        ReportsListUseCase(application)
    }

    private val modelDeleteUseCase by lazy {
        ReportDeleteUseCase(application)
    }

    private fun resolveDbReports(): LiveData<List<ListReportEntity>> {
        val useCaseResponse: UseCase.Resource<ReportsListUseCase.ResponseValue> =
            modelsListUseCase.executeUseCase(ReportsListUseCase.RequestValues())
        val response: ReportsListUseCase.ResponseValue? = useCaseResponse.data
        return response?.data!!
    }

    fun deleteReport(item: ListReportEntity) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            modelDeleteUseCase.executeUseCase(ReportDeleteUseCase.RequestValues(listOf(item)))
        }
    }
}