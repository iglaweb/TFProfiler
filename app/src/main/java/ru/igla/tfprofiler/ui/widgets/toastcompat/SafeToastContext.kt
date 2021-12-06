package ru.igla.tfprofiler.ui.widgets.toastcompat

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.WindowManager.BadTokenException
import android.widget.Toast

/**
 * @author drakeet
 */
internal class SafeToastContext(base: Context, private val toast: Toast) : ContextWrapper(base) {
    private var badTokenListener: BadTokenListener? = null

    override fun getApplicationContext(): Context {
        return ApplicationContextWrapper(baseContext.applicationContext)
    }

    fun setBadTokenListener(badTokenListener: BadTokenListener) {
        this.badTokenListener = badTokenListener
    }

    private inner class ApplicationContextWrapper constructor(base: Context) :
        ContextWrapper(base) {
        override fun getSystemService(name: String): Any {
            return if (WINDOW_SERVICE == name) {
                // noinspection ConstantConditions
                WindowManagerWrapper(baseContext.getSystemService(name) as WindowManager)
            } else super.getSystemService(name)
        }
    }

    private inner class WindowManagerWrapper constructor(private val base: WindowManager) :
        WindowManager {
        override fun getDefaultDisplay(): Display {
            return base.defaultDisplay
        }

        override fun removeViewImmediate(view: View) {
            base.removeViewImmediate(view)
        }

        override fun addView(view: View, params: ViewGroup.LayoutParams) {
            try {
                Log.d(TAG, "WindowManager's addView(view, params) has been hooked.")
                base.addView(view, params)
            } catch (e: BadTokenException) {
                Log.i(TAG, e.message!!)
                badTokenListener?.onBadTokenCaught(toast)
            } catch (throwable: Throwable) {
                Log.e(TAG, "[addView]", throwable)
            }
        }

        override fun updateViewLayout(view: View, params: ViewGroup.LayoutParams) {
            base.updateViewLayout(view, params)
        }

        override fun removeView(view: View) {
            base.removeView(view)
        }
    }

    companion object {
        private const val TAG = "WindowManagerWrapper"
    }
}