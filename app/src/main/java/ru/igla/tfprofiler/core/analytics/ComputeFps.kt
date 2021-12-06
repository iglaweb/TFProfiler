package ru.igla.tfprofiler.core.analytics

import android.os.SystemClock
import ru.igla.tfprofiler.utils.round
import java.util.*


class ComputeFps {

    private val frameRateWindow = 100
    private val frameTimestamps by lazy {
        ArrayDeque<Long>(frameRateWindow)
    }

    fun calcFps(): Double {
        // Keep track of frames analyzed
        val currentTimeMs = SystemClock.elapsedRealtime()
        frameTimestamps.addFirst(currentTimeMs)

        // Compute the FPS using a moving average
        while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
        val timestampFirst = frameTimestamps.peekFirst() ?: currentTimeMs
        val timestampLast = frameTimestamps.peekLast() ?: currentTimeMs
        val deltaTime = kotlin.math.abs(timestampFirst - timestampLast)
        val avgFrameTime = deltaTime / frameTimestamps.size.coerceAtLeast(1)
        if (avgFrameTime < 1) return 1000.0
        val fps = 1000.0 / avgFrameTime
        return fps.round(2)
    }

    fun clear() {
        frameTimestamps.clear()
    }
}