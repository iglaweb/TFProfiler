package ru.igla.tfprofiler.core

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ErrorDialog(private val listener: DialogInterface.OnClickListener? = null) :
    DialogFragment() {

    companion object {
        private const val ARG_MESSAGE = "message"

        @JvmStatic
        fun newInstance(
            message: String?,
            listener: DialogInterface.OnClickListener? = null
        ): ErrorDialog {
            val dialog = ErrorDialog(listener)
            val args = Bundle()
            args.putString(ARG_MESSAGE, message)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity: Activity? = activity
        return AlertDialog.Builder(activity)
            .setMessage(arguments?.getString(ARG_MESSAGE))
            .setPositiveButton(
                android.R.string.ok
            ) { dialogInterface, i ->
                listener?.onClick(dialogInterface, i) ?: dialogInterface.dismiss()
            }
            .create()
    }
}