package ru.igla.tfprofiler.model_in_camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import ru.igla.tfprofiler.R;
import ru.igla.tfprofiler.core.Device;
import ru.igla.tfprofiler.core.Timber;
import ru.igla.tfprofiler.core.analytics.StatisticsEstimator;
import ru.igla.tfprofiler.customview.OverlayView;
import ru.igla.tfprofiler.env.ImageUtils;
import ru.igla.tfprofiler.models_list.CameraType;
import ru.igla.tfprofiler.models_list.MediaRequest;
import ru.igla.tfprofiler.models_list.ModelEntity;
import ru.igla.tfprofiler.models_list.NeuralModelsListFragment;
import ru.igla.tfprofiler.reports_list.ListReportEntity;
import ru.igla.tfprofiler.tflite_runners.Classifier;
import ru.igla.tfprofiler.tflite_runners.ClassifierFactory;
import ru.igla.tfprofiler.tflite_runners.ModelOptions;
import ru.igla.tfprofiler.tracking.MultiBoxTracker;
import ru.igla.tfprofiler.ui.widgets.toast.Toaster;
import ru.igla.tfprofiler.utils.DebugDrawer;
import ru.igla.tfprofiler.utils.IOUtils;
import ru.igla.tfprofiler.utils.StringUtils;
import ru.igla.tfprofiler.utils.TimeWatchClockOS;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {

    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private static final boolean MAINTAIN_ASPECT = false;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);

    OverlayView trackingOverlay;
    private Integer sensorOrientation;

    @Nullable
    private Classifier<Classifier.Recognition> detector;

    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;

    private boolean computingDetection = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;

    private ModelEntity modelEntity = null;
    private CameraType cameraType;

    @Nullable
    Toaster mToaster;

    @Nullable
    private StatisticsEstimator statisticsEstimator;

    public void showToast(@NonNull String text) {
        Toaster toast = mToaster == null ? new Toaster(getApplicationContext()) : mToaster;
        mToaster = toast;
        toast.showToast(text);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null) {
            MediaRequest mediaRequest = intent.getParcelableExtra(NeuralModelsListFragment.MEDIA_ITEM);
            if (mediaRequest == null) {
                return;
            }

            this.modelEntity = mediaRequest.getModelEntity();
            String cameraType = intent.getStringExtra(NeuralModelsListFragment.EXTRA_CAMERA_TYPE);
            if (StringUtils.isNullOrEmpty(cameraType)) {
                showToast("Camera type is empty. Finish activity");
                finish();
            }
            this.cameraType = CameraType.valueOf(cameraType);
        }
        if (this.modelEntity == null) {
            showToast("Model is empty. Finish activity");
            finish();
        }
        if (this.cameraType == null) {
            showToast("Camera is empty. Finish activity");
            finish();
        }
        this.statisticsEstimator = new StatisticsEstimator(getApplicationContext());
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        tracker = new MultiBoxTracker(this);

        final int cropSize = modelEntity.getInputSize();
        try {
            ModelOptions modelOptions = getCurrentModelOptions();
            recreateClassifier(modelOptions);
            if (detector == null) {
                throw new IOException();
            }
        } catch (final IOException e) {
            Timber.e(e);
            showToast("Classifier could not be initialized");
            finish();
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        Timber.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        Timber.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);


        /*String fromCameraId = CameraUtils.getFrontFacingCameraId(getContext());
        if(cameraId.equalsIgnoreCase(fromCameraId)) {
            matrix.preScale(-1.0f, 1.0f);
        }*/

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT,
                        false
                );

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                canvas -> {
                    tracker.draw(canvas);
                    if (isDebug()) {
                        tracker.drawDebug(canvas);
                    }
                });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            closeClassifier();

            if (rgbFrameBitmap != null && !rgbFrameBitmap.isRecycled()) {
                rgbFrameBitmap.recycle();
            }
            if (croppedBitmap != null && !croppedBitmap.isRecycled()) {
                croppedBitmap.recycle();
            }
        }
    }

    DebugDrawer debugDrawer = new DebugDrawer();

    @Override
    protected void processImage() {
        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;
        Timber.i("Preparing image " + currTimestamp + " for detection in bg thread.");

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);

        // For examining the actual TF input.
        debugDrawer.saveBitmap(croppedBitmap);

        runInBackground(
                () -> runRecognitionInterference(currTimestamp));
    }

    private void runRecognitionInterference(long currTimestamp) {
        if (detector == null) return;

        Timber.i("Running detection on image " + currTimestamp);
        final long startTime = SystemClock.uptimeMillis();
        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
        final long lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

        statisticsEstimator.incrementFrameNumber(getModelOptions());

        statisticsEstimator.setInterferenceTime(getModelOptions(), lastProcessingTimeMs);
        final double fps = statisticsEstimator.calcFps(getModelOptions());

        long memoryUsage = statisticsEstimator.appMemoryEstimator.getMemory();
        statisticsEstimator.setMemoryUsage(getModelOptions(), memoryUsage);

        final int croppedWidth = croppedBitmap.getWidth();
        final int croppedHeight = croppedBitmap.getHeight();

        debugDrawer.prepareOutput(croppedBitmap);

        final List<Classifier.Recognition> mappedRecognitions =
                new LinkedList<>();

        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                debugDrawer.draw(location);

                cropToFrameTransform.mapRect(location);

                result.setLocation(location);
                mappedRecognitions.add(result);
            }
        }


        debugDrawer.writeOutput();

        tracker.trackResults(mappedRecognitions, currTimestamp);
        trackingOverlay.postInvalidate();

        computingDetection = false;

        runOnUiThread(
                () -> {
                    showFrameInfo(previewWidth + "x" + previewHeight);
                    showCropInfo(croppedWidth + "x" + croppedHeight);
                    showInference(lastProcessingTimeMs + " ms");

                    long initializationTime = statisticsEstimator.getInitTime(getModelOptions());

                    DescriptiveStatistics descriptiveStatistics = statisticsEstimator.getStats(getModelOptions()).getStatistics();
                    double std = descriptiveStatistics.getStandardDeviation();
                    double mean = descriptiveStatistics.getMean();
                    int min = (int) descriptiveStatistics.getMin();
                    int max = (int) descriptiveStatistics.getMax();

                    String statsStr = String.format(
                            Locale.US,
                            "Initialization: %d ms\r\n" +
                                    "%.2f ± %.2f ms",
                            initializationTime,
                            mean, std
                    );
                    showInferenceMore(statsStr);
                    showInferenceMore(min, max, memoryUsage);
                    showFps(String.valueOf(fps));
                });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.tfe_od_camera_connection_fragment_tracking;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    @NonNull
    protected CameraType getCameraType() {
        return cameraType == null ? CameraType.REAR : cameraType;
    }

    @Override
    protected void onInferenceConfigurationChanged() {
        if (rgbFrameBitmap == null) {
            // Defer creation until we're getting camera frames.
            return;
        }
        ModelOptions modelOptions = getCurrentModelOptions();
        runInBackground(() -> recreateClassifier(modelOptions));
    }

    private ModelOptions getCurrentModelOptions() {
        final Device device = getDevice();
        final int numThreads = getNumThreads();
        return new ModelOptions.Builder()
                .device(device)
                .numThreads(numThreads)
                .xnnpack(useXnnpack())
                .build();
    }

    @Override
    protected ListReportEntity getReportData() {
        if (statisticsEstimator == null) return null;
        return statisticsEstimator.createReport(modelEntity);
    }

    private void closeClassifier() {
        Timber.d("Closing classifier.");
        IOUtils.closeQuietly(detector);
        detector = null;
    }

    private void recreateClassifier(ModelOptions modelOptions) {
        closeClassifier();

        if (modelOptions.getDevice() == Device.GPU
                && (modelEntity != null && modelEntity.getQuantized())) {
            Timber.d("Not creating classifier: GPU doesn't support quantized models.");
            runOnUiThread(
                    () -> showToast(getString(R.string.tfe_ic_gpu_quant_error)));
            return;
        }

        TimeWatchClockOS timeWatchClockOS = new TimeWatchClockOS();
        timeWatchClockOS.start();
        try {
            Timber.d(
                    "Creating classifier %s",
                    modelOptions.toString()
            );
            detector = ClassifierFactory.create(
                    getApplicationContext(),
                    modelEntity,
                    modelOptions
            );
        } catch (Exception e) {
            Timber.e(e, "Failed to create classifier.");
        } finally {
            setModelOptions(modelOptions);
            clearStats();
            statisticsEstimator.setInitTime(modelOptions, timeWatchClockOS.stop());
        }
    }

    /***
     * Clear stats as it can influence stats
     */
    private void clearStats() {
        statisticsEstimator.clearStats(getModelOptions());
        runOnUiThread(DetectorActivity.super::clearInterferenceStats);
    }
}
