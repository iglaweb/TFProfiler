package ru.igla.tfprofiler.media_track

import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import ru.igla.tfprofiler.utils.PathUtil
import ru.igla.tfprofiler.video.FileUtils
import java.util.*

object MediaTrackUtils {
    private val okFileExtensions = arrayOf(
        "jpg",
        "png",
        "gif",
        "jpeg"
    )

    fun isImage(path: String): Boolean {
        for (extension in okFileExtensions) {
            if (path.toLowerCase(Locale.US).endsWith(extension)) {
                return true
            }
        }
        return false
    }

    @WorkerThread
    fun getRealFilePath(context: Context, selectedImageUri: Uri): String {
        val path = PathUtil.getPath(context, selectedImageUri)
        if (path.isNullOrEmpty()) {
            //copy file to output directory on sdcard
            return FileUtils.tryGetRealPathFromURI(context, selectedImageUri)
                ?: return FileUtils.copyFileByUri(context, selectedImageUri)
        }
        return path
    }
}