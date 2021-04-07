package ru.igla.tfprofiler.media_track

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.SystemClock
import ru.igla.tfprofiler.core.analytics.StatisticsEstimator
import ru.igla.tfprofiler.env.ImageUtils
import ru.igla.tfprofiler.models_list.ModelEntity
import ru.igla.tfprofiler.prefs.AndroidPreferenceManager
import ru.igla.tfprofiler.tflite_runners.base.ImageBatchProcessing
import ru.igla.tfprofiler.tflite_runners.base.ImageRecognizer
import ru.igla.tfprofiler.tflite_runners.base.ModelOptions
import ru.igla.tfprofiler.tflite_runners.base.ProcessType
import ru.igla.tfprofiler.utils.logI

class RunInterferenceCase(
    private val statisticsEstimator: StatisticsEstimator,
    private val preferenceManager: AndroidPreferenceManager,
    private val recognizeImageCallback: RecgonizeImageCallback
) {

    @Throws(java.lang.Exception::class)
    fun runImageInterference(
        detector: ImageRecognizer<ImageBatchProcessing.ImageResult>,
        modelEntity: ModelEntity,
        selectedModelOptions: ModelOptions,
        bitmap: Bitmap
    ) {
        if (selectedModelOptions.numberOfInputImages > 1) {
            ImageBatchProcessing.init(detector, selectedModelOptions.numberOfInputImages)
        }

        modelEntity.let { model ->
            val previewWidth = bitmap.width
            val previewHeight = bitmap.height

            val cropWidth: Int = model.modelConfig.inputWidth
            val cropHeight: Int = model.modelConfig.inputHeight

            val frameToCropTransform = ImageUtils.getTransformationMatrix(
                previewWidth, previewHeight,
                cropWidth, cropHeight,
                0,
                false,
                false
            )

            val croppedBitmap = Bitmap.createBitmap(cropWidth, cropHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(croppedBitmap)
            canvas.drawBitmap(bitmap, frameToCropTransform, null)

            recognizeImageCallback.onPreview(croppedBitmap)

            val startTime = SystemClock.uptimeMillis()
            val ret: ImageBatchProcessing.RecognitionBatch =
                if (selectedModelOptions.numberOfInputImages == 1) {
                    val results = detector.recognizeImage(listOf(croppedBitmap))
                    ImageBatchProcessing.RecognitionBatch(
                        ProcessType.PROCESSED, results, -1, -1
                    )
                } else {
                    ImageBatchProcessing.addImage(croppedBitmap)
                    ImageBatchProcessing.recognizeBatch()
                }

            val lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime
            logI { "Results = " + ret.results.size }

            if (ret.type == ProcessType.PROCESSED) {
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
}