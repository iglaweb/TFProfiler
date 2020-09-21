package ru.igla.tfprofiler.utils

import ru.igla.tfprofiler.core.Timber.Log.DEBUG
import ru.igla.tfprofiler.core.Timber.Log.ERROR
import ru.igla.tfprofiler.core.Timber.Log.WARN
import ru.igla.tfprofiler.core.Tree

/**
 * A tree which logs important information for crash reporting.
 */
class DevelopReportingTree : Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        when (priority) {
            ERROR -> {
                Log.e(t)
            }
            WARN -> {
                Log.w(message)
            }
            DEBUG -> {
                Log.d(message)
            }
            else -> {
                Log.i(message)
            }
        }
    }
}