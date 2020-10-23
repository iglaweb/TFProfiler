package ru.igla.tfprofiler.model_in_camera;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.Camera;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Trace;
import android.util.Pair;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

import ru.igla.tfprofiler.R;
import ru.igla.tfprofiler.core.Device;
import ru.igla.tfprofiler.core.SharedViewModel;
import ru.igla.tfprofiler.core.Timber;
import ru.igla.tfprofiler.env.CameraUtils;
import ru.igla.tfprofiler.env.ImageUtils;
import ru.igla.tfprofiler.models_list.CameraType;
import ru.igla.tfprofiler.report_details.ModelReportActivity;
import ru.igla.tfprofiler.report_details.ModelReportFragment;
import ru.igla.tfprofiler.reports_list.ListReportEntity;
import ru.igla.tfprofiler.tflite_runners.base.ModelOptions;
import ru.igla.tfprofiler.utils.IntentUtils;
import ru.igla.tfprofiler.utils.PermissionUtils;
import ru.igla.tfprofiler.utils.StringUtils;


public abstract class CameraActivity extends AppCompatActivity
        implements OnImageAvailableListener,
        Camera.PreviewCallback,
        View.OnClickListener,
        AdapterView.OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener {

    private static final int PERMISSIONS_REQUEST = 1;

    protected int previewWidth = 0;
    protected int previewHeight = 0;
    private boolean debug = false;
    private Handler handler;
    private HandlerThread handlerThread;
    private boolean useCamera2API;
    private boolean isProcessingFrame = false;
    private byte[][] yuvBytes = new byte[3][];
    private int[] rgbBytes = null;
    private int yRowStride;
    private Runnable postInferenceCallback;
    private Runnable imageConverter;

    private LinearLayout bottomSheetLayout;
    private LinearLayout gestureLayout;
    private BottomSheetBehavior<LinearLayout> sheetBehavior;

    protected TextView
            frameValueTextView,
            cropValueTextView,
            inferenceTimeTextView,
            inference_info_more,
            fpsTextView;
    protected TextView inferenceTimeMin, inferenceTimeMax, inferenceMemory;

    private SwitchCompat xnnpackSwitch;
    private Spinner deviceSpinner;
    protected ImageView bottomSheetArrowImageView;
    private ImageView plusImageView, minusImageView;
    private TextView threadsTextView;


    private boolean useXnnpack = false;
    private Device device = Device.CPU;
    private int numThreads = -1;
    private ModelOptions modelOptions = new ModelOptions(device, numThreads, useXnnpack);

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Timber.d("onCreate " + this);
        super.onCreate(null);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.tfe_od_activity_camera);

        if (PermissionUtils.hasCameraPermission(getApplicationContext())) {
            setFragment(getCameraType());
        } else {
            requestCameraPermission();
        }

        threadsTextView = findViewById(R.id.threads);
        plusImageView = findViewById(R.id.plus);
        minusImageView = findViewById(R.id.minus);
        bottomSheetLayout = findViewById(R.id.bottom_sheet_layout);
        gestureLayout = findViewById(R.id.gesture_layout);
        sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetArrowImageView = findViewById(R.id.bottom_sheet_arrow);

        configureBottomSheet();

        frameValueTextView = findViewById(R.id.frame_info);
        cropValueTextView = findViewById(R.id.crop_info);
        inferenceTimeTextView = findViewById(R.id.inference_info);
        fpsTextView = findViewById(R.id.fps_info);

        inferenceTimeMin = findViewById(R.id.inference_info_time_min);
        inferenceTimeMax = findViewById(R.id.inference_info_time_max);
        inferenceMemory = findViewById(R.id.inference_info_memory);

        inference_info_more = findViewById(R.id.inference_info_more);

        plusImageView.setOnClickListener(this);
        minusImageView.setOnClickListener(this);

        deviceSpinner = findViewById(R.id.device_spinner);
        deviceSpinner.setOnItemSelectedListener(this);

        xnnpackSwitch = findViewById(R.id.xnnpack_enabled);
        xnnpackSwitch.setOnCheckedChangeListener(this);
        useXnnpack = xnnpackSwitch.isChecked();

        device = Device.valueOf(deviceSpinner.getSelectedItem().toString());
        numThreads = Integer.parseInt(threadsTextView.getText().toString().trim());

        SharedViewModel model = new ViewModelProvider(this).get(SharedViewModel.class);

        ImageView ivSettings = findViewById(R.id.btnSettings);
        ivSettings.setOnClickListener(v -> {
            model.setModelData(getReportData());

            Intent intent = new Intent(this, ModelReportActivity.class);
            intent.putExtra(ModelReportFragment.EXTRA_KEY_REPORT_DATA, getReportData());
            IntentUtils.startActivityForResultSafely(this, ModelReportFragment.REPORT_REQUEST_CODE, intent);
        });

        ImageView ivToogleCamera = findViewById(R.id.toggleCamera);
        ivToogleCamera.setOnClickListener(v -> {
            if (this.cameraType == null) {
                this.cameraType = CameraType.FRONT;
            }

            if (useCamera2API) {
                CameraConnectionFragment fragment = (CameraConnectionFragment) getSupportFragmentManager().
                        findFragmentById(R.id.container);
                if (fragment != null) {
                    fragment.toogleCamera();
                }
            } else {
                LegacyCameraConnectionFragment fragment = (LegacyCameraConnectionFragment) getSupportFragmentManager().
                        findFragmentById(R.id.container);
                if (fragment != null) {
                    //todo
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ModelReportFragment.REPORT_REQUEST_CODE && resultCode == RESULT_OK) {
            finish();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (parent == deviceSpinner) {
            setDevice(Device.valueOf(parent.getItemAtPosition(pos).toString()));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing.
    }

    protected ModelOptions getModelOptions() {
        return modelOptions;
    }

    protected void setModelOptions(ModelOptions modelOptions) {
        this.modelOptions = modelOptions;
    }

    protected Device getDevice() {
        return device;
    }

    private void setDevice(Device device) {
        if (this.device != device) {
            Timber.d("Updating  device: " + device);
            this.device = device;
            final boolean threadsEnabled = device == Device.CPU;
            plusImageView.setEnabled(threadsEnabled);
            minusImageView.setEnabled(threadsEnabled);
            threadsTextView.setText(threadsEnabled ? String.valueOf(numThreads) : "N/A");
            onInferenceConfigurationChanged();
        }
    }

    private void configureBottomSheet() {
        ViewTreeObserver vto = gestureLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        gestureLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        //                int width = bottomSheetLayout.getMeasuredWidth();
                        int height = gestureLayout.getMeasuredHeight();

                        sheetBehavior.setPeekHeight(height);
                    }
                });
        sheetBehavior.setHideable(false);
        sheetBehavior.setBottomSheetCallback(
                new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        switch (newState) {
                            case BottomSheetBehavior.STATE_HIDDEN:
                                break;
                            case BottomSheetBehavior.STATE_EXPANDED: {
                                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_down);
                            }
                            break;
                            case BottomSheetBehavior.STATE_COLLAPSED: {
                                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
                            }
                            break;
                            case BottomSheetBehavior.STATE_DRAGGING:
                                break;
                            case BottomSheetBehavior.STATE_SETTLING:
                                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
                                break;
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    }
                });
    }

    protected int[] getRgbBytes() {
        imageConverter.run();
        return rgbBytes;
    }

    protected int getLuminanceStride() {
        return yRowStride;
    }

    protected byte[] getLuminance() {
        return yuvBytes[0];
    }

    /**
     * Callback for android.hardware.Camera API
     */
    @Override
    public void onPreviewFrame(final byte[] bytes, final Camera camera) {
        if (isProcessingFrame) {
            Timber.w("Dropping frame!");
            return;
        }

        try {
            // Initialize the storage bitmaps once when the resolution is known.
            if (rgbBytes == null) {
                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                previewHeight = previewSize.height;
                previewWidth = previewSize.width;
                rgbBytes = new int[previewWidth * previewHeight];
                onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
            }
        } catch (final Exception e) {
            Timber.e(e, "Exception!");
            return;
        }

        isProcessingFrame = true;
        yuvBytes[0] = bytes;
        yRowStride = previewWidth;

        imageConverter =
                () -> ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);

        postInferenceCallback =
                () -> {
                    camera.addCallbackBuffer(bytes);
                    isProcessingFrame = false;
                };
        processImage();
    }

    /**
     * Callback for Camera2 API
     */
    @Override
    public void onImageAvailable(final ImageReader reader) {
        // We need wait until we have some size from onPreviewSizeChosen
        if (previewWidth == 0 || previewHeight == 0) {
            return;
        }
        if (rgbBytes == null) {
            rgbBytes = new int[previewWidth * previewHeight];
        }
        try {
            final Image image = reader.acquireLatestImage();
            if (image == null) {
                return;
            }

            if (isProcessingFrame) {
                image.close();
                return;
            }
            isProcessingFrame = true;
            Trace.beginSection("imageAvailable");
            final Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);
            yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();

            imageConverter =
                    () -> ImageUtils.convertYUV420ToARGB8888(
                            yuvBytes[0],
                            yuvBytes[1],
                            yuvBytes[2],
                            previewWidth,
                            previewHeight,
                            yRowStride,
                            uvRowStride,
                            uvPixelStride,
                            rgbBytes);

            postInferenceCallback =
                    () -> {
                        image.close();
                        isProcessingFrame = false;
                    };

            processImage();
        } catch (final Exception e) {
            Timber.e(e);
        } finally {
            Trace.endSection();
        }
    }

    @Override
    public synchronized void onStart() {
        Timber.d("onStart " + this);
        super.onStart();
    }

    @Override
    public synchronized void onResume() {
        Timber.d("onResume " + this);
        super.onResume();

        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public synchronized void onPause() {
        Timber.d("onPause " + this);

        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            Timber.e(e, "Exception!");
        }

        super.onPause();
    }

    @Override
    public synchronized void onStop() {
        Timber.d("onStop " + this);
        super.onStop();
    }

    @Override
    public synchronized void onDestroy() {
        Timber.d("onDestroy " + this);
        super.onDestroy();
    }

    protected void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, @NotNull final String[] permissions, @NotNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST) {
            if (PermissionUtils.allPermissionsGranted(grantResults)) {
                setFragment(getCameraType());
            } else {
                requestCameraPermission();
            }
        }
    }

    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PermissionUtils.PERMISSION_CAMERA)) {
                Toast.makeText(
                        CameraActivity.this,
                        "Camera permission is required for this demo",
                        Toast.LENGTH_LONG)
                        .show();
            }
            requestPermissions(new String[]{PermissionUtils.PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
        }
    }

    @Nullable
    private CameraType cameraType = null;

    /*private CameraType getCameraType() {
        Pair<String, Boolean> pair = CameraUtils.chooseCamera(cameraType, getApplicationContext());
        if (pair != null) {
            useCamera2API = pair.second;
        }
        return pair == null ? null : pair.first;
    }*/

    protected void setFragment(CameraType cameraType) {
        Pair<String, Boolean> pair = CameraUtils.chooseCamera(cameraType, getApplicationContext());
        if (pair != null) {
            useCamera2API = pair.second;
        }
        final String cameraId = pair == null ? null : pair.first;
        this.cameraType = cameraType;
        final Fragment fragment;
        if (useCamera2API) {
            CameraConnectionFragment camera2Fragment =
                    CameraConnectionFragment.newInstance(
                            new CameraConnectionFragment.ConnectionCallback() {
                                @Override
                                public void onPreviewSizeChosen(final Size size, final int rotation) {
                                    previewHeight = size.getHeight();
                                    previewWidth = size.getWidth();
                                    CameraActivity.this.onPreviewSizeChosen(size, rotation);
                                }
                            },
                            this,
                            getLayoutId(),
                            getDesiredPreviewFrameSize());

            camera2Fragment.setCamera(cameraType, cameraId);
            fragment = camera2Fragment;
        } else {
            fragment =
                    new LegacyCameraConnectionFragment(
                            this,
                            getLayoutId(),
                            getDesiredPreviewFrameSize(),
                            cameraType
                    );
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    protected void fillBytes(final Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                Timber.d("Initializing buffer %d at size %d", i, buffer.capacity());
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

    public boolean isDebug() {
        return debug;
    }

    protected void readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        }
    }

    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.plus) {
            String threads = threadsTextView.getText().toString().trim();
            int numThreads = Integer.parseInt(threads);
            if (numThreads >= 9) return;
            setNumThreads(++numThreads);
            threadsTextView.setText(String.valueOf(numThreads));
        } else if (v.getId() == R.id.minus) {
            String threads = threadsTextView.getText().toString().trim();
            int numThreads = Integer.parseInt(threads);
            if (numThreads == 1) {
                return;
            }
            setNumThreads(--numThreads);
            threadsTextView.setText(String.valueOf(numThreads));
        }
    }

    private void setNumThreads(int numThreads) {
        if (this.numThreads != numThreads) {
            Timber.d("Updating  numThreads: " + numThreads);
            this.numThreads = numThreads;
            onInferenceConfigurationChanged();
        }
    }

    protected void clearInterferenceStats() {
        showInference("");
        showInferenceMore("");
        showFps("");
        showInferenceMore(0, 0, 0);
    }

    protected void showFrameInfo(String frameInfo) {
        frameValueTextView.setText(frameInfo);
    }

    protected void showCropInfo(String cropInfo) {
        cropValueTextView.setText(cropInfo);
    }

    protected void showInference(String inferenceTime) {
        inferenceTimeTextView.setText(inferenceTime);
    }

    protected int getNumThreads() {
        return numThreads;
    }

    protected boolean useXnnpack() {
        return useXnnpack;
    }

    protected void showInferenceMore(String inference) {
        inference_info_more.setText(inference);
    }

    @SuppressLint("SetTextI18n")
    protected void showInferenceMore(long timeMin, long timeMax, long memory) {
        String memoryStr = StringUtils.getReadableFileSize(memory, true);
        inferenceTimeMin.setText(timeMin + " ms");
        inferenceTimeMax.setText(timeMax + " ms");
        inferenceMemory.setText(memoryStr);
    }


    protected void showFps(String fps) {
        fpsTextView.setText(fps);
    }

    protected abstract void processImage();

    protected abstract void onPreviewSizeChosen(final Size size, final int rotation);

    protected abstract int getLayoutId();

    protected abstract Size getDesiredPreviewFrameSize();

    protected abstract void onInferenceConfigurationChanged();

    protected abstract ListReportEntity getReportData();

    @NonNull
    protected abstract CameraType getCameraType();

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.xnnpack_enabled) {
            this.useXnnpack = isChecked;
            onInferenceConfigurationChanged();
        }
    }
}
