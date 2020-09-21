package ru.igla.tfprofiler.prefs

import android.content.Context
import android.content.SharedPreferences

abstract class AndroidSystemPrefs(val context: Context) : PrefsBase() {

    abstract val prefName: String

    private val sharedPreferences: SharedPreferences by lazy { getSharedPrefs(prefName, context) }

    companion object {
        @JvmStatic
        fun getSharedPrefs(prefName: String, context: Context): SharedPreferences {
            return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        }
    }

    override fun getStringValue(key: String, defaultValue: String?): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    override fun getBoolValue(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    override fun getIntValue(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    override fun getLongValue(key: String, defaultValue: Long): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    override fun getFloatValue(key: String, defaultValue: Float): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }

    override fun editBoolValue(key: String, value: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean(key, value)
            apply()
        }
    }

    override fun editIntValue(key: String, value: Int) {
        sharedPreferences.edit().apply {
            putInt(key, value)
            apply()
        }
    }

    override fun editLongValue(key: String, value: Long) {
        sharedPreferences.edit().apply {
            putLong(key, value)
            apply()
        }
    }

    override fun editFloatValue(key: String, value: Float) {
        sharedPreferences.edit().apply {
            putFloat(key, value)
            apply()
        }
    }

    override fun editStringValue(key: String, value: String?) {
        sharedPreferences.edit().apply {
            putString(key, value)
            apply()
        }
    }
}