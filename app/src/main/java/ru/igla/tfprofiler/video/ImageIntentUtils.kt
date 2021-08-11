package ru.igla.tfprofiler.video

import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import ru.igla.tfprofiler.core.intents.IntentManager
import ru.igla.tfprofiler.utils.DateUtils
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by igor-lashkov on 15/11/2017.
 */

object ImageIntentUtils {

    const val SELECT_PICTURE_REQUEST_CODE = 1024

    fun getMimeType(context: Context, uri: Uri): String? {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val cr = context.contentResolver
            cr?.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(
                uri
                    .toString()
            )
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.lowercase(Locale.getDefault())
            )
        }
    }

    fun openImageIntent(intentManager: IntentManager): Uri {
        // Determine Uri of camera image to save.
        val path = FileUtils.getMediaPath(intentManager.context)
        val root = File("$path/")
        root.mkdirs()
        val fileName = "JPEG_" + DateUtils.getCurrentDateInMs() + ".jpg"
        val sdImageMainDirectory = File(root, fileName)
        val outputFileUri = Uri.fromFile(sdImageMainDirectory)

        // Camera.
        val cameraIntents = ArrayList<Intent>()
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val packageManager = intentManager.context.packageManager
        val listCameraIntents = packageManager.queryIntentActivities(captureIntent, 0)
        for (res in listCameraIntents) {
            val packageName = res.activityInfo.packageName
            val intent = Intent(captureIntent)
            intent.apply {
                component = ComponentName(packageName, res.activityInfo.name)
                `package` = packageName
                putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
            }
            cameraIntents.add(intent)
        }

        // Filesystem.
        val galleryIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
            }

        // Chooser of filesystem options.
        val chooserIntent = Intent.createChooser(galleryIntent, "Select Image/Video")
        if (cameraIntents.isNotEmpty()) {
            chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS,
                cameraIntents.toTypedArray<Parcelable>()
            )
        }
        intentManager.startActivityForResult(chooserIntent, SELECT_PICTURE_REQUEST_CODE)
        return outputFileUri
    }
}