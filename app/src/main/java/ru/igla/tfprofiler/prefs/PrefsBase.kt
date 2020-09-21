package ru.igla.tfprofiler.prefs


abstract class PrefsBase {

    var ignorePrefChanges = false

    abstract fun getBoolValue(key: String, defaultValue: Boolean): Boolean

    abstract fun getIntValue(key: String, defaultValue: Int): Int

    abstract fun getFloatValue(key: String, defaultValue: Float): Float

    abstract fun getLongValue(key: String, defaultValue: Long): Long

    abstract fun getStringValue(key: String, defaultValue: String?): String?

    abstract fun editBoolValue(key: String, value: Boolean)

    abstract fun editIntValue(key: String, value: Int)

    abstract fun editFloatValue(key: String, value: Float)

    abstract fun editLongValue(key: String, value: Long)

    abstract fun editStringValue(key: String, value: String?)

    abstract fun clearCurrentSettings()
}