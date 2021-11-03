package ru.igla.tfprofiler.core.cpu_load

class FibonacciCpuSimulator : CpuSimulator {
    private var fib0: Long = 0L
    private var fib1: Long = 1L

    override fun simulateCpu() {
        val fib2 = fib0 + fib1
        fib0 = fib1
        fib1 = fib2
    }
}