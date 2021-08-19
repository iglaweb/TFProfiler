package ru.igla.tfprofiler.core.tflite

import android.content.Context
import android.os.Build
import org.tensorflow.lite.Delegate
import org.tensorflow.lite.HexagonDelegate
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate
import ru.igla.tfprofiler.core.*
import ru.igla.tfprofiler.models_list.ModelConfig
import ru.igla.tfprofiler.tflite_runners.base.ModelOptions
import ru.igla.tfprofiler.video.FileUtils
import timber.log.Timber
import java.nio.MappedByteBuffer

class TFInterpreterWrapper(
    /** An instance of the driver class to run model inference with TensorFlow Lite. */
    val interpreter: Interpreter,
    private val delegate: DelegateDecorator?
) : AutoCloseable {

    class DelegateDecorator(
        val delegate: Delegate,
        private val closeable: AutoCloseable
    ) : AutoCloseable {
        override fun close() {
            closeable.close()
        }
    }

    override fun close() {
        interpreter.close()
        delegate?.close()
    }

    companion object {
        //maybe device doesn't support Hexagon DSP execution, throw exception
        @Throws(UnsupportedOperationException::class, FailedCreateTFDelegate::class)
        private fun requestDelegate(
            context: Context,
            device: Device
        ): DelegateDecorator? {
            try {
                when (device) {
                    Device.HEXAGON -> {
                        //https://github.com/tensorflow/tensorflow/blob/master/tensorflow/lite/g3doc/performance/hexagon_delegate.md
                        //https://blog.tensorflow.org/2019/12/accelerating-tensorflow-lite-on-qualcomm.html
                        val delegate = HexagonDelegate(context)
                        return DelegateDecorator(delegate, delegate)
                    }
                    Device.GPU -> {
                        val gpuDelegateOptions = GpuDelegate.Options()
                        val delegate = GpuDelegate(gpuDelegateOptions)
                        return DelegateDecorator(delegate, delegate)
                    }
                    Device.CPU -> {
                        //no impl
                    }
                    Device.NNAPI -> {
                        // https://www.tensorflow.org/lite/performance/nnapi
                        // NNAPI is supported from API Level 27 (Android Oreo MR1),
                        // the support for operations improved significantly for API Level 28 (Android Pie) onwards
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val delegate = NnApiDelegate()
                            return DelegateDecorator(delegate, delegate)
                        }
                    }
                }
            } catch (e: UnsatisfiedLinkError) {
                //https://github.com/tensorflow/tensorflow/issues/43063
                //java.lang.UnsatisfiedLinkError: dlopen failed: library "libndk_translation_proxy_libGLESv3.so" not found
                reportError(e, device)
            } catch (e: Exception) { //UnsupportedOperationException
                reportError(e, device)
            }
            return null
        }

        private fun reportError(e: Throwable, device: Device) {
            if (e is UnsupportedOperationException) {
                throw FailedCreateTFDelegate(device, e.message ?: "")
            }
            e.message?.let { msg ->
                val err = when {
                    msg.contains("libOpenCL") -> {
                        FailedCreateTFDelegate(
                            device,
                            "This device does not support OpenCL\n$msg"
                        )
                    }
                    msg.contains("libGLES") -> {
                        FailedCreateTFDelegate(
                            device,
                            "This device does not support libGLES\n$msg"
                        )
                    }
                    else -> {
                        FailedCreateTFDelegate(device, msg)
                    }
                }
                throw err
            } ?: throw FailedCreateTFDelegate(device, e.message ?: "")
        }

        @JvmStatic
        @Throws(Exception::class)
        fun createTestInterpeter(
            context: Context,
            modelPath: String
        ): TFInterpreterWrapper {
            return createTfliteInterpreter(
                context,
                modelPath,
                ModelConfig(
                    -1,
                    Size(1, 1),
                    ModelOptimizedType.FLOATING,
                    ColorSpace.GRAYSCALE,
                    InputShapeType.NHWC
                ),
                ModelOptions(device = Device.CPU, numThreads = 1, useXnnpack = false)
            )
        }

        @Throws(
            Exception::class,
            FailedCreateTFDelegate::class,
            UnsupportedOperationException::class
        )
        fun createTfliteInterpreter(
            context: Context,
            modelPath: String,
            modelConfig: ModelConfig,
            modelOptions: ModelOptions
        ): TFInterpreterWrapper {
            return try {
                val device: Device = modelOptions.device
                val useXnnpack: Boolean = modelOptions.useXnnpack
                val inputImgCount = modelOptions.numberOfInputImages

                val options: Interpreter.Options = Interpreter.Options()
                    .setCancellable(true)
                val delegate = requestDelegate(context, device)
                if (delegate != null) {
                    options.addDelegate(delegate.delegate)
                }

                //Temporary not working. Seems it's fixed in https://github.com/tensorflow/tensorflow/issues/42056
                //Internal error: Failed to apply XNNPACK delegate: ModifyGraphWithDelegate is disallowed when graph is immutable.
                options.setUseXNNPACK(useXnnpack)
                options.setNumThreads(modelOptions.numThreads)
                val interpreter = Interpreter(
                    loadByteBufferModel(context, modelPath),
                    options
                )

                val batchCount: Int = inputImgCount
                if (batchCount > 1) {
                    //resize if should inference more than one image
                    val dims = intArrayOf(
                        batchCount,
                        modelConfig.inputSize.width,
                        modelConfig.inputSize.height,
                        modelConfig.colorSpace.channels
                    )
                    interpreter.resizeInput(0, dims)
                    interpreter.allocateTensors()
                }

                TFInterpreterWrapper(interpreter, delegate)
            } catch (e: Exception) {
                Timber.e(e)
                throw e
            }
        }

        private fun loadByteBufferModel(
            context: Context,
            modelPath: String
        ): MappedByteBuffer {
            return if (FileUtils.isMediaStoragePath(context, modelPath)) {
                TensorFlowUtils.loadModelFileFromExternal(modelPath)
            } else {
                TensorFlowUtils.loadModelFileFromAssets(context, modelPath)
            }
        }
    }
}
