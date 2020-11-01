package ru.igla.tfprofiler.analytics

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Created with IntelliJ IDEA.
 * User: igor-lashkov
 * Date: 6/29/13
 * Time: 12:35 AM
 */
class CrashlyticsTracker(application: Application) : ILogger {
    override fun logEvent(eventName: String) {
        //no impl
    }

    override fun logEvent(eventName: String, jsonValue: String) {
        //no impl
    }
    override fun logEvent(eventName: String, params: Map<String, Any>) {
        //no impl
    }

    override fun logError(message: String, throwable: Throwable) {
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.recordException(throwable)
    }
}