package ru.igla.tfprofiler.analytics

import android.os.Handler
import android.os.Looper

/**
 * Created with IntelliJ IDEA.
 * User: igor-lashkov
 * Date: 10/03/14
 * Time: 20:36
 */
class AndroidLogger private constructor() : AbstractLogger() {
    private val handler: Handler = Handler(Looper.getMainLooper())

    /***
     * Register tracker
     *
     * @param obj tracker to register
     */
    @Synchronized
    override fun register(obj: ILogger) {
        handler.post { super@AndroidLogger.register(obj) }
    }

    override fun unregisterAll() {
        handler.removeCallbacksAndMessages(null)
        handler.post { super@AndroidLogger.unregisterAll() }
    }

    override fun trackJsonEvent(resId: String, jsonValue: String) {
        logEvent(resId, jsonValue)
    }

    @Synchronized
    override fun logEvent(eventName: String) {
        handler.post { super@AndroidLogger.logEvent(eventName) }
    }

    override fun logEvent(eventName: String, jsonValue: String) {
        handler.post { super@AndroidLogger.logEvent(eventName, jsonValue) }
    }

    @Synchronized
    override fun logEvent(eventName: String, params: Map<String, Any>) {
        handler.post { super@AndroidLogger.logEvent(eventName, params) }
    }

    override fun logError(message: String, throwable: Throwable) {
        handler.post { super@AndroidLogger.logError(message, throwable) }
    }

    private class LoggerHolder private constructor() {
        companion object {
            val instance = AndroidLogger()
        }

        init {
            throw IllegalAccessError("Helper class")
        }
    }

    companion object {
        val instance: AndroidLogger
            get() = LoggerHolder.instance
    }

}