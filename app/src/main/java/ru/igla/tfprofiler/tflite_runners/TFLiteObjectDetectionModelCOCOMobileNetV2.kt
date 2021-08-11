package ru.igla.tfprofiler.tflite_runners

import android.graphics.RectF
import ru.igla.tfprofiler.tflite_runners.base.ImageBatchProcessing.ImageResult
import ru.igla.tfprofiler.tflite_runners.base.TFLiteObjectDetectionAPIModelBase
import ru.igla.tfprofiler.tflite_runners.domain.Recognition
import java.util.*
import kotlin.math.min

/**
 * https://tfhub.dev/google/object_detection/mobile_object_localizer_v1/1
 *
 *
 * This model uses a MobileNet V2 backbone with a 0.75 width-multiplier, and a SSDLite detection head.
 *
 *
 * Use case:
 * This model is suitable for localizing the most prominent objects in an image.
 *
 *
 * Limitations:
 * This is a class-agnostic object detector. Object classification is not provided.
 * This model may not perform well on very small objects.
 */
class TFLiteObjectDetectionModelCOCOMobileNetV2 :
    TFLiteObjectDetectionAPIModelBase<ImageResult>() {
    // outputLocations: array of shape [Batchsize, NUM_DETECTIONS,4]
    // contains the location of detected boxes
    private lateinit var outputLocations: Array<Array<FloatArray>>

    // outputClasses: array of shape [Batchsize, NUM_DETECTIONS]
    // contains the classes of detected boxes
    private lateinit var outputClasses: Array<FloatArray>

    // outputScores: array of shape [Batchsize, NUM_DETECTIONS]
    // contains the scores of detected boxes
    private lateinit var outputScores: Array<FloatArray>

    // numDetections: array of shape [Batchsize]
    // contains the number of detected boxes
    private lateinit var numDetections: FloatArray

    override fun prepareOutputs(): Map<Int, Any> {
        outputLocations = Array(1) { Array(NUM_DETECTIONS) { FloatArray(4) } }
        outputClasses = Array(1) { FloatArray(NUM_DETECTIONS) }
        outputScores = Array(1) { FloatArray(NUM_DETECTIONS) }
        numDetections = FloatArray(1)
        val outputMap: MutableMap<Int, Any> = HashMap()
        outputMap[0] = outputLocations
        outputMap[1] = outputClasses
        outputMap[2] = outputScores
        outputMap[3] = numDetections
        return outputMap
    }

    override fun getDetections(outputMap: MutableMap<Int, Any>): List<ImageResult> {
        // Show the best detections.
        // after scaling them back to the input size.

        // You need to use the number of detections from the output and not the NUM_DETECTONS variable declared on top
        // because on some models, they don't always output the same total number of detections
        // For example, your model's NUM_DETECTIONS = 20, but sometimes it only outputs 16 predictions
        // If you don't use the output's numDetections, you'll get nonsensical data
        val numDetectionsOutput = min(
            NUM_DETECTIONS,
            numDetections[0].toInt()
        ) // cast from float to integer, use min for safety
        val recognitions: MutableList<Recognition> = ArrayList(numDetectionsOutput)
        for (i in 0 until numDetectionsOutput) {
            val confidence = outputScores[0][i]
            if (confidence > MINIMUM_CONFIDENCE_TF_OD_API) {
                val detection = RectF(
                    outputLocations[0][i][1] * mInputSize.width,
                    outputLocations[0][i][0] * mInputSize.height,
                    outputLocations[0][i][3] * mInputSize.width,
                    outputLocations[0][i][2] * mInputSize.height
                )
                // SSD Mobilenet V1 Model assumes class 0 is background class
                // in label file and class labels start from 1 to number_of_classes+1,
                // while outputClasses correspond to class index from 0 to number_of_classes
                recognitions.add(
                    Recognition(
                        "" + i,
                        "Entity",
                        outputScores[0][i],
                        detection
                    )
                )
            }
        }
        return listOf(ImageResult(recognitions))
    }

    companion object {
        private const val MINIMUM_CONFIDENCE_TF_OD_API = 0.5f

        // Only return this many results.
        private const val NUM_DETECTIONS = 100
    }
}