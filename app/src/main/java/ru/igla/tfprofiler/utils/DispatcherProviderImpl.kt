package ru.igla.tfprofiler.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class DispatcherProviderImpl : DispatcherProvider {
    override val main: CoroutineDispatcher
        get() = Dispatchers.Main
    override val background: CoroutineDispatcher
        get() = Dispatchers.IO
}

interface DispatcherProvider {
    val main: CoroutineDispatcher
    val background: CoroutineDispatcher
}