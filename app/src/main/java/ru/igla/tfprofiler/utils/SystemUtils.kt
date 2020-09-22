package ru.igla.tfprofiler.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.os.Debug
import android.os.Process
import androidx.annotation.WorkerThread


object SystemUtils {

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