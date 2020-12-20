package ru.igla.tfprofiler.ui

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.MaterialDialog
import org.tensorflow.lite.TensorFlowLite
import ru.igla.tfprofiler.BuildConfig
import ru.igla.tfprofiler.R
import java.util.*

class AboutDialog : DialogFragment() {

    companion object {
        private const val TAG = "[ABOUT_DIALOG]"

        fun show(context: FragmentActivity) =
            AboutDialog().show(context.supportFragmentManager, TAG)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity ?: throw IllegalStateException("No activity provided!")
        val realUrlLauncher = RealUrlLauncher(requireContext())

        val body = String.format(
            Locale.US,
            getString(R.string.about_body),
            TensorFlowLite.runtimeVersion()
        )
        return MaterialDialog(context)
            .title(text = getString(R.string.about_title, BuildConfig.VERSION_NAME))
            .message(text = body) {
                html { realUrlLauncher.viewUrl(it) }
                lineSpacing(1.4f)
            }
            .positiveButton(R.string.dismiss)
    }
}