package ru.igla.tfprofiler.core.intents

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import ru.igla.tfprofiler.utils.IntentUtils

/**
 * Created by lashkov on 18/11/16.
 * Copyright (c) 2016 igla LLC. All rights reserved.
 */
internal class FragmentIntentWrapper(private val fragment: Fragment) :
    IntentWrapper {
    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        IntentUtils.startFragmentForResultSafely(fragment, requestCode, intent)
    }

    override val context: Context
        get() = fragment.requireActivity()
}