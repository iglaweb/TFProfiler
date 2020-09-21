package ru.igla.tfprofiler.core.analytics

import android.content.Context
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import ru.igla.tfprofiler.utils.SystemUtils
import ru.igla.tfprofiler.utils.TimeWatchClockOS
import ru.igla.tfprofiler.utils.logD
import java.util.concurrent.Executors

class AppMemoryEstimator(val context: Context) {

    /***
     * Use separated persistent thread
     */
    private val dispatcherBg by lazy {
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    }

    private val timeWatchClockOS by lazy {
        TimeWatchClockOS()
    }

    var lastMemoryUsage = -1L

    @Volatile
    var isRunning = false

    fun getMemory(): Long {
        if (isRunning) {
            return lastMemoryUsage
        }

        GlobalScope.launch(dispatcherBg) {
            try {
                isRunning = true
                timeWatchClockOS.start()
                lastMemoryUsage = SystemUtils.getProcessMemoryInfo(context.applicationContext)
                logD {
                    "Memory measure elapsed: " + timeWatchClockOS.stop() + " ms"
                }
            } finally {
                isRunning = false
            }
        }
        return lastMemoryUsage
    }
}