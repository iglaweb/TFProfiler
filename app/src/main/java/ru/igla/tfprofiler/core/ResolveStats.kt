package ru.igla.tfprofiler.core

import ru.igla.tfprofiler.core.analytics.StatisticsEstimator
import ru.igla.tfprofiler.prefs.IPreferenceManager
import ru.igla.tfprofiler.text_track.StatOutResult
import ru.igla.tfprofiler.tflite_runners.base.ModelOptions

class ResolveStats(
    private val statisticsEstimator: StatisticsEstimator,
    private val preferenceManager: IPreferenceManager
) {

    fun resolveStats(
        selectedModelOptions: ModelOptions,
        lastProcessingTimeMs: Long
    ): StatOutResult? {
        statisticsEstimator.incrementFrameNumber(selectedModelOptions)

        // warmup run check
        val prefWarmupRuns = preferenceManager.defaultPrefs.warmupRuns
        val frameNumber = statisticsEstimator.getFrameNumber(selectedModelOptions)
        if (frameNumber < prefWarmupRuns) return null

        statisticsEstimator.setInterferenceTime(selectedModelOptions, lastProcessingTimeMs)
        val fps = statisticsEstimator.calcFps(selectedModelOptions)
        val memoryUsage = statisticsEstimator.appMemoryEstimator.getMemory()
        statisticsEstimator.setMemoryUsage(selectedModelOptions, memoryUsage)

        val descriptiveStatistics =
            statisticsEstimator.getStats(selectedModelOptions).statistics
        val std = descriptiveStatistics.standardDeviation
        val mean = descriptiveStatistics.mean

        val initTime = statisticsEstimator.getStats(selectedModelOptions).initializationTime

        return StatOutResult(
            initTime,
            lastProcessingTimeMs,
            mean,
            std,
            fps,
            memoryUsage
        )
    }
}