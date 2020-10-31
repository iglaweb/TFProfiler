package ru.igla.tfprofiler.tflite_runners.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Trace;

import org.jetbrains.annotations.NotNull;
import org.tensorflow.lite.DataType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import ru.igla.tfprofiler.core.ColorSpace;
import ru.igla.tfprofiler.core.Device;
import ru.igla.tfprofiler.core.ModelType;
import ru.igla.tfprofiler.core.Timber;
import ru.igla.tfprofiler.core.ops.BaseOpNormalizer;
import ru.igla.tfprofiler.core.ops.GrayOpNormalizer;
import ru.igla.tfprofiler.core.ops.OpNormalizer;
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

    public abstract Map<Integer, Object> prepareOutputImage();

    public abstract List<T> getDetections();

    protected int inputWidth;
    protected int inputHeight;

    // Pre-allocated buffers.
    protected Vector<String> labels = new Vector<>();
    private int[] intValues;

    private ByteBuffer imgData;

    protected TFInterpeterThreadExecutor tfLiteExecutor;

    private static final int COLOR_PIXEL_SIZE = 3;
    private static final int GRAY_PIXEL_SIZE = 1;

    private static final int DIM_BATCH_SIZE = 1;

    private OpNormalizer opNormalizer;

    public TFLiteObjectDetectionAPIModelBase() {
    }

    @Override
    public void init(Context context, ModelEntity modelEntity, ModelOptions modelOptions) throws Exception {
        create(
                context,
                modelEntity,
                modelOptions.getDevice(),
                modelOptions.getNumThreads(),
                modelOptions.getUseXnnpack()
        );
    }

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param context     The asset manager to be used to load assets.
     * @param modelEntity The filepath of the model GraphDef protocol buffer.
     */
    public Classifier<T> create(
            final Context context,
            ModelEntity modelEntity,
            Device device,
            int numThreads,
            boolean useXnnpack)
            throws IOException {

        final String modelFilename = modelEntity.getModelFile();
        final String labelFilename = modelEntity.getLabelFile();

        this.inputWidth = modelEntity.getModelConfig().getInputWidth();
        this.inputHeight = modelEntity.getModelConfig().getInputHeight();

        if (!StringUtils.isNullOrEmpty(labelFilename)) {
            String actualFilename = labelFilename.split("file:///android_asset/")[1];
            this.labels = TensorFlowUtils.loadLabelList(context.getAssets(), actualFilename);
        }

        try {
            tfLiteExecutor = new TFInterpeterThreadExecutor(context, modelFilename);
            tfLiteExecutor.init(device, numThreads, useXnnpack);
        } catch (Exception e) {
            Timber.e(e);
            throw new RuntimeException(e);
        }

        boolean isModelQuantized;
        if (modelEntity.getModelType() == ModelType.CUSTOM) {
            TFInterpreterWrapper interpreter = tfLiteExecutor.getTfLite();
            if (interpreter == null) {
                throw new RuntimeException("Interpreter is not configured");
            }
            int probabilityTensorIndex = 0;
            // Creates the output tensor and its processor.
            DataType probabilityDataType = interpreter.getInterpreter().getOutputTensor(probabilityTensorIndex).dataType();
            isModelQuantized = probabilityDataType == DataType.UINT8;
        } else {
            isModelQuantized = modelEntity.getModelConfig().getQuantized();
        }

        this.opNormalizer = getNormalizer(isModelQuantized, modelEntity.getModelConfig().getColorSpace());

        // Pre-allocate buffers.
        final int numBytesPerChannel;
        if (isModelQuantized) {
            numBytesPerChannel = 1; // Quantized
        } else {
            numBytesPerChannel = 4; // Floating point
        }

        //https://www.tensorflow.org/hub/common_signatures/images#input

        final int pixelSize = modelEntity.getModelConfig().getColorSpace() == ColorSpace.GRAYSCALE ? GRAY_PIXEL_SIZE : COLOR_PIXEL_SIZE;
        this.imgData = ByteBuffer.allocateDirect(
                DIM_BATCH_SIZE *
                        this.inputWidth *
                        this.inputHeight *
                        pixelSize *
                        numBytesPerChannel
        );
        this.imgData.order(ByteOrder.nativeOrder());
        this.intValues = new int[this.inputWidth * this.inputHeight];
        return this;
    }

    private void normalizeBitmap(final Bitmap bitmap) {
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        imgData.rewind();
        opNormalizer.convertBitmapToByteBuffer(imgData, intValues, inputWidth, inputHeight);
    }

    public OpNormalizer getNormalizer(boolean isQuantized, ColorSpace colorSpace) {
        return colorSpace == ColorSpace.COLOR ? new BaseOpNormalizer(isQuantized) : new GrayOpNormalizer(isQuantized);
    }

    @NotNull
    @Override
    public List<T> recognizeImage(@NotNull final Bitmap bitmap) {
        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage");

        Trace.beginSection("preprocessBitmap");
        normalizeBitmap(bitmap);
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
