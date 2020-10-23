package ru.igla.tfprofiler.core.analytics

import android.content.Context
import ru.igla.tfprofiler.models_list.ModelEntity
import ru.igla.tfprofiler.reports_list.ListReportEntity
import ru.igla.tfprofiler.reports_list.ReportDelegateItem
import ru.igla.tfprofiler.tflite_runners.base.ModelOptions
import ru.igla.tfprofiler.utils.DateUtils

class StatisticsEstimator(context: Context) {
    @JvmField
    val appMemoryEstimator = AppMemoryEstimator(context.applicationContext)

    private val dataAnalyticsMap: MutableMap<ModelOptions, DataAnalytics> = mutableMapOf()

    fun createReport(
        modelEntity: ModelEntity
    ): ListReportEntity {
        val reportItems = mutableListOf<ReportDelegateItem>()
        for ((key, value) in dataAnalyticsMap) {
            val noException = value.exception?.message.isNullOrEmpty()
            val item = ReportDelegateItem(

                exception = value.exception?.message,

                device = key.device,
                threads = key.numThreads,
                useXnnpack = key.useXnnpack,
                fps = value.fps,

                memoryUsageMin = value.memoryUsageMin,
                memoryUsageMax = value.memoryUsageMax,

                modelInitTime = value.initializationTime,

                meanTime = if (noException) value.statistics.mean else 0.0,
                stdTime = if (noException) value.statistics.standardDeviation else 0.0,
                percentile99Time = if (noException) value.statistics.getPercentile(99.0) else 0.0,

                minTime = value.interferenceTimeMin,
                maxTime = value.interferenceTimeMax,

                inference = value.interferenceRuns,
                warmupRuns = value.warmupRuns
            )
            reportItems.add(item)
        }

        return ListReportEntity(
            idReport = 1L,
            createdAt = DateUtils.getCurrentDateInMs(),

            modelName = modelEntity.name,
            modelType = modelEntity.modelType,
            reportDelegateItems = reportItems,

            modelConfig = modelEntity.modelConfig
        )
    }

    fun clearStats(modelOptions: ModelOptions) {
        getStats(modelOptions).clear()
    }

    fun setInitTime(modelOptions: ModelOptions, initTime: Long) {
        getStats(modelOptions).initializationTime = initTime
    }

    fun setMemoryUsage(modelOptions: ModelOptions, memoryUsage: Long) {
        getStats(modelOptions).setMemoryUsage(memoryUsage)
    }

    fun getStats(modelOptions: ModelOptions): DataAnalytics {
        val key = dataAnalyticsMap[modelOptions] ?: DataAnalytics()
        dataAnalyticsMap[modelOptions] = key
        return key
    }

    fun getInitTime(modelOptions: ModelOptions): Long {
        return getStats(modelOptions).initializationTime
    }

    fun calcFps(modelOptions: ModelOptions): Double {
        getStats(modelOptions).apply {
            fps = computeFps.calcFps()
            return fps
        }
    }

    fun setWarmupRuns(modelOptions: ModelOptions, warmupRuns: Int) {
        getStats(modelOptions).warmupRuns = warmupRuns
    }

    fun setInterferenceTime(modelOptions: ModelOptions, processTime: Long) {
        getStats(modelOptions).apply {
            statistics.addValue(processTime.toDouble())
            setInterferenceTime(processTime)
        }
    }

    fun getFrameNumber(modelOptions: ModelOptions): Int {
        return getStats(modelOptions).interferenceRuns
    }

    fun incrementFrameNumber(modelOptions: ModelOptions) {
        getStats(modelOptions).apply {
            incrementRuns()
        }
    }

    fun setError(modelOptions: ModelOptions, e: Exception) {
        getStats(modelOptions).exception = e
    }
}