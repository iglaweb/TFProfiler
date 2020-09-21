package ru.igla.tfprofiler.media_track

import android.content.Context
import ru.igla.tfprofiler.utils.IOUtils
import java.io.File

object MediaPathProvider {

    fun isMediaStoragePath(context: Context, path: String): Boolean {
        val diskPath = getPath(context, "").trim()
        return path.trim().startsWith(diskPath)
    }

    private fun getPath(context: Context, postfix: String): String {
        val externalFilesDir =
            IOUtils.getAppSpecificFilesDirAbsolutePath(context.applicationContext)
        val modelDir = File(externalFilesDir, postfix)
        if (!modelDir.exists()) {
            modelDir.mkdirs()
        }
        return modelDir.absolutePath
    }

    fun getRootPath(context: Context): String {
        return getPath(context, "")
    }

    @JvmStatic
    fun getMediaPath(context: Context): String {
        return getPath(context, "/media")
    }

    @JvmStatic
    fun getCustomModelsPath(context: Context): String {
        return getPath(context, "/custom_models")
    }
}