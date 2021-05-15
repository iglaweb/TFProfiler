package ru.igla.tfprofiler.core.tflite

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import ru.igla.tfprofiler.models_list.ModelEntity
import ru.igla.tfprofiler.tflite_runners.base.ModelOptions
import ru.igla.tfprofiler.utils.ExceptionHandler
import java.util.concurrent.Executors

class OpenCVInterpeterThreadExecutor(val context: Context, private val modelPath: String) {

    /***
     * Use separete persistent thread as we must run GPU delegate on same one thread otherwise we can get exception (google pixel)
     */
    private val dispatcherBg by lazy {
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    }

    private val exceptionHandler by lazy { ExceptionHandler { _, _ -> } } // for cancellation exception

    var openCVInterpreterWrapper: OpenCVInterpreterWrapper? = null

    @Throws(Exception::class)
    fun init(
        modelEntity: ModelEntity,
        modelOptions: ModelOptions
    ) {
        this.close()
        runBlocking(exceptionHandler + dispatcherBg) {
            openCVInterpreterWrapper = OpenCVInterpreterWrapper.createInterpreter(
                modelPath,
                modelEntity.modelConfig,
                modelOptions
            )
        }
    }

    fun runBitmapProcessing(bitmap: Bitmap) {
        runBlocking(exceptionHandler + dispatcherBg) {
            openCVInterpreterWrapper?.recognize(bitmap)
        }
    }

    fun close() {
        runBlocking(exceptionHandler + dispatcherBg) {
            openCVInterpreterWrapper?.close()
            openCVInterpreterWrapper = null
        }
    }
}