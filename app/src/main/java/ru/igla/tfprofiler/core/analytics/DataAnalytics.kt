package ru.igla.tfprofiler.core.analytics

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import kotlin.math.max
import kotlin.math.min

class DataAnalytics {

    companion object {
        const val INVALID_TIME = -1L
        const val INVALID_MEMORY = -1L
    }

    val computeFps: ComputeFps by lazy { ComputeFps() }

    val statistics: DescriptiveStatistics by lazy {
        DescriptiveStatistics().apply {
            windowSize = 1000
        }
    }

    var fps = 0.0

    var exception: Exception? = null

    @JvmField
    var initializationTime: Long = -1L

    var memoryUsageMin = -1L
    var memoryUsageMax = -1L

    var interferenceTimeMin = -1L
    var interferenceTimeMax = -1L

    var interferenceRuns = 0
    var warmupRuns = 0

    fun clear() {
        exception = null

        initializationTime = 0L
        fps = 0.0
        interferenceRuns = 0

        memoryUsageMin = 0L
        memoryUsageMax = 0L

        interferenceTimeMin = 0L
        interferenceTimeMax = 0L

        computeFps.clear()
        statistics.clear()
    }

    fun setMemoryUsage(memoryUsageBytes: Long) {
        if (memoryUsageBytes >= 0) {
            memoryUsageMin = if (memoryUsageMin == -1L) memoryUsageBytes else min(
                memoryUsageMin,
                memoryUsageBytes
            )
            memoryUsageMax = if (memoryUsageMax == -1L) memoryUsageBytes else max(
                memoryUsageMax,
                memoryUsageBytes
            )
        }
    }

    fun incrementRuns() {
        interferenceRuns++
    }

    fun setInterferenceTime(processingTimeMs: Long) {
        if (processingTimeMs >= 0) {
            interferenceTimeMin = if (interferenceTimeMin == -1L) processingTimeMs else
                min(
                    interferenceTimeMin,
                    processingTimeMs
                )
            interferenceTimeMax = if (interferenceTimeMax == -1L) processingTimeMs else
                max(
                    interferenceTimeMax,
                    processingTimeMs
                )
        }
    }
}