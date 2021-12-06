package ru.igla.tfprofiler.core.tflite

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import org.tensorflow.lite.support.metadata.MetadataExtractor
import ru.igla.tfprofiler.models_list.domain.ModelEntity
import timber.log.Timber
import java.io.*
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import kotlin.math.exp

object TensorFlowUtils {

    @Throws(IOException::class)
    fun loadLabelList(assetManager: AssetManager, labelPath: String): List<String> {
        assetManager.open(labelPath).use { ins -> return loadLabelList(ins) }
    }

    @Throws(IOException::class)
    fun loadLabelList(inputStream: InputStream): List<String> {
        val labelList: MutableList<String> = ArrayList()
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            var line: String
            while (reader.readLine().also { line = it } != null) {
                Timber.w(line)
                labelList.add(line)
            }
        }
        return labelList
    }

    /**
     * Memory-map the model file in Assets.
     * Preload and memory map the model file, returning a MappedByteBuffer containing the model.
     */
    @Throws(IOException::class)
    fun loadModelFileFromAssets(context: Context, modelFilename: String?): MappedByteBuffer {
        val assetManager = context.assets
        assetManager.openFd(modelFilename!!).use { fileDescriptor ->
            FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
                val fileChannel = inputStream.channel
                val startOffset = fileDescriptor.startOffset
                val declaredLength = fileDescriptor.declaredLength
                return fileChannel.map(
                    FileChannel.MapMode.READ_ONLY,
                    startOffset,
                    declaredLength
                )
            }
        }
    }

    @Throws(IOException::class)
    fun loadModelFileFromExternal(modelFilename: String): MappedByteBuffer {
        val file = File(modelFilename)
        FileInputStream(file).use { inputStream ->
            val fileChannel = inputStream.channel
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
        }
    }

    fun isAssetFileExists(context: Context, filename: String): Boolean {
        val assetManager = context.resources.assets
        try {
            assetManager.open(filename).use {
                return true
            }
        } catch (e: Exception) {
            return false
        }
    }

    fun getMetaData(context: Context, modelFilename: String): MetadataExtractor? {
        try {
            val m = loadModelFileFromAssets(context, modelFilename)
            val metadataExtractor = MetadataExtractor(m)
            return if (!metadataExtractor.hasMetadata()) {
                null
            } else metadataExtractor
        } catch (e: IOException) {
            Timber.e(e)
        }
        return null
    }

    fun getModelFileSize(context: Context, modelEntity: ModelEntity): Long {
        if (modelEntity.modelType.isCustomModel()) {
            return File(modelEntity.modelFile).length()
        }
        try {
            val modelFilename = modelEntity.modelFile
            context.assets.openFd(modelFilename).use { fd -> return fd.length }
        } catch (e: IOException) {
            Timber.e(e)
            return AssetFileDescriptor.UNKNOWN_LENGTH
        }
    }

    fun sigmoid(x: Float): Float {
        return (1.0 / (1.0 + exp(-x.toDouble()))).toFloat()
    }
}