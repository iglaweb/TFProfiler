package ru.igla.tfprofiler.core.tflite

import android.content.Context
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import ru.igla.tfprofiler.models_list.ModelEntity
import ru.igla.tfprofiler.tflite_runners.base.ModelOptions
import ru.igla.tfprofiler.utils.ExceptionHandler
import java.util.concurrent.Executors

class TFInterpeterThreadExecutor(val context: Context, private val modelPath: String) {

    /***
     * Use separete persistent thread as we must run GPU delegate on same one thread otherwise we can get exception (google pixel)
     */
    private val dispatcherTfliteBg by lazy {
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    }

    private val exceptionHandler by lazy { ExceptionHandler { _, _ -> } } // for cancellation exception

    var tfLite: TFInterpreterWrapper? = null

    @Throws(Exception::class)
    fun init(
        modelEntity: ModelEntity,
        modelOptions: ModelOptions
    ) {
        this.close()

        runBlocking(exceptionHandler + dispatcherTfliteBg) {
            tfLite = TFInterpreterWrapper.createTfliteInterpreter(
                context,
                modelPath,
                modelEntity.modelConfig,
                modelOptions
            )
        }
    }

    fun runForMultipleInputsOutputs(inputArray: Array<Any>, outputMap: MutableMap<Int, Any>) {
        runBlocking(exceptionHandler + dispatcherTfliteBg) {
            tfLite?.interpreter?.runForMultipleInputsOutputs(inputArray, outputMap)
        }
    }

    fun close() {
        runBlocking(exceptionHandler + dispatcherTfliteBg) {
            tfLite?.close()
            tfLite = null
        }
    }
}