package ru.igla.tfprofiler.text_track

import android.os.SystemClock
import ru.igla.tfprofiler.core.ResolveStats
import ru.igla.tfprofiler.core.analytics.StatisticsEstimator
import ru.igla.tfprofiler.models_list.domain.ModelEntity
import ru.igla.tfprofiler.prefs.IPreferenceManager
import ru.igla.tfprofiler.tflite_runners.base.Classifier
import ru.igla.tfprofiler.tflite_runners.base.ModelOptions
import ru.igla.tfprofiler.tflite_runners.domain.TextRecognition
import ru.igla.tfprofiler.utils.lazyNonSafe
import ru.igla.tfprofiler.utils.logI

class TextRunInterferenceCase(
    statisticsEstimator: StatisticsEstimator,
    preferenceManager: IPreferenceManager
) {

    private val resolveStats by lazyNonSafe {
        ResolveStats(
            statisticsEstimator,
            preferenceManager
        )
    }

    @Throws(java.lang.Exception::class)
    fun runTextInterference(
        detector: Classifier<String, List<TextRecognition>>,
        modelEntity: ModelEntity,
        selectedModelOptions: ModelOptions,
        data: String
    ): TextOutResult {

        modelEntity.let {
            val startTime = SystemClock.uptimeMillis()
            val results = detector.runInference(data)
            val lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime
            logI { "Results = " + results.size }

            val statOutResult =
                resolveStats.resolveStats(selectedModelOptions, lastProcessingTimeMs)
            return TextOutResult(
                results,
                statOutResult
            )
        }
    }
}


