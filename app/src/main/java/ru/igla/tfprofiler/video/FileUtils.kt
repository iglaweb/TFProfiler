package ru.igla.tfprofiler.video

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.WorkerThread
import ru.igla.tfprofiler.TFProfilerApp.Companion.instance
import ru.igla.tfprofiler.core.Timber
import ru.igla.tfprofiler.utils.IOUtils
import ru.igla.tfprofiler.utils.PathUtil
import java.io.*
import java.util.*

object FileUtils {

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
            return tryGetRealPathFromURI(context, selectedImageUri)
                ?: return copyFileByUri(context, selectedImageUri)
        }
        return path
    }

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
        return getPath(context, "/models")
    }

    private fun tryGetRealPathFromURI(context: Context?, contentURI: Uri): String? {
        val cursor = context?.contentResolver?.query(contentURI, null, null, null, null)
        return if (cursor == null) { // Source is Dropbox or other similar local file path
            contentURI.path
        } else {
            cursor.use {
                cursor.moveToFirst()
                val idx =
                    cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA) //we cannot get physical address of a file (Picassa folder)
                if (idx == -1) return null
                cursor.getString(idx)
            }
        }
    }

    private fun copyFileByUri(context: Context, uri: Uri, filename: String = "temp_video.mp4"): String {
        val sourceFilename: String = RealPathUtil.getRealPath(context, uri)
        val destinationFilename = getMediaPath(context) + "/" + filename
        return copyFile(sourceFilename, destinationFilename)
    }

    fun copyFile(
        sourceFilename: String,
        destinationFilename: String
    ): String {
        //delete temp file if exists
        val file = File(destinationFilename)
        if (file.exists()) {
            file.delete()
        }

        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null
        try {
            bis = BufferedInputStream(FileInputStream(sourceFilename))
            bos = BufferedOutputStream(FileOutputStream(destinationFilename, false))
            val buf = ByteArray(1024)
            bis.read(buf)
            do {
                bos.write(buf)
            } while (bis.read(buf) != -1)
        } catch (e: IOException) {
            Timber.e(e)
        } finally {
            IOUtils.closeQuietly(bis)
            IOUtils.closeQuietly(bos)
        }
        return destinationFilename
    }

    fun writeBitmapExternalStorage(filename: String, bmp: Bitmap): Boolean {
        val file = File(getRootPath(instance), filename)
        try {
            FileOutputStream(file).use { outputStream ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
            }
        } catch (e: Exception) {
            Timber.w(e.message)
            return false
        }
        return true
    }
}