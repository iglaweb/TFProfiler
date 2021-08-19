package ru.igla.tfprofiler.tflite_runners.base

import android.content.Context
import android.graphics.Bitmap
import ru.igla.tfprofiler.core.ColorSpace
import ru.igla.tfprofiler.core.Size
import ru.igla.tfprofiler.core.ops.BaseOpNormalizer
import ru.igla.tfprofiler.core.ops.GrayOpNormalizer
import ru.igla.tfprofiler.core.ops.OpNormalizer
import ru.igla.tfprofiler.models_list.ModelEntity
import ru.igla.tfprofiler.utils.forEachNoIterator
import java.nio.ByteBuffer
import java.nio.ByteOrder

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

open class TFLiteImageDetectAPIModelBase<T> :
    TFLiteAPIModelBase<List<Bitmap>, List<T>>(),
    Classifier<List<Bitmap>, List<T>> {

    @JvmField
    protected var modelOptions: ModelOptions? = null

    // Pre-allocated buffers.
    private lateinit var tempIntValues: IntArray
    private lateinit var imgData: ByteBuffer
    protected lateinit var inputSize: Size
    private var opNormalizer: OpNormalizer? = null

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param context     The asset manager to be used to load assets.
     * @param modelEntity The filepath of the model GraphDef protocol buffer.
     */
    @Throws(Exception::class)
    override fun init(
        context: Context,
        modelEntity: ModelEntity,
        modelOptions: ModelOptions
    ) {
        super.init(context, modelEntity, modelOptions)
        this.modelOptions = modelOptions
        this.inputSize = modelEntity.modelConfig.inputSize
        this.opNormalizer = getNormalizer(
            isModelQuantized,
            modelEntity.modelConfig.colorSpace
        )

        // Pre-allocate buffers.
        val numBytesPerChannel: Int = if (isModelQuantized) {
            1 // Quantized
        } else {
            4 // Floating point
        }

        //https://www.tensorflow.org/hub/common_signatures/images#input
        val pixelSize = modelEntity.modelConfig.colorSpace.channels
        val batchImageSize = modelOptions.numberOfInputImages
        this.imgData = ByteBuffer.allocateDirect(
            batchImageSize *
                    inputSize.width *
                    inputSize.height *
                    pixelSize *
                    numBytesPerChannel
        ).apply {
            order(ByteOrder.nativeOrder())
        } // 1 x 256x256 x 3 x 4
        tempIntValues = IntArray(inputSize.width * inputSize.height)
    }

    open fun getNormalizer(isQuantized: Boolean, colorSpace: ColorSpace): OpNormalizer? {
        return if (colorSpace === ColorSpace.COLOR) BaseOpNormalizer(isQuantized) else GrayOpNormalizer(
            isQuantized
        )
    }

    /***
     * Bitmaps of equal size and the size matches the neural network input
     * @param data bitmaps to normalize
     */
    override fun preprocess(data: List<Bitmap>): Array<Any> {
        imgData.rewind()
        data.forEachNoIterator { bitmap ->
            // Preprocess the image data from 0-255 int to normalized float based
            // on the provided parameters.
            val imageOffset = 0
            bitmap.getPixels(
                tempIntValues, imageOffset, bitmap.width,
                0, 0,
                bitmap.width,
                bitmap.height
            )
            opNormalizer?.convertBitmapToByteBuffer(imgData, tempIntValues, inputSize)
        }
        return arrayOf(imgData)
    }

    override fun prepareOutputs(): MutableMap<Int, Any> {
        return mutableMapOf()
    }

    override fun getDetections(outputMap: Map<Int, Any>): List<T> {
        return emptyList()
    }
}