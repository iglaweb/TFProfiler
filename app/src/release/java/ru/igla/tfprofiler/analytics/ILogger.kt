package ru.igla.tfprofiler.analytics

/**
 * Created with IntelliJ IDEA.
 * User: igor-lashkov
 * Date: 10/03/20
 * Time: 20:43
 */
interface ILogger {
    fun logEvent(eventName: String)
    fun logEvent(eventName: String, jsonValue: String)
    fun logEvent(eventName: String, params: Map<String, Any>)
    fun logError(message: String, throwable: Throwable)
}