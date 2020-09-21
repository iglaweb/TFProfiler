package ru.igla.tfprofiler.core.intents

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment

/**
 * Created by lashkov on 16/11/16.
 * Copyright (c) 2016 igla LLC. All rights reserved.
 */
class IntentManager private constructor(private val permissionWrapper: IntentWrapper) {
    val context: Context
        get() = permissionWrapper.context

    @TargetApi(Build.VERSION_CODES.M)
    fun startActivityForResult(@RequiresPermission intent: Intent, requestCode: Int) {
        permissionWrapper.startActivityForResult(intent, requestCode)
    }

    companion object {
        fun create(activity: Activity): IntentManager {
            return IntentManager(ActivityIntentWrapper(activity))
        }

        fun create(fragment: Fragment): IntentManager {
            return IntentManager(FragmentIntentWrapper(fragment))
        }
    }
}