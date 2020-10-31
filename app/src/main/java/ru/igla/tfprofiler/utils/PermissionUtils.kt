package ru.igla.tfprofiler.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object PermissionUtils {
    const val PERMISSION_CAMERA = Manifest.permission.CAMERA

    @JvmStatic
    fun allPermissionsGranted(grantResults: IntArray): Boolean {
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    @JvmStatic
    fun hasCameraPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}