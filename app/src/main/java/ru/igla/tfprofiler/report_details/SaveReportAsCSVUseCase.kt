package ru.igla.tfprofiler.report_details

import android.content.Context
import de.siegmar.fastcsv.writer.CsvWriter
import ru.igla.tfprofiler.core.UseCase
import ru.igla.tfprofiler.media_track.MediaPathProvider
import ru.igla.tfprofiler.reports_list.ListReportEntity
import java.io.File
import java.nio.charset.StandardCharsets

class SaveReportAsCSVUseCase(val context: Context) :
    UseCase<SaveReportAsCSVUseCase.RequestValues, SaveReportAsCSVUseCase.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Resource<ResponseValue> {
        val csvPath = MediaPathProvider.getRootPath(context) + "/model_report.csv"
        val file = File(csvPath)
        val csvWriter = CsvWriter()
        csvWriter.append(file, StandardCharsets.UTF_8).use { csvAppender ->
            // header
            csvAppender.appendLine(
                "Model type",
                "Input size",
                "Type",
                "Device",
                "Threads",
                "XNNPACK",
                "FPS",
                "Max memory",
                "Min memory",
                "Model Init time (ms)",
                "Mean time (ms)",
                "Std time (ms)",
                "99th Percentile",
                "Min time",
                "Max time",
                "Total runs",
                "Warmup runs",
                "Exception"
            )

            val data = requestValues.reportEntity
            val modelConfig = requestValues.reportEntity.modelConfig
            for (item in data.reportDelegateItems) {
                csvAppender.appendLine(
                    data.modelType.id,
                    "" + modelConfig.inputWidth + "x" + modelConfig.inputHeight,
                    modelConfig.quantizedStr(),

                    item.device.name,
                    item.threads.toString(),
                    item.useXnnpack.toString(),

                    item.fps.toString(),
                    item.memoryUsageMax.toString(),
                    item.memoryUsageMin.toString(),

                    item.modelInitTime.toString(),

                    item.meanTime.toString(),
                    item.stdTime.toString(),
                    item.percentile99Time.toString(),

                    item.minTime.toString(),
                    item.maxTime.toString(),

                    item.inference.toString(),
                    item.warmupRuns.toString(),

                    item.exception ?: ""
                )
            }
            csvAppender.endLine()
        }
        val responseValue = ResponseValue(csvPath)
        return Resource.success(responseValue)
    }

    class RequestValues(val reportEntity: ListReportEntity) : UseCase.RequestValues
    class ResponseValue(val filePath: String) : UseCase.ResponseValue
}