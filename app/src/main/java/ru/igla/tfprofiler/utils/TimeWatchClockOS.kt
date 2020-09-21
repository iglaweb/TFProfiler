package ru.igla.tfprofiler.utils

import android.os.SystemClock

class TimeWatchClockOS {
    private var timeStart = 0L
    private var timeElapsed = 0L
    private var isStarted = false

    fun start() {
        isStarted = true
        timeStart = SystemClock.elapsedRealtime()
        timeElapsed = 0L
    }

    fun stop(): Long {
        if (isStarted) { //return fixed time
            timeElapsed = SystemClock.elapsedRealtime() - timeStart
            isStarted = false
        }
        return timeElapsed
    }
}