package ru.igla.tfprofiler.ui.pick_inference_type

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.annotation.UiThread
import ru.igla.tfprofiler.R
import ru.igla.tfprofiler.core.RequestMode
import ru.igla.tfprofiler.utils.sentenceCase

object InferenceTypeLauncher {

    enum class InferenceType(val humanName: String) {
        TFLITE("TFLite"),
        OPENCV("OpenCV")
    }

    @Suppress("DEPRECATION")
    val windowOverlayType = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
        WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
    else
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

    @UiThread
    fun showInferenceTypeDialog(
        context: Context,
        listener: InferenceLaunchListener
    ) {
        val launchOptions = listOf(
            InferenceType.TFLITE.humanName,
            InferenceType.OPENCV.humanName
        )
        showDialog(context, launchOptions) { _, item ->
            val selectedOption = InferenceType.values()[item]
            listener.onSelectedOption(selectedOption)
        }
    }

    @UiThread
    fun showImageTypeDialog(
        context: Context,
        listener: ImageRequestListener
    ) {
        val launchOptions = listOf(
            RequestMode.CAMERA.name.sentenceCase(),
            RequestMode.VIDEO.name.sentenceCase(),
            RequestMode.DATASET.name.sentenceCase()
        )
        showDialog(context, launchOptions) { _, item ->
            val selectedOption = RequestMode.values()[item]
            listener.onSelectedOption(selectedOption)
        }
    }

    private fun showDialog(
        context: Context,
        packages: List<String>,
        listener: DialogInterface.OnClickListener
    ) {
        Handler(Looper.getMainLooper()).post {
            val adapter = InferenceListArrayAdapter(
                context,
                R.layout.select_dialog_item,
                R.id.text1,
                packages
            )
            val alertDialog = AlertDialog.Builder(context)
                .setTitle("Choose type to run inference")
                .setAdapter(adapter, listener)
                .create()
            if (context !is Activity) {
                val window = alertDialog.window
                window?.setType(windowOverlayType)
            }
            alertDialog.show()
        }
    }
}