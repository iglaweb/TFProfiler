package ru.igla.tfprofiler.core.jni

import org.opencv.core.Mat
import org.opencv.dnn.Dnn
import org.opencv.dnn.Net
import org.opencv.imgproc.Imgproc
import ru.igla.tfprofiler.core.Size
import ru.igla.tfprofiler.core.tflite.OpenCVInterpreterWrapper
import ru.igla.tfprofiler.utils.forEachNoIterator
import ru.igla.tfprofiler.utils.logI

class JavaOpenCVDnnModelExecutor(
    private val modelPath: String,
    private val cuda: Boolean,
    private var useNHWC: Boolean = false,
    private val channelsCount: Int = 3,
    inputWidth: Int,
    inputHeight: Int
) : DnnModelExecutor {

    private val imageSize = Size(inputWidth, inputHeight)
    private val imageSizeCV = org.opencv.core.Size(
        inputWidth.toDouble(), inputHeight.toDouble()
    )

    private var initialized = false
    private var dnnNet: Net? = null

    @Throws(Exception::class)
    override fun init(): Boolean {
        OpenCVInterpreterWrapper.createDnnModel(modelPath).let {
            dnnNet = it
            if (cuda) {
                it.setPreferableBackend(Dnn.DNN_BACKEND_CUDA)
                it.setPreferableTarget(Dnn.DNN_TARGET_CUDA_FP16)
            } else {
                it.setPreferableBackend(Dnn.DNN_BACKEND_OPENCV)
                it.setPreferableTarget(Dnn.DNN_TARGET_CPU)
            }
        }
        initialized = true
        return true
    }

    override fun deInit() {
        logI { "*** Requested deinitialization with jniDeInit() ***" }
        if (jniDeInitModel() == 0) {
            logI { "*** jniDeInit() DnnModelExecutor OK ***" }
            initialized = false
        } else {
            logI { "*** jniDeInit() DnnModelExecutor ERROR ***" }
        }
    }

    private fun jniDeInitModel(): Int {
        dnnNet = null
        return 0
    }

    override fun executeModel(images: List<Mat>) {
        if (images.isEmpty()) return
        val dnn = checkNotNull(dnnNet) { "Dnn net model is not initialized!" }

        val readyImages = mutableListOf<Mat>()
        images.forEachNoIterator { image ->
            var ret = Mat()
            if (channelsCount == 1 && image.channels() >= 3) {
                Imgproc.cvtColor(image, ret, Imgproc.COLOR_BGR2GRAY)
            } else {
                ret = image
            }

            if (ret.cols() != imageSize.width || ret.rows() != imageSize.height) {
                Imgproc.resize(ret, ret, imageSizeCV)
            }
            readyImages.add(ret)
        }

        //optimization
        val scaleFactor = 1.0
        var blob = if (readyImages.size > 1) {
            Dnn.blobFromImages(readyImages, scaleFactor)
        } else {
            Dnn.blobFromImage(readyImages.first(), scaleFactor)
        }

        if (useNHWC) {
            // e.g. (1, 3, 128, 128) -> (1, 128, 128, 3)
            val imageSize = readyImages.size
            blob = blob.reshape(
                1, intArrayOf(
                    imageSize,
                    imageSizeCV.width.toInt(),
                    imageSizeCV.height.toInt(),
                    channelsCount
                )
            )
        }

        dnn.setInput(blob)
        val classification = dnn.forward()
        classification?.free()

        readyImages.forEachNoIterator {
            it.free()
        }
        readyImages.clear()
        blob.free()
    }
}