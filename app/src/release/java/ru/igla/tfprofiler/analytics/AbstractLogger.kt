package ru.igla.tfprofiler.analytics

import ru.igla.tfprofiler.utils.StringUtils
import java.util.*

/**
 * Created with IntelliJ IDEA.
 * User: igor-lashkov
 * Date: 10/03/14
 * Time: 20:36
 */
open class AbstractLogger protected constructor() : ILogger {
    private val trackers: MutableList<ILogger>

    /***
     * Register tracker
     *
     * @param obj tracker to register
     */
    @Synchronized
    open fun register(obj: ILogger) {
        if (!trackers.contains(obj)) {
            trackers.add(obj)
        }
    }

    open fun unregisterAll() {
        trackers.clear()
    }

    open fun trackJsonEvent(resId: String, jsonValue: String) {
        logEvent(resId, jsonValue)
    }

    @Synchronized
    override fun logEvent(eventName: String) {
        for (logger in trackers) {
            logger.logEvent(eventName)
        }
    }

    override fun logEvent(eventName: String, jsonValue: String) {
        for (logger in trackers) {
            logger.logEvent(eventName, jsonValue)
        }
    }

    @Synchronized
    override fun logEvent(eventName: String, params: Map<String, Any>) {
        for (logger in trackers) {
            logger.logEvent(eventName, params)
        }
    }

    override fun logError(message: String, throwable: Throwable) {
        val nonEmptyMsg = if (StringUtils.isNullOrEmpty(message)) "empty" else message
        for (tracker in trackers) {
            tracker.logError(nonEmptyMsg, throwable)
        }
    }

    private class LoggerHolder private constructor() {
        companion object {
            val instance = AbstractLogger()
        }

        init {
            throw IllegalAccessError("Helper class")
        }
    }

    companion object {
        val instance: AbstractLogger
            get() = LoggerHolder.instance
    }

    init {
        trackers = ArrayList()
    }
}