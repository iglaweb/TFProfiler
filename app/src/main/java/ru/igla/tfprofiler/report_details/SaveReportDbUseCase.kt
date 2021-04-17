package ru.igla.tfprofiler.report_details

import android.app.Application
import ru.igla.tfprofiler.core.ModelType
import ru.igla.tfprofiler.core.UseCase
import ru.igla.tfprofiler.db.*
import ru.igla.tfprofiler.reports_list.ListReportEntity
import ru.igla.tfprofiler.utils.DateUtils

class SaveReportDbUseCase(val application: Application) :
    UseCase<SaveReportDbUseCase.RequestValues, SaveReportDbUseCase.ResponseValue>() {

    private val roomReportDbController by lazy {
        RoomModelReportsDbController(
            AppDatabase.getInstance(application)
        )
    }

    override fun executeUseCase(requestValues: RequestValues): Resource<ResponseValue> {
        val data = requestValues.data
        if (data.hasInvalidValues()) { // sql cannot save Double.NaN
            return Resource.error("Failed to save record in db")
        }

        val dbModelReportItem = DbModelReportItem(
            modelId = data.modelConfig.tableId,
            createdAt = data.createdAt
        )

        val reportList = data.reportDelegateItems.map { item ->
            DbReportDelegateItem(
                idReportDelegate = 0,
                exception = item.exception ?: "",

                modelReportId = 0, //later set

                createdAt = DateUtils.getCurrentDateInMs(),

                device = item.device,
                threads = item.threads,
                xnnpack = item.useXnnpack,
                imageBatchCount = item.batchImageCount,

                fps = item.fps,
                memoryUsageMax = item.memoryUsageMax,
                memoryUsageMin = item.memoryUsageMin,

                modelInitTime = item.modelInitTime,

                meanTime = item.meanTime,
                stdTime = item.stdTime,
                percentileTime99 = item.percentile99Time,

                minTime = item.minTime,
                maxTime = item.maxTime,

                interferenceRuns = item.inference,
                warmupRuns = item.warmupRuns
            )
        }

        val modelEntity = DbModelItem(
            idModel = 0,
            modelType = ModelType.CUSTOM_TFLITE,
            title = "filename",
            inputWidth = 1,
            inputHeight = 1,
            modelPath = "modelPath",
            labelPath = "", //default
            source = "",
            details = "",
            modelFormat = data.modelConfig.modelFormat,
            colorSpace = data.modelConfig.colorSpace,
            inputShapeType = data.modelConfig.inputShapeType
        )

        val modelReportWithDelegates =
            ModelReportWithDelegates(dbModelReportItem, reportList, modelEntity)
        roomReportDbController.addReportItem(modelReportWithDelegates)
        return Resource.success(ResponseValue())
    }

    class RequestValues(val data: ListReportEntity) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}