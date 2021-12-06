package ru.igla.tfprofiler.ui.widgets.toastcompat

import android.widget.Toast

/**
 * @author drakeet
 */
interface BadTokenListener {
    fun onBadTokenCaught(toast: Toast)
}