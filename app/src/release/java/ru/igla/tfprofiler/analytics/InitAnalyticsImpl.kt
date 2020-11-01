package ru.igla.tfprofiler.analytics

import android.app.Application
import ru.igla.tfprofiler.BuildConfig
import ru.igla.tfprofiler.core.InitAnalytics

class InitAnalyticsImpl : InitAnalytics {
    override fun init(app: Application) {
        AndroidLogger.instance.apply {
            val metricaTracker: ILogger =
                MetricaTracker(app, BuildConfig.METRICA_KEY)
            register(metricaTracker)
            val crashlyticsTracker: ILogger = CrashlyticsTracker(app)
            register(crashlyticsTracker)
        }
    }
}