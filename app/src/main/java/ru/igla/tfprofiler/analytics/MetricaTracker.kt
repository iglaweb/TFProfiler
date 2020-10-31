package ru.igla.tfprofiler.analytics

import android.app.Application
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig

/**
 * Created with IntelliJ IDEA.
 * User: igor-lashkov
 * Date: 6/29/13
 * Time: 12:35 AM
 */
class MetricaTracker(application: Application, apiKey: String) : ILogger {
    override fun logEvent(eventName: String) {
        YandexMetrica.reportEvent(eventName)
    }

    override fun logEvent(eventName: String, jsonValue: String) {
        YandexMetrica.reportEvent(eventName, jsonValue)
    }

    override fun logEvent(eventName: String, params: Map<String, Any>) {
        YandexMetrica.reportEvent(eventName, params)
    }

    override fun logError(message: String, throwable: Throwable) {
        YandexMetrica.reportError(message, throwable)
    }

    init {
        val configBuilder = YandexMetricaConfig.newConfigBuilder(apiKey)
        YandexMetrica.activate(application, configBuilder.build())
        YandexMetrica.enableActivityAutoTracking(application)
    }
}