package ru.igla.tfprofiler.core.cpu_load

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CPUIntenseLoadWorker {

    var cancelled = false

    lateinit var poolExecutor: ExecutorService

    private val hashSet = HashSet<CpuStressRunner>()

    fun startWork(threads: Int = getNumberOfCores()) {
        stop()
        poolExecutor = Executors.newFixedThreadPool(threads)
        for (i in 0 until threads) {
            val regexThread = CpuStressRunner(FibonacciCpuSimulator())
            hashSet.add(regexThread)
            poolExecutor.submit(regexThread)
        }
    }

    fun stop() {
        if (hashSet.isEmpty()) return
        hashSet.forEach {
            it.cancelled = true
        }
        hashSet.clear()
        poolExecutor.shutdown()
    }

    private fun getNumberOfCores(): Int {
        return Runtime.getRuntime().availableProcessors()
    }
}