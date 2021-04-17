package ru.igla.tfprofiler.core.tflite

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.dnn.Net
import org.opencv.imgproc.Imgproc
import ru.igla.tfprofiler.core.Device
import ru.igla.tfprofiler.core.InputShapeType
import ru.igla.tfprofiler.core.Timber
import ru.igla.tfprofiler.core.jni.DnnModelExecutor
import ru.igla.tfprofiler.models_list.ModelConfig
import ru.igla.tfprofiler.tflite_runners.base.ModelOptions
import ru.igla.tfprofiler.utils.forEachNoIterator


class OpenCVInterpreterWrapper(
    /** An instance of the driver class to run model inference with TensorFlow Lite. */
    private val dnnModelExecutor: DnnModelExecutor
) : AutoCloseable {

    fun recognize(bitmap: Bitmap) {
        val frame = Mat()
        Utils.bitmapToMat(bitmap, frame)
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB)

        dnnModelExecutor.executeModel(longArrayOf(frame.nativeObjAddr))
        frame.free()
    }

    override fun close() {
        dnnModelExecutor.deInit()
    }

    companion object {
        private fun getOutputNames(net: Net): List<String> {
            val names: MutableList<String> = ArrayList()
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
                val dnnModelExecutor = DnnModelExecutor()
                val isNHWC = modelConfig.inputShapeType == InputShapeType.NHWC
                val cuda = modelOptions.device == Device.GPU
                val channels = modelConfig.colorSpace.channels
                dnnModelExecutor.init(
                    modelPath,
                    isNHWC,
                    cuda,
                    channels,
                    modelConfig.inputWidth,
                    modelConfig.inputHeight
                )
                OpenCVInterpreterWrapper(dnnModelExecutor)
            } catch (e: Exception) {
                Timber.e(e)
                throw e
            }
        }

        fun createTestInterpeter(
            modelPath: String
        ): OpenCVInterpreterWrapper {
            try {
                return OpenCVInterpreterWrapper(
                    DnnModelExecutor()
                )
            } catch (e: Exception) {
                Timber.e(e)
                throw e
            }
        }
    }
}
