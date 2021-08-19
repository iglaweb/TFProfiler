package ru.igla.tfprofiler.utils

import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import timber.log.Timber


/**
 * Created by igor-lashkov on 27/11/2017.
 */

object ViewUtils {

    private val uiHandler = Handler(Looper.getMainLooper())

    @JvmStatic
    fun runOnUiThread(action: () -> Unit) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            action()
        } else {
            uiHandler.post { action() }
        }
    }

    @JvmStatic
    fun stopHandler(backgroundHandler: Handler?) {
        backgroundHandler?.looper?.apply {
            quitSafely()
        }
    }

    @JvmStatic
    fun cancelCallbacks() {
        uiHandler.removeCallbacksAndMessages(null)
    }

    @JvmStatic
    fun cancelCallback(runnable: Runnable) {
        uiHandler.removeCallbacks(runnable)
    }

    @JvmStatic
    fun dismissDialogSafety(dialog: DialogInterface?) {
        dialog?.let {
            runOnUiThread {
                try {
                    it.dismiss()
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }
}