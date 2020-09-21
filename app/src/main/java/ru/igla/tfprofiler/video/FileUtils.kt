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
        val result: String?
        val cursor = context?.contentResolver?.query(contentURI, null, null, null, null)
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.path
        } else {
            cursor.moveToFirst()
            val idx =
                cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA) //we cannot get physical address of a file (Picassa folder)
            if (idx == -1) return null
            result = cursor.getString(idx)
            IOUtils.closeQuietly(cursor)
        }
        return result
    }

    fun copyFileByUri(context: Context, uri: Uri, filename: String = "temp_video.mp4"): String {
        val sourceFilename: String = RealPathUtil.getRealPath(context, uri)
        val destinationFilename = MediaPathProvider.getMediaPath(context) + "/" + filename

        //delete temp file if exists
        val file = File(destinationFilename)
        if (file.exists()) {
            file.delete()
        }
        return copyFile(sourceFilename, destinationFilename)
    }

    fun copyFile(
        sourceFilename: String,
        destinationFilename: String
    ): String {
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