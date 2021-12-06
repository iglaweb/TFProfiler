package ru.igla.tfprofiler.ui.widgets.toastcompat

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import ru.igla.tfprofiler.utils.StringUtils.isNullOrEmpty

/**
 * Simple util for showing Toast. Do not duplicate Toast, if it is already running.
 */
class Toaster(private val mContext: Context) {

    private var mToast: ToastCompat? = null
    private var mCurrentText: String? = null

    private fun isActivityAlive(): Boolean {
        if (mContext is Activity) {
            return !mContext.isFinishing
        }
        return true
    }

    private fun showToast(text: String, length: Int) {
        if (!isActivityAlive()) {
            return
        }
        val toastView = mToast?.view
        if (mToast == null || toastView != null && !toastView.isShown) {
            makeAndShowNewToast(text, length)
        } else if (!isSameToast(text)) {
            mToast?.cancel()
            makeAndShowNewToast(text, length)
        }
    }

    private fun makeAndShowNewToast(text: String, length: Int) {
        mToast = ToastCompat.makeText(mContext, text, length)
        mToast?.show()
        mCurrentText = text
    }

    private fun isSameToast(text: String): Boolean {
        return mToast != null &&
                !isNullOrEmpty(mCurrentText) && mCurrentText == text
    }

    /**
     * Show Toast with "long" duration.
     */
    fun showToast(@StringRes stringId: Int) {
        val text = mContext.resources.getString(stringId)
        showToast(text)
    }

    /**
     * Show Toast with "long" duration.
     */
    fun showToast(text: String) {
        showToast(text, Toast.LENGTH_LONG)
    }

    /**
     * Show Toast with "short" duration.
     */
    fun showShortToast(text: String) {
        showToast(text, Toast.LENGTH_SHORT)
    }

    fun cancel() {
        mToast?.cancel()
    }
}