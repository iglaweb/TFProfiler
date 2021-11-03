package ru.igla.tfprofiler.core.tflite

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.dnn.Dnn
import org.opencv.dnn.Net
import org.opencv.imgproc.Imgproc
import ru.igla.tfprofiler.core.domain.Device
import ru.igla.tfprofiler.core.domain.InputShapeType
import ru.igla.tfprofiler.core.jni.DnnModelExecutor
import ru.igla.tfprofiler.core.jni.JavaOpenCVDnnModelExecutor
import ru.igla.tfprofiler.models_list.domain.ModelConfig
import ru.igla.tfprofiler.tflite_runners.base.ModelOptions
import ru.igla.tfprofiler.utils.forEachNoIterator
import ru.igla.tfprofiler.utils.logI
import timber.log.Timber
import java.io.File


class OpenCVInterpreterWrapper(
    /** An instance of the driver class to run model inference with TensorFlow Lite. */
    private val dnnModelExecutor: DnnModelExecutor
) : AutoCloseable {

    fun recognize(bitmap: Bitmap) {
        val frame = Mat()
        Utils.bitmapToMat(bitmap, frame)
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB)

        dnnModelExecutor.executeModel(listOf(frame))
        frame.free()
    }

    override fun close() {
        dnnModelExecutor.deInit()
    }

    companion object {
        private fun getOutputLayerIdxs(net: Net): List<String> {
            val names: MutableList<String> = mutableListOf()
            val outLayers = net.unconnectedOutLayers.toList()
            val layersNames = net.layerNames
            outLayers.forEachNoIterator {
                names.add(
                    layersNames[it - 1]
                )
            }
            return names
        }

        /***
         * Consider short string path, due to limit
         * https://answers.opencv.org/question/236672/error-on-loading-onnx-model/
         */
        @Throws(Exception::class)
        fun createInterpreter(
            modelPath: String,
            modelConfig: ModelConfig,
            modelOptions: ModelOptions
        ): OpenCVInterpreterWrapper {
            return try {
                val isNHWC = modelConfig.inputShapeType == InputShapeType.NHWC
                val cuda = modelOptions.device == Device.GPU
                val channels = modelConfig.colorSpace.channels

                val opencvDnn = JavaOpenCVDnnModelExecutor(
                    modelPath,
                    cuda,
                    isNHWC,
                    channels,
                    modelConfig.inputSize.width,
                    modelConfig.inputSize.height
                )
                opencvDnn.init()
                OpenCVInterpreterWrapper(opencvDnn)
            } catch (e: Exception) {
                Timber.e(e)
                throw e
            }
        }

        /***
         * Load model with implicit opencv calls, as it loads correctly
         * otherwise error thrown Cannot determine an origin framework of files
         */
        private fun loadDnnNetCorrectly(modelPath: String): Net {
            return when (File(modelPath).extension) {
                "onnx" -> Dnn.readNetFromONNX(modelPath)
                "t7", "net" -> Dnn.readNetFromTorch(modelPath)
                "pb" -> Dnn.readNetFromTensorflow(modelPath)
                "caffemodel" -> Dnn.readNetFromCaffe(modelPath)
                else -> Dnn.readNet(modelPath)
            }
        }

        @Throws(Exception::class)
        fun createDnnModel(
            modelPath: String
        ): Net {
            // can throw exception if it was not loaded
            val dnnNet = loadDnnNetCorrectly(modelPath)
            logI { "DNN from ONNX was successfully loaded!" }
            logI { "OpenCV model was successfully read. Layer IDs: \n ${dnnNet.layerNames}" }
            logI {
                val layerNames = dnnNet.unconnectedOutLayersNames
                val outputLayerIdx = getOutputLayerIdxs(dnnNet)
                "Layer names: $layerNames, outputLayersIdx: $outputLayerIdx"
            }
            logI { "Net dump: " + dnnNet.dump() }
            return dnnNet
        }

        @Throws(Exception::class)
        fun testCreateDnnModel(
            modelPath: String
        ): Boolean {
            // can throw exception if it was not loaded
            val dnnNet = createDnnModel(modelPath)
            return !dnnNet.empty()
        }
    }
}
