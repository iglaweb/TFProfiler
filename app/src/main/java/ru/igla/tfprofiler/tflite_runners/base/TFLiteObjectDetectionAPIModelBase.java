package ru.igla.tfprofiler.tflite_runners.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Trace;

import org.jetbrains.annotations.NotNull;
import org.tensorflow.lite.DataType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.igla.tfprofiler.core.ColorSpace;
import ru.igla.tfprofiler.core.ModelFormat;
import ru.igla.tfprofiler.core.ModelType;
import ru.igla.tfprofiler.core.Size;
import ru.igla.tfprofiler.core.ops.BaseOpNormalizer;
import ru.igla.tfprofiler.core.ops.GrayOpNormalizer;
import ru.igla.tfprofiler.core.ops.OpNormalizer;
import ru.igla.tfprofiler.core.tflite.FailedCreateTFDelegate;
import ru.igla.tfprofiler.core.tflite.TFInterpeterThreadExecutor;
import ru.igla.tfprofiler.core.tflite.TFInterpreterWrapper;
import ru.igla.tfprofiler.core.tflite.TensorFlowUtils;
import ru.igla.tfprofiler.models_list.ModelEntity;
import ru.igla.tfprofiler.utils.StringUtils;

/**
 * Wrapper for frozen detection models trained using the Tensorflow Object Detection API:
 * - https://github.com/tensorflow/models/tree/master/research/object_detection
 * where you can find the training code.
 * <p>
 * To use pretrained models in the API or convert to TF Lite models, please see docs for details:
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/detection_model_zoo.md
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/running_on_mobile_tensorflowlite.md#running-our-model-on-android
 */
public abstract class TFLiteObjectDetectionAPIModelBase<T> implements Classifier<T> {

    protected ModelOptions modelOptions;

    public abstract Map<Integer, Object> prepareOutputImage();

    public abstract List<T> getDetections();

    // Pre-allocated buffers.
    protected List<String> labels = new ArrayList<>();
    private int[] tempIntValues;

    private ByteBuffer imgData;

    protected Size mInputSize;

    protected TFInterpeterThreadExecutor tfLiteExecutor;

    private OpNormalizer opNormalizer;

    public TFLiteObjectDetectionAPIModelBase() {
    }

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param context     The asset manager to be used to load assets.
     * @param modelEntity The filepath of the model GraphDef protocol buffer.
     */
    @Override
    public void init(@NotNull Context context, ModelEntity modelEntity, @NotNull ModelOptions modelOptions) throws Exception {
        final String modelFilename = modelEntity.getModelFile();
        final String labelFilename = modelEntity.getLabelFile();
        if (!StringUtils.isNullOrEmpty(labelFilename)) {
            String actualFilename = labelFilename.split("file:///android_asset/")[1];
            this.labels = TensorFlowUtils.loadLabelList(context.getAssets(), actualFilename);
        }

        this.modelOptions = modelOptions;
        this.mInputSize = modelEntity.getModelConfig().getInputSize();
        this.tfLiteExecutor = new TFInterpeterThreadExecutor(context, modelFilename);
        this.tfLiteExecutor.init(modelEntity, modelOptions);

        boolean isModelQuantized;
        if (modelEntity.getModelType() == ModelType.CUSTOM_TFLITE) {
            TFInterpreterWrapper interpreter = tfLiteExecutor.getTfLite();
            if (interpreter == null) {
                throw new FailedCreateTFDelegate(modelOptions.getDevice(), "Interpreter is not configured");
            }
            int probabilityTensorIndex = 0;
            // Creates the output tensor and its processor.
            DataType probabilityDataType = interpreter.getInterpreter().getOutputTensor(probabilityTensorIndex).dataType();
            isModelQuantized = probabilityDataType == DataType.UINT8;
        } else {
            isModelQuantized = modelEntity.getModelConfig().getModelFormat() == ModelFormat.QUANTIZED;
        }

        this.opNormalizer = getNormalizer(
                isModelQuantized,
                modelEntity.getModelConfig().getColorSpace()
        );

        // Pre-allocate buffers.
        final int numBytesPerChannel;
        if (isModelQuantized) {
            numBytesPerChannel = 1; // Quantized
        } else {
            numBytesPerChannel = 4; // Floating point
        }

        //https://www.tensorflow.org/hub/common_signatures/images#input

        final int pixelSize = modelEntity.getModelConfig().getColorSpace().getChannels();
        final int batchImageSize = modelOptions.getNumberOfInputImages();

        this.imgData = ByteBuffer.allocateDirect(
                batchImageSize *
                        mInputSize.getWidth() *
                        mInputSize.getHeight() *
                        pixelSize *
                        numBytesPerChannel
        ); // 1 x 256x256 x 3 x 4
        this.imgData.order(ByteOrder.nativeOrder());
        this.tempIntValues = new int[this.mInputSize.getWidth() * this.mInputSize.getHeight()];
    }

    /***
     * Bitmaps of equal size and the size matches the neural network input
     * @param bitmaps
     */
    private void normalizeBitmaps(final List<Bitmap> bitmaps) {
        imgData.rewind();
        for (Bitmap bitmap : bitmaps) {
            // Preprocess the image data from 0-255 int to normalized float based
            // on the provided parameters.
            int imageOffset = 0;
            bitmap.getPixels(
                    tempIntValues, imageOffset, bitmap.getWidth(),
                    0, 0,
                    bitmap.getWidth(),
                    bitmap.getHeight()
            );
            opNormalizer.convertBitmapToByteBuffer(imgData, tempIntValues, mInputSize);
        }
    }

    public OpNormalizer getNormalizer(boolean isQuantized, ColorSpace colorSpace) {
        return colorSpace == ColorSpace.COLOR ?
                new BaseOpNormalizer(isQuantized) :
                new GrayOpNormalizer(isQuantized);
    }

    @NotNull
    @Override
    public List<T> recognizeImage(@NotNull final List<Bitmap> bitmaps) {
        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage");

        Trace.beginSection("preprocessBitmap");
        normalizeBitmaps(bitmaps);
        Trace.endSection(); // preprocessBitmap

        // Copy the input data into TensorFlow.
        Trace.beginSection("feed");
        Map<Integer, Object> outputMap = prepareOutputImage();
        Object[] inputArray = {imgData};
        Trace.endSection();

        runInferenceCall(inputArray, outputMap);

        final List<T> recognitions = getDetections();
        Trace.endSection(); // "recognizeImage"
        return recognitions;
    }

    @Override
    public void close() {
        if (tfLiteExecutor != null) {
            tfLiteExecutor.close();
            tfLiteExecutor = null;
        }
    }

    private void runInferenceCall(Object[] inputArray, Map<Integer, Object> outputMap) {
        // Run the inference call.
        Trace.beginSection("run");
        tfLiteExecutor.runForMultipleInputsOutputs(inputArray, outputMap);
        Trace.endSection();
    }
}
