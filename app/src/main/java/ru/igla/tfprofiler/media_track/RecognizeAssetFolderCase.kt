package ru.igla.tfprofiler.media_track

import android.content.Context
import android.graphics.BitmapFactory
import kotlinx.coroutines.*
import ru.igla.tfprofiler.core.Timber
import ru.igla.tfprofiler.core.UseCase
import ru.igla.tfprofiler.video.TimestampBitmap
import java.io.IOException
import kotlin.coroutines.coroutineContext

class RecognizeAssetFolderCase(
    private val onReadAssetImageCallback: OnReadAssetImageCallback
) {

    interface OnReadAssetImageCallback {
        fun onReadAssetImage(timestampBitmap: TimestampBitmap)
        fun onProgress(progress: FrameInformation)
    }

    private suspend fun getImageList(list: Array<String>): MutableList<String> {
        val imageList = mutableListOf<String>()
        list.forEach {
            if (!coroutineContext.isActive) {
                throw CancellationException()
            }
            val isImage = MediaTrackUtils.isImage(it)
            if (isImage) imageList.add(it)
        }
        return imageList
    }

    @Throws(java.lang.Exception::class)
    suspend fun iterateAssetsFiles(context: Context, imagesFolder: String): UseCase.Status {
        return withContext(Dispatchers.IO) {
            try {
                val imagePaths = context.assets.list(imagesFolder)
                    ?: throw (Exception("No files in $imagesFolder"))

                val imageList = getImageList(imagePaths)
                val listSize = imageList.size
                Timber.i("Resolved $listSize images (png,jpeg,jpg)")

                if (listSize == 0) {
                    throw Exception("No images found in $imagesFolder")
                }

                var count = 0
                imageList.forEachIndexed { _, imagePath ->
                    coroutineContext.ensureActive()

                    val path = "$imagesFolder/${imagePath}"
                    context.assets.open(path).use {
                        val bmp = BitmapFactory.decodeStream(it)
                        onReadAssetImageCallback.onReadAssetImage(
                            TimestampBitmap(
                                bmp
                            )
                        )
                    }
                    count++
                    onReadAssetImageCallback.onProgress(
                        FrameInformation(
                            listSize, count
                        )
                    )
                }
            } catch (e: IOException) {
                Timber.e(e)
                throw e
            }
            return@withContext UseCase.Status.SUCCESS
        }
    }
}