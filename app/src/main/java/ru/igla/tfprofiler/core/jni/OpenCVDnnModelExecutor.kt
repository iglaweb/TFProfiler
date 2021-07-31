package ru.igla.tfprofiler.core.jni

import org.opencv.core.Mat
import ru.igla.tfprofiler.core.Size
import java.util.logging.Level
import java.util.logging.Logger

class OpenCVDnnModelExecutor(
    val modelPath: String,
    val nhwc: Boolean,
    val cuda: Boolean,
    val channels: Int,
    val inputSize: Size
) : DnnModelExecutor {

    private var initialized = false
    private var initializing = false

    override fun init(): Boolean {
        return if (!initializing) {
            logger.log(
                Level.INFO,
                " *** Requested a fresh initialization with jniInit() ***"
            )
            initializing = true
            val inputWidth = inputSize.width
            val inputHeight = inputSize.height
            if (jniInitModel(
                    modelPath,
                    nhwc,
                    cuda,
                    channels,
                    inputWidth,
                    inputHeight) == 0
            ) { // If all went ok, state as initialized true
                logger.log(
                    Level.INFO,
                    " *** jniInit() OK ***"
                )
                initializing = false
                initialized = true
                true
            } else {
                initializing = false
                initialized = false
                logger.log(
                    Level.SEVERE,
                    " *** jniInit() ERROR ***"
                )
                false
            }
        } else {
            logger.log(
                Level.INFO,
                " *** Requested initialization with jniInit() while already initializing ***"
            )
            true
        }
    }

    override fun deInit() {
        logger.log(Level.INFO, " *** Requested deinitialization with jniDeInit() ***")
        if (jniDeInitModel() == 0) {
            logger.log(Level.INFO, " *** jniDeInit() DnnModelExecutor OK ***")
            initialized = false
        } else {
            logger.log(Level.SEVERE, " *** jniDeInit() DnnModelExecutor ERROR ***")
        }
    }


    override fun executeModel(images: List<Mat>) {
        if (!initialized) {
            logger.log(Level.SEVERE, " *** DnnModelExecutor is not initialized, use jniInit() ***")
            return
        }

        val imgs = LongArray(images.size)
        images.forEachIndexed { idx, item ->
            imgs[idx] = item.nativeObj
        }
        try {
            jniExecuteModel(imgs)
        } catch (e: Throwable) {
            logger.log(Level.SEVERE, "Exception", e)
        }
    }

    companion object {
        private val logger = Logger.getLogger(
            OpenCVDnnModelExecutor::class.java.name
        )

        private external fun jniExecuteModel(bitmap: LongArray)
        private external fun jniDeInitModel(): Int
        private external fun jniInitModel(
            modelPath: String,
            nhwc: Boolean,
            cuda: Boolean,
            channels: Int,
            inputWidth: Int,
            inputHeight: Int
        ): Int
    }
}