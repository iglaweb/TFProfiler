package ru.igla.tfprofiler.tflite_runners.base

import android.graphics.Bitmap
import ru.igla.tfprofiler.tflite_runners.domain.ImageResult
import ru.igla.tfprofiler.utils.DateUtils
import ru.igla.tfprofiler.utils.logI
import java.util.*


object ImageBatchProcessing {

    class RecognitionBatch(
        val type: ProcessType,
        val results: List<ImageResult>,
        val startTime: Long,
        val endTime: Long
    ) {
        companion object {
            val EMPTY = RecognitionBatch(ProcessType.EMPTY, emptyList(), -1L, -1L)
        }
    }

    private lateinit var imageDetector: Recognizer<List<Bitmap>, List<ImageResult>>
    private var batchSize = 10

    private val imageQueue: Queue<Bitmap> = ArrayDeque()

    private val lock = Any()

    @JvmStatic
    fun init(
        eyeStateEstimatorHpe: Recognizer<List<Bitmap>, List<ImageResult>>,
        batchSize: Int,
    ) {
        this.imageDetector = eyeStateEstimatorHpe
        this.batchSize = batchSize
    }

    @JvmStatic
    fun addImage(src: Bitmap): Boolean {
        val dst: Bitmap = src.copy(src.config, src.isMutable)
        imageQueue.add(dst)
        while (imageQueue.size > batchSize) {
            imageQueue.poll()?.apply {
                if (!isRecycled) {
                    recycle()
                }
            }
        }
        return imageQueue.size < batchSize
    }

    private fun clearBuffer() {
        while (imageQueue.isNotEmpty()) {
            imageQueue.poll()?.apply {
                if (!isRecycled) {
                    recycle()
                }
            }
        }
    }

    fun recognizeBatch(): RecognitionBatch {
        if (imageQueue.size < batchSize) {
            return RecognitionBatch.EMPTY
        }

        val copyImageList: MutableList<Bitmap> = mutableListOf()
        for (image in imageQueue) {
            copyImageList.add(image)
        }

        logI { "Detect batch" }
        val timeStart = DateUtils.getCurrentDateInMs()
        val ret = imageDetector.runInference(copyImageList)
        copyImageList.clear()
        val timeEnd = DateUtils.getCurrentDateInMs()
        logI { "Recognized eye batch in ${timeEnd - timeStart} ms" }

        synchronized(lock) {
            clearBuffer()
        }
        return RecognitionBatch(ProcessType.PROCESSED, ret, timeStart, timeEnd)
    }
}