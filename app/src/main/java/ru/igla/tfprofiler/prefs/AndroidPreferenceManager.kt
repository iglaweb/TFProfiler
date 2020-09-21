package ru.igla.tfprofiler.prefs

import android.content.Context

class AndroidPreferenceManager constructor(
    context: Context
) : IPreferenceManager {

    private val androidPrefs = object : AndroidSystemPrefs(context) {
        override val prefName: String
            get() = AppConfigurePrefs.PREFS_NAME
    }
    override val defaultPrefs: AppConfigurePrefs by lazy {
        AppConfigurePrefs(androidPrefs)
    }
}