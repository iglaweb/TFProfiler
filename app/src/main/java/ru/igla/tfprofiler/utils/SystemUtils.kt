package ru.igla.tfprofiler.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.pm.FeatureInfo
import android.content.pm.PackageManager
import android.os.Debug
import android.os.Process
import androidx.annotation.WorkerThread


object SystemUtils {

    fun rnd2Percent(num: Float): Int {
        return (num * 100f).toInt()
    }

    /***
     * https://android.googlesource.com/platform/cts/+/f1b6c7d504ae31f3500ce5c9b2e75dadb2799f4d/tests/tests/opengl/src/android/opengl/cts/OpenGlEsVersionTest.java
     */
    fun getGLESVersionFromPackageManager(context: Context): Int {
        val packageManager: PackageManager = context.packageManager
        val featureInfos = packageManager.systemAvailableFeatures
        if (featureInfos.isNotEmpty()) {
            for (featureInfo in featureInfos) {
                // Null feature name means this feature is the open gl es version feature.
                if (featureInfo.name == null) {
                    return if (featureInfo.reqGlEsVersion != FeatureInfo.GL_ES_VERSION_UNDEFINED) {
                        featureInfo.reqGlEsVersion
                    } else {
                        1 shl 16 // Lack of property means OpenGL ES version 1
                    }
                }
            }
        }
        return 1
    }

    /** @see FeatureInfo.getGlEsVersion
     */
    fun getMajorVersionGLES(glEsVersion: Int): Int {
        return glEsVersion and -0x10000 shr 16
    }

    /** @see FeatureInfo.getGlEsVersion
     */
    fun getMinorVersionGLES(glEsVersion: Int): Int {
        return glEsVersion and 0xffff
    }

    /**
     * Returns the available ammount of RAM of your Android device in Bytes e.g 1567342592 (1.5GB)
     * @return {Long}
     */
    fun getMemorySizeInBytes(context: Context): Long {
        val activityManager =
            context.applicationContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager?
                ?: return -1L
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.totalMem
    }

    @JvmStatic
    @WorkerThread
    fun getProcessMemoryInfo(context: Context): Long {
        val activityManager =
            context.getSystemService(ACTIVITY_SERVICE) as ActivityManager? ?: return -1L

        val id = Process.myPid()
        val memoryInfo: Array<Debug.MemoryInfo> =
            activityManager.getProcessMemoryInfo(intArrayOf(id))
        if (memoryInfo.isNotEmpty()) {
            val memInfo = memoryInfo[0]
            Log.d(
                "",
                "getProcessMemoryInfo: myMemoryInfo " + memInfo.dalvikPrivateDirty
            )
            var res = memInfo.totalPrivateDirty
            res += memInfo.totalPrivateClean
            return res * 1024L
        }
        return -1L
    }
}