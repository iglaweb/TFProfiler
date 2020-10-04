package ru.igla.tfprofiler.media_track

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.SystemClock
import ru.igla.tfprofiler.core.analytics.StatisticsEstimator
import ru.igla.tfprofiler.env.ImageUtils
import ru.igla.tfprofiler.models_list.ModelEntity
import ru.igla.tfprofiler.prefs.AndroidPreferenceManager
import ru.igla.tfprofiler.tflite_runners.base.Classifier
import ru.igla.tfprofiler.tflite_runners.base.ImageRecognizer
import ru.igla.tfprofiler.tflite_runners.base.ModelOptions
import ru.igla.tfprofiler.utils.logI
import ru.igla.tfprofiler.video.TimestampBitmap

class RunInterferenceCase(
    private val statisticsEstimator: StatisticsEstimator,
    private val preferenceManager: AndroidPreferenceManager,
    private val recognizeImageCallback: RecgonizeImageCallback
) {

    @Throws(java.lang.Exception::class)
    fun runImageInterference(
        detector: ImageRecognizer<Classifier.Recognition>,
        modelEntity: ModelEntity,
        selectedModelOptions: ModelOptions,
        timestampBitmap: TimestampBitmap,
    ) {
        modelEntity.let { model ->
            val previewWidth = timestampBitmap.bitmap.width
            val previewHeight = timestampBitmap.bitmap.height

            val cropWidth: Int = model.inputWidth
            val cropHeight: Int = model.inputHeight

            val frameToCropTransform = ImageUtils.getTransformationMatrix(
                previewWidth, previewHeight,
                cropWidth, cropHeight,
                0,
                false,
                false
            )


            val croppedBitmap = Bitmap.createBitmap(cropWidth, cropHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(croppedBitmap)
            val rgbFrameBitmap = timestampBitmap.bitmap
            canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null)

            recognizeImageCallback.onPreview(croppedBitmap)

            val startTime = SystemClock.uptimeMillis()
            val results: List<Classifier.Recognition> = detector.recognizeImage(croppedBitmap)
            val lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime

            logI { "Results = " + results.size }

            statisticsEstimator.incrementFrameNumber(selectedModelOptions)

            // warmup run check
            val prefWarmupRuns = preferenceManager.defaultPrefs.warmupRuns
            val frameNumber = statisticsEstimator.getFrameNumber(selectedModelOptions)
            if (frameNumber < prefWarmupRuns) return

            statisticsEstimator.setInterferenceTime(selectedModelOptions, lastProcessingTimeMs)
            val fps = statisticsEstimator.calcFps(selectedModelOptions)
            val memoryUsage = statisticsEstimator.appMemoryEstimator.getMemory()
            statisticsEstimator.setMemoryUsage(selectedModelOptions, memoryUsage)

            val descriptiveStatistics =
                statisticsEstimator.getStats(selectedModelOptions).statistics
            val std = descriptiveStatistics.standardDeviation
            val mean = descriptiveStatistics.mean

            val initTime = statisticsEstimator.getStats(selectedModelOptions).initializationTime

            recognizeImageCallback.startRecognizeImage(
                BitmapResult(
                    previewWidth,
                    previewHeight,
                    croppedBitmap.width,
                    croppedBitmap.height,

                    initTime,
                    lastProcessingTimeMs,
                    mean,
                    std,
                    fps,
                    memoryUsage
                )
            )
        }
    }
}