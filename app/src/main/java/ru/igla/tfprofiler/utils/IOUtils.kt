package ru.igla.tfprofiler.utils

import android.content.Context
import android.os.Environment
import ru.igla.tfprofiler.utils.StringUtils.isNullOrEmpty
import java.io.Closeable
import java.io.File

object IOUtils {

    private fun combinePaths(vararg paths: String): String {
        if (paths.isEmpty()) return ""
        var file = File(paths[0])
        for (i in 1 until paths.size) {
            file = File(file, paths[i])
        }
        return file.path
    }

    private fun getFilesDirAbsolutePath(context: Context, basePath: String): String {
        return combinePaths(
            basePath,
            "/Android/data/",
            context.packageName,
            "/files/"
        )
    }

    private fun getExternalFileDir(c: Context): String? {
        val file = c.getExternalFilesDir(null) ?: return null
        val path = file.absolutePath //we can receive with missing '/' in the end of path
        return if (!path.endsWith("/")) {
            "$path/"
        } else path
        //return file sdcard directory
    }

    fun getAppSpecificFilesDirAbsolutePath(context: Context): String? {
        val externalDir = getExternalFileDir(context)
        return if (isNullOrEmpty(externalDir)) {
            getFilesDirAbsolutePath(
                context,
                baseExternalStoragePath
            )
        } else externalDir
    }

    private val baseExternalStoragePath: String
        get() = Environment.getExternalStorageDirectory().absolutePath

    /**
     * Closes 'closeable', ignoring any checked exceptions. Does nothing if 'closeable' is null.
     */
    @JvmStatic
    fun closeQuietly(closeable: Closeable?) {
        closeable?.let {
            try {
                it.close()
            } catch (ignored: Exception) {
                //ignore
            }
        }
    }
}