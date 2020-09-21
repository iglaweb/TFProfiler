package ru.igla.tfprofiler.core.intents

import android.content.Context
import android.content.Intent

/**
 * Created by lashkov on 18/11/16.
 * Copyright (c) 2016 igla LLC. All rights reserved.
 */
interface IntentWrapper {
    fun startActivityForResult(intent: Intent, requestCode: Int)
    val context: Context
}