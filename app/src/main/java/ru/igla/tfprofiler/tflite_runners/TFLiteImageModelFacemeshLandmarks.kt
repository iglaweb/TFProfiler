package ru.igla.tfprofiler.tflite_runners

import android.graphics.RectF
import ru.igla.tfprofiler.core.ColorSpace
import ru.igla.tfprofiler.core.ops.BaseOpNormalizer
import ru.igla.tfprofiler.core.ops.OpNormalizer
import ru.igla.tfprofiler.core.tflite.TensorFlowUtils
import ru.igla.tfprofiler.tflite_runners.base.TFLiteImageDetectAPIModelBase
import ru.igla.tfprofiler.tflite_runners.blazeface.ssd.Keypoint
import ru.igla.tfprofiler.tflite_runners.domain.ImRecognition
import ru.igla.tfprofiler.tflite_runners.domain.ImageResult
import java.util.*

/***
 * https://google.github.io/mediapipe/solutions/face_mesh.html
 * Model: https://github.com/google/mediapipe/blob/master/mediapipe/modules/face_detection/face_detection_front.tflite
 */
class TFLiteImageModelFacemeshLandmarks : TFLiteImageDetectAPIModelBase<ImageResult>() {

    private lateinit var landmarkPoints: Array<Array<Array<FloatArray>>>
    private lateinit var result: Array<Array<Array<FloatArray>>>

    override fun prepareOutputs(): MutableMap<Int, Any> {
        val batchImageCount = modelOptions.numberOfInputImages
        landmarkPoints =
            Array(batchImageCount) { Array(1) { Array(1) { FloatArray(LANDMARKS_LEN) } } }
        result = Array(batchImageCount) { Array(1) { Array(1) { FloatArray(1) } } }
        val outputs = HashMap<Int, Any>()
        outputs[0] = landmarkPoints
        outputs[1] = result
        return outputs
    }

    override fun getNormalizer(isQuantized: Boolean, colorSpace: ColorSpace): OpNormalizer {
        return BaseOpNormalizer(isQuantized, 127.5f, 127.5f)
    }

    private fun extractDetections(
        result: Array<Array<FloatArray>>,
        landmarkPoints: Array<Array<FloatArray>>
    ): List<ImRecognition> {
        val score = result[0][0][0]
        val classScore = TensorFlowUtils.sigmoid(score)
        val detections: MutableList<ImRecognition> = ArrayList()

        if (classScore >= THRESHOLD_DETECT) {
            val output: MutableList<Keypoint> = ArrayList()
            var i = 0
            while (i < LANDMARKS_LEN - 3) {
                val x = landmarkPoints[0][0][i]
                val y = landmarkPoints[0][0][i + 1]
                output.add(
                    Keypoint(
                        x,
                        y
                    )
                )
                i += 3
            }
            val rec = ImRecognition(
                "1",
                "Landmarks",
                classScore,
                RectF(
                    0f,
                    0f,
                    inputSize.width - 1f,
                    inputSize.height - 1f
                ),
                output
            )
            detections.add(rec)
        }
        return detections
    }

    override fun getDetections(outputMap: Map<Int, Any>): List<ImageResult> {
        val batchImageCount = modelOptions.numberOfInputImages
        if (batchImageCount == 1) {
            val detections = extractDetections(result[0], landmarkPoints[0])
            return listOf(ImageResult(detections))
        }
        val imageResults: MutableList<ImageResult> = ArrayList()
        val len = result.size
        for (i in 0 until len) {
            val detections = extractDetections(result[i], landmarkPoints[i])
            imageResults.add(ImageResult(detections))
        }
        return imageResults
    }

    companion object {
        private const val THRESHOLD_DETECT = 0.2f
        private const val LANDMARKS_LEN = 468 * 3 //1404
    }
}