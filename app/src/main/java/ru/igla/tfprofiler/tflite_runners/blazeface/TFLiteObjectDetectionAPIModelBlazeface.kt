package ru.igla.tfprofiler.tflite_runners.blazeface

import android.graphics.RectF
import ru.igla.tfprofiler.tflite_runners.base.TFLiteImageDetectAPIModelBase
import ru.igla.tfprofiler.tflite_runners.blazeface.ssd.Keypoint
import ru.igla.tfprofiler.tflite_runners.blazeface.ssd.SingleShotMultiBoxDetector
import ru.igla.tfprofiler.tflite_runners.domain.ImRecognition
import ru.igla.tfprofiler.tflite_runners.domain.ImageResult
import ru.igla.tfprofiler.utils.logI
import java.util.*
import kotlin.math.min

/**
 * Wrapper for frozen detection models trained using the Tensorflow Object Detection API:
 * - https://github.com/tensorflow/models/tree/master/research/object_detection
 * where you can find the training code.
 *
 *
 * To use pretrained models in the API or convert to TF Lite models, please see docs for details:
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/detection_model_zoo.md
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/running_on_mobile_tensorflowlite.md#running-our-model-on-android
 */
class TFLiteObjectDetectionAPIModelBlazeface : TFLiteImageDetectAPIModelBase<ImageResult>() {

    private lateinit var boxesResult: Array<Array<FloatArray>>
    private lateinit var scoresResult: Array<Array<FloatArray>>

    private val ssd = SingleShotMultiBoxDetector()

    override fun prepareOutputs(): MutableMap<Int, Any> {
        val batchImageCount = modelOptions.numberOfInputImages
        boxesResult = Array(1) { Array(896 * batchImageCount) { FloatArray(16) } }
        scoresResult = Array(1) { Array(896 * batchImageCount) { FloatArray(1) } }
        val outputs = HashMap<Int, Any>()
        outputs[0] = boxesResult
        outputs[1] = scoresResult
        return outputs
    }

    private fun extractDetections(
        boxesResult: Array<Array<FloatArray>>,
        scoresResult: Array<Array<FloatArray>>
    ): List<ImRecognition> {
        // Calculate detections from model results
        val detectionList = ssd.process(boxesResult, scoresResult)
        if (detectionList.isEmpty()) {
            logI { "No detections" }
        } else {
            logI { "Detections: " + detectionList.size + " " + detectionList[0].score }
        }
        val detections: MutableList<ImRecognition> = ArrayList()
        for (detection in detectionList) {
            if (detection.score > THRESHOLD_DETECT) {
                val x = detection.xMin * inputSize.width
                val y = detection.yMin * inputSize.height
                val width = detection.width * inputSize.width
                val height = detection.height * inputSize.height
                val keypoints = detection.keypoints
                val output: MutableList<Keypoint> = ArrayList()
                for (keypoint in keypoints) {
                    output.add(
                        Keypoint(
                            keypoint.x * inputSize.width,
                            keypoint.y * inputSize.height
                        )
                    )
                }
                val rec = ImRecognition(
                    "1",
                    "Face",
                    detection.score,
                    RectF(
                        x,
                        y,
                        min(x + width - 1f, inputSize.width - 1f),
                        min(y + height - 1f, inputSize.height - 1f)
                    ),
                    output
                )
                detections.add(rec)
            }
        }
        return detections
    }

    override fun getDetections(outputMap: Map<Int, Any>): List<ImageResult> {
        val batchImageCount = modelOptions.numberOfInputImages
        if (batchImageCount == 1) {
            val detections = extractDetections(boxesResult, scoresResult)
            return listOf(ImageResult(detections))
        }
        val arr = boxesResult[0]
        val batchCount = arr.size / batchImageCount
        val len = arr.size
        val imageResults: MutableList<ImageResult> = ArrayList()
        var i = 0
        while (i < len - batchCount + 1) {
            val boxArr = boxesResult[0]
            val box = boxArr.copyOfRange(i, i + batchCount)
            val scaledBoxes = arrayOf(box)
            val scoreArr = scoresResult[0]
            val score = scoreArr.copyOfRange(i, i + batchCount)
            val scaledScores = arrayOf(score)
            val detections = extractDetections(scaledBoxes, scaledScores)
            imageResults.add(ImageResult(detections))
            i += batchCount
        }
        return imageResults
    }

    companion object {
        // Minimum detection confidence to track a detection.
        private const val THRESHOLD_DETECT = 0.1f
    }
}