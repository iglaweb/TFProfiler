package ru.igla.tfprofiler.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.net.Uri
import android.os.Build
import android.util.Pair
import android.view.Surface
import android.view.WindowManager
import androidx.exifinterface.media.ExifInterface
import ru.igla.tfprofiler.models_list.CameraType
import timber.log.Timber
import java.io.IOException


/**
 * Created by igor-lashkov on 26/10/2017.
 */
@SuppressWarnings("deprecated")
object CameraUtils {

    /**
     * This method is responsible for solving the rotation issue if exist. Also scale the images to
     * 1024x1024 resolution
     *
     * @param context       The current context
     * @param selectedImage The Image URI
     * @return Bitmap image results
     * @throws IOException
     */
    @Throws(IOException::class)
    fun handleSamplingAndRotationBitmap(
        context: Context,
        selectedImage: Uri,
        targetW: Int,
        targetH: Int
    ): Bitmap? {
        context.contentResolver.openInputStream(selectedImage).use {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(it, null, options)
            val photoW = options.outWidth
            val photoH = options.outHeight
            // Determine how much to scale down the image
            val scaleFactor = maxOf(photoW / targetW, photoH / targetH)
            // Calculate inSampleSize
            options.inSampleSize = scaleFactor
            options.inJustDecodeBounds = false
            context.contentResolver.openInputStream(selectedImage).use { stream ->
                options.inJustDecodeBounds = false
                BitmapFactory.decodeStream(stream, null, options)?.let { bmp ->
                    return rotateImageIfRequired(context, bmp, selectedImage)
                }
            }
        }
        return null
    }

    /**
     * Rotate an image if required.
     *
     * @param img           The image bitmap
     * @param selectedImage Image URI
     * @return The resulted Bitmap after manipulation
     */
    @Throws(IOException::class)
    private fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri): Bitmap {
        context.contentResolver.openInputStream(selectedImage).use {
            val ei = if (it != null && Build.VERSION.SDK_INT > 23)
                ExifInterface(it)
            else if (selectedImage.path != null)
                ExifInterface(selectedImage.path!!)
            else
                return img
            val orientation =
                ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90.0f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180.0f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270.0f)
                else -> img
            }
        }
    }

    private fun rotateImage(bmp: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
        bmp.recycle()
        return rotatedImg
    }

    @JvmStatic
    fun getCameraId(cameraType: CameraType): Int {
        val ci = CameraInfo()
        for (i in 0 until Camera.getNumberOfCameras()) {
            Camera.getCameraInfo(i, ci)
            if (cameraType === CameraType.REAR && ci.facing == CameraInfo.CAMERA_FACING_BACK) return i
            if (cameraType === CameraType.FRONT && ci.facing == CameraInfo.CAMERA_FACING_FRONT) return i
        }
        return -1 // No camera found
    }

    @JvmStatic
    fun getScreenOrientation(windowManager: WindowManager): Int {
        return when (windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_270 -> 270
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_90 -> 90
            else -> 0
        }
    }

    @JvmStatic
    fun chooseCamera(cameraType: CameraType, context: Context): Pair<String, Boolean>? {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
            ?: return null
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                // We don't use a front facing camera in this sample.
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null) {
                    // exclude other cameras
                    if (facing == CameraMetadata.LENS_FACING_FRONT &&
                        cameraType === CameraType.REAR
                    ) {
                        continue
                    }
                    if (facing == CameraMetadata.LENS_FACING_BACK &&
                        cameraType === CameraType.FRONT
                    ) {
                        continue
                    }
                } else {
                    continue
                }
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    ?: continue

                // Fallback to camera1 API for internal cameras that don't have full support.
                // This should help with legacy situations where using the camera2 API causes
                // distorted or otherwise broken previews.
                val useCamera2API = (facing == CameraMetadata.LENS_FACING_EXTERNAL
                        || isHardwareLevelSupported(
                    characteristics, CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL
                ))
                logI { String.format("Camera API lv2?: %s", useCamera2API) }
                return Pair.create(cameraId, useCamera2API)
            }
        } catch (e: CameraAccessException) {
            Timber.e(e, "Not allowed to access camera")
        }
        return null
    }

    @JvmStatic
    // Returns true if the device supports the required hardware level, or better.
    private fun isHardwareLevelSupported(
        characteristics: CameraCharacteristics,
        requiredLevel: Int
    ): Boolean {
        val deviceLevel =
            characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) ?: return false
        return if (deviceLevel == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            requiredLevel == deviceLevel
        } else requiredLevel <= deviceLevel
        // deviceLevel is not LEGACY, can use numerical sort
    }

    @JvmStatic
    fun getFrontFacingCameraId(context: Context): String? {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
            ?: return null
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)
                val cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (cOrientation == CameraMetadata.LENS_FACING_FRONT) return cameraId
            }
        } catch (e: CameraAccessException) {
            Timber.e(e)
        }
        return null
    }
}