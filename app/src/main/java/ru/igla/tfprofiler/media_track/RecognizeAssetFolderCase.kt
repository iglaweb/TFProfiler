package ru.igla.tfprofiler.media_track

import android.content.Context
import android.graphics.BitmapFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import ru.igla.tfprofiler.core.Resource
import ru.igla.tfprofiler.utils.logI
import ru.igla.tfprofiler.video.FileUtils
import timber.log.Timber
import java.io.IOException
import kotlin.coroutines.coroutineContext

class RecognizeAssetFolderCase {

    private suspend fun getImageList(list: Array<String>): MutableList<String> {
        val imageList = mutableListOf<String>()
        list.forEach {
            if (!coroutineContext.isActive) {
                throw CancellationException()
            }
            val isImage = FileUtils.isImage(it)
            if (isImage) imageList.add(it)
        }
        return imageList
    }

    @Throws(IOException::class)
    suspend fun iterateAssetsFiles(
        context: Context,
        imagesFolder: String
    ): Flow<Resource<FrameMetaInfo>> {
        return flow {
            try {
                val imagePaths = context.assets.list(imagesFolder)
                    ?: throw (IOException("No files in $imagesFolder"))

                val imageList = getImageList(imagePaths)
                val listSize = imageList.size
                logI { "Resolved $listSize images (png,jpeg,jpg)" }

                if (listSize == 0) {
                    throw IOException("No images found in $imagesFolder")
                }

                var count = 0
                imageList.forEachIndexed { _, imagePath ->
                    coroutineContext.ensureActive()

                    count++
                    val path = "$imagesFolder/${imagePath}"
                    context.assets.open(path).use {
                        val bmp = BitmapFactory.decodeStream(it)
                        emit(Resource.loading(FrameMetaInfo(bmp, count, listSize)))
                    }
                }
                emit(Resource.success(FrameMetaInfo(null, count, listSize)))
            } catch (e: IOException) {
                Timber.e(e)
                throw e
            }
        }
    }
}