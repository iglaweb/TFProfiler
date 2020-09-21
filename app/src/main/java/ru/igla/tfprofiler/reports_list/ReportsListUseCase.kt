package ru.igla.tfprofiler.reports_list

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import ru.igla.tfprofiler.core.UseCase
import ru.igla.tfprofiler.db.AppDatabase
import ru.igla.tfprofiler.db.RoomModelReportsDbController
import ru.igla.tfprofiler.utils.DateUtils
import ru.igla.tfprofiler.utils.forEachNoIterator

class ReportsListUseCase(val application: Application) :
    UseCase<ReportsListUseCase.RequestValues,
            ReportsListUseCase.ResponseValue>() {

    private val roomReportDbController by lazy {
        RoomModelReportsDbController(
            AppDatabase.getInstance(application)
        )
    }

    override fun executeUseCase(requestValues: RequestValues): Resource<ResponseValue> {
        val data = roomReportDbController.getReports()

        val transformedLiveData: LiveData<List<ListReportEntity>> =
            Transformations.map(data) { dbItems ->
                val list = mutableListOf<ListReportEntity>()
                dbItems.forEachNoIterator { data ->

                    val modelItem = data.modelReportItem
                    val delegates = data.reportDelegateItems

                    val reportItems = mutableListOf<ReportDelegateItem>()
                    delegates.forEachNoIterator { item ->
                        val reportItem = ReportDelegateItem(
                            idReportDelegate = item.idReportDelegate,
                            exception = item.exception,
                            device = item.device,
                            threads = item.threads,
                            useXnnpack = item.xnnpack,
                            fps = item.fps,
                            memoryUsageMin = item.memoryUsageMin,
                            memoryUsageMax = item.memoryUsageMax,
                            modelInitTime = item.modelInitTime,
                            meanTime = item.meanTime,
                            stdTime = item.stdTime,
                            percentile99Time = item.percentileTime99,
                            minTime = item.minTime,
                            maxTime = item.maxTime,
                            inference = item.interferenceRuns,
                            warmupRuns = item.warmupRuns
                        )
                        reportItems.add(reportItem)
                    }

                    list.add(
                        ListReportEntity(
                            idReport = modelItem.idModelReport,
                            createdAt = DateUtils.getCurrentDateInMs(),
                            reportDelegateItems = reportItems,
                            modelType = modelItem.modelType,
                            inputSize = modelItem.inputSize,
                            quantized = modelItem.quantized
                        )
                    )
                }
                list
            }

        val responseValue = ResponseValue(transformedLiveData)
        return Resource.success(responseValue)
    }

    class RequestValues : UseCase.RequestValues
    class ResponseValue(val data: LiveData<List<ListReportEntity>>) : UseCase.ResponseValue
}