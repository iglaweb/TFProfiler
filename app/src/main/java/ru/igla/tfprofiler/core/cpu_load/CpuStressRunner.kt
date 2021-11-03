package ru.igla.tfprofiler.core.cpu_load

import ru.igla.tfprofiler.utils.DateUtils

class CpuStressRunner(private val cpuSimulator: CpuSimulator) : Runnable {
    @Volatile
    var cancelled = false

    override fun run() {
        val load = 90L
        while (!cancelled) {
            val timeMs = System.currentTimeMillis() + load
            while (!cancelled && DateUtils.getCurrentDateInMs() < timeMs) {
                cpuSimulator.simulateCpu()
            }
            Thread.sleep(100L - load)
        }
    }
}