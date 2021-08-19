package ru.igla.tfprofiler.utils

import kotlinx.coroutines.CoroutineExceptionHandler
import timber.log.Timber
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

abstract class ExceptionHandler : AbstractCoroutineContextElement(CoroutineExceptionHandler),
    CoroutineExceptionHandler {

    final override val key: CoroutineContext.Key<*> = CoroutineExceptionHandler.Key
}

@Suppress("FunctionName")
inline fun ExceptionHandler(
    crossinline f: (context: CoroutineContext, exception: Throwable) -> Unit
): CoroutineExceptionHandler = object : ExceptionHandler() {
    override fun handleException(context: CoroutineContext, exception: Throwable) =
        f(context, exception).also {
            Timber.e(exception)
        }
}