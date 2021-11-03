package ru.igla.tfprofiler.media_track

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.SystemClock
import ru.igla.tfprofiler.core.ResolveStats
import ru.igla.tfprofiler.core.analytics.StatisticsEstimator
import ru.igla.tfprofiler.models_list.domain.ModelEntity
import ru.igla.tfprofiler.prefs.IPreferenceManager
import ru.igla.tfprofiler.tflite_runners.base.ImageBatchProcessing
import ru.igla.tfprofiler.tflite_runners.base.ModelOptions
import ru.igla.tfprofiler.tflite_runners.base.ProcessType
import ru.igla.tfprofiler.tflite_runners.base.Recognizer
import ru.igla.tfprofiler.tflite_runners.domain.ImageResult
import ru.igla.tfprofiler.utils.ImageUtils
import ru.igla.tfprofiler.utils.lazyNonSafe
import ru.igla.tfprofiler.utils.logI

class BitmapRunInterferenceCase(
    private val statisticsEstimator: StatisticsEstimator,
    preferenceManager: IPreferenceManager,
    private val recognizeImageCallback: RecognizeImageCallback
) {

    private val resolveStats by lazyNonSafe {
        ResolveStats(
            statisticsEstimator,
            preferenceManager
        )
    }

    @Throws(java.lang.Exception::class)
    fun runImageInterference(
        detector: Recognizer<List<Bitmap>, List<ImageResult>>,
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

            val cropWidth: Int = model.modelConfig.inputSize.width
            val cropHeight: Int = model.modelConfig.inputSize.height

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
                    val results = detector.runInference(listOf(croppedBitmap))
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

                val statOutResult =
                    resolveStats.resolveStats(selectedModelOptions, lastProcessingTimeMs)
                        ?: return

                recognizeImageCallback.startRecognizeImage(
                    BitmapResult(
                        previewWidth,
                        previewHeight,
                        croppedBitmap.width,
                        croppedBitmap.height,
                        statOutResult
                    )
                )
            }
        }
    }
}