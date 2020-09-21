package ru.igla.tfprofiler.core.intents

import android.app.Activity
import android.content.Context
import android.content.Intent
import ru.igla.tfprofiler.utils.IntentUtils

/**
 * Created by lashkov on 18/11/16.
 * Copyright (c) 2016 igla LLC. All rights reserved.
 */
internal class ActivityIntentWrapper(private val activity: Activity) : IntentWrapper {
    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        IntentUtils.startActivityForResultSafely(activity, requestCode, intent)
    }

    override val context: Context
        get() = activity
}