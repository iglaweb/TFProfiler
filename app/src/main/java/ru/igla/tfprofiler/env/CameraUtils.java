package ru.igla.tfprofiler.env;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Pair;

import androidx.annotation.Nullable;

import ru.igla.tfprofiler.core.Timber;
import ru.igla.tfprofiler.models_list.CameraType;

public final class CameraUtils {

    private CameraUtils() {
    }

    @Nullable
    public static Pair<String, Boolean> chooseCamera(CameraType cameraType, Context context) {
        final CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) return null;
        try {
            for (final String cameraId : manager.getCameraIdList()) {
                final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null) {
                    // exclude other cameras
                    if (facing == CameraCharacteristics.LENS_FACING_FRONT &&
                            cameraType == CameraType.REAR) {
                        continue;
                    }
                    if (facing == CameraCharacteristics.LENS_FACING_BACK &&
                            cameraType == CameraType.FRONT) {
                        continue;
                    }
                }

                final StreamConfigurationMap map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (map == null) {
                    continue;
                }

                // Fallback to camera1 API for internal cameras that don't have full support.
                // This should help with legacy situations where using the camera2 API causes
                // distorted or otherwise broken previews.
                boolean useCamera2API =
                        (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
                                || isHardwareLevelSupported(
                                characteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);
                Timber.i("Camera API lv2?: %s", useCamera2API);
                return Pair.create(cameraId, useCamera2API);
            }
        } catch (CameraAccessException e) {
            //LOGGER.e(e, "Not allowed to access camera");
        }
        return null;
    }

    // Returns true if the device supports the required hardware level, or better.
    private static boolean isHardwareLevelSupported(
            CameraCharacteristics characteristics, int requiredLevel) {
        int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            return requiredLevel == deviceLevel;
        }
        // deviceLevel is not LEGACY, can use numerical sort
        return requiredLevel <= deviceLevel;
    }

    @Nullable
    public static String getFrontFacingCameraId(Context context) {
        final CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) return null;

        try {
            for (final String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == CameraCharacteristics.LENS_FACING_FRONT) return cameraId;
            }
        } catch (CameraAccessException e) {
            Timber.e(e);
        }
        return null;
    }
}
