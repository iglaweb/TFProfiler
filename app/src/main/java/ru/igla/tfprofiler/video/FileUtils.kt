package ru.igla.tfprofiler.video

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import ru.igla.tfprofiler.core.Timber
import ru.igla.tfprofiler.media_track.MediaPathProvider
import ru.igla.tfprofiler.utils.IOUtils
import java.io.*

object FileUtils {

    fun tryGetRealPathFromURI(context: Context?, contentURI: Uri): String? {
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

    fun copyFileByUri(context: Context, uri: Uri, filename: String = "temp_video.mp4"): String {
        val sourceFilename: String = RealPathUtil.getRealPath(context, uri)
        val destinationFilename = MediaPathProvider.getMediaPath(context) + "/" + filename
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
}