package ru.igla.tfprofiler.core.analytics

import ru.igla.tfprofiler.utils.DateUtils
import ru.igla.tfprofiler.utils.round
import java.util.*


class ComputeFps {

    private val frameRateWindow = 100
    private val frameTimestamps = ArrayDeque<Long>(frameRateWindow)

    fun calcFps(): Double {
        // Keep track of frames analyzed
        val currentTime = DateUtils.getCurrentDateInMs()
        frameTimestamps.addFirst(currentTime)

        // Compute the FPS using a moving average
        while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
        val timestampFirst = frameTimestamps.peekFirst() ?: currentTime
        val timestampLast = frameTimestamps.peekLast() ?: currentTime
        val deltaTime = timestampFirst - timestampLast
        val avgFrameTime = deltaTime / frameTimestamps.size.coerceAtLeast(1)
        val fps = 1000.0 / avgFrameTime
        return fps.round(2)
    }

    fun clear() {
        frameTimestamps.clear()
    }
}