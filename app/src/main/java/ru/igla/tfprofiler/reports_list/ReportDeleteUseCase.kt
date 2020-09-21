package ru.igla.tfprofiler.reports_list

import android.app.Application
import ru.igla.tfprofiler.core.UseCase
import ru.igla.tfprofiler.db.AppDatabase
import ru.igla.tfprofiler.db.RoomModelReportsDbController

class ReportDeleteUseCase(val application: Application) :
    UseCase<ReportDeleteUseCase.RequestValues,
            ReportDeleteUseCase.ResponseValue>() {

    private val roomReportDbController by lazy {
        RoomModelReportsDbController(
            AppDatabase.getInstance(application)
        )
    }

    override fun executeUseCase(requestValues: RequestValues): Resource<ResponseValue> {
        roomReportDbController.deleteReport(requestValues.data)
        val responseValue = ResponseValue()
        return Resource.success(responseValue)
    }

    class RequestValues(val data: List<ListReportEntity>) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}