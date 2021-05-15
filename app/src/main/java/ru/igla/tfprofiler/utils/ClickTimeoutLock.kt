package ru.igla.tfprofiler.utils

import android.os.Handler
import android.os.Looper

object ClickTimeoutLock {
    private const val DEFAULT_TIMEOUT = 200L
    private const val MSG_LOCK = 0

    private val lockHandler: Handler by lazy { Handler(Looper.getMainLooper()) }

    @Synchronized
    fun canProceedClick(): Boolean {
        if (isLocked) {
            return false
        }
        lock()
        return true
    }

    @get:Synchronized
    val isLocked: Boolean
        get() = lockHandler.hasMessages(MSG_LOCK)

    @Synchronized
    fun lock() {
        lock(DEFAULT_TIMEOUT)
    }

    @Synchronized
    fun lock(lockTime: Long) {
        lockHandler.sendEmptyMessageDelayed(MSG_LOCK, lockTime)
    }

    @Synchronized
    fun unlock() {
        lockHandler.removeMessages(MSG_LOCK)
    }
}