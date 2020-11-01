package ru.igla.tfprofiler.models_list

import android.app.Application
import android.os.Build
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.annotation.Nullable
import ru.igla.tfprofiler.core.Timber
import ru.igla.tfprofiler.core.UseCase
import ru.igla.tfprofiler.core.tflite.TensorFlowUtils
import ru.igla.tfprofiler.utils.SystemUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class GetDeviceInfoUseCase(val application: Application) :
    UseCase<GetDeviceInfoUseCase.RequestValues,
            GetDeviceInfoUseCase.ResponseValue>() {

    private var cpuName: String? = null
    private var cpuSoC: String? = null

    private fun getTextViewHTML(html: String): SpannableStringBuilder {
        val sequence = fromHtml(html) ?: return SpannableStringBuilder(html)
        return SpannableStringBuilder(sequence)
    }

    @Suppress("DEPRECATION")
    @Nullable
    private fun fromHtml(source: String): Spanned? {
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(source)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return null
    }

    private fun getDeviceTextViewHtml(gpuInfo: GPUInfo?): SpannableStringBuilder {
        val generatedText = generateDeviceText(gpuInfo)
        return getTextViewHTML(generatedText)
    }

    private fun generateDeviceText(gpuInfo: GPUInfo?): String {
        val cpuDetails = getCPUDetails()
        val cpuName = this.cpuName ?: getProcValue(cpuDetails, "Processor").apply {
            cpuName = this
        }
        val hardwareName = this.cpuSoC ?: getProcValue(cpuDetails, "Hardware").apply {
            cpuSoC = this
        }

        val boardPlatform = TensorFlowUtils.getBoardPlatform()
        Timber.i("Board platform: $boardPlatform")
        val productDevice = TensorFlowUtils.getProductDevice()
        Timber.i("Product device: $productDevice")

        val memoryUsage = SystemUtils.getMemorySizeInBytes(application)
        return StringBuilder().apply {
            append("<b>Board platform: </b>")
            append(boardPlatform)
            append("<br><br><b>Product device: </b>")
            append(productDevice)

            if (hardwareName.isNotEmpty()) {
                append("<br><br><b>Hardware: </b>")
                append(hardwareName)
            }

            if (cpuName.isNotEmpty()) {
                append("<br><br><b>CPU: </b>")
                append(cpuName)
            }

            append("<br><br><b>CPU cores: </b>")
            append(Runtime.getRuntime().availableProcessors())

            gpuInfo?.let { gpu ->
                append("<br><br><b>GPU: </b>")
                append(gpu.glEsVersionStr)
                append(" GLES, ")
                append(gpu.vendorName)
                append(", ")
                append(gpu.modelName)
            }

            if (memoryUsage != -1L) {
                val memoryUsageStr =
                    String.format(
                        "%.1f GB",
                        (memoryUsage.toDouble() / (1024 * 1024 * 1024))
                    )
                append("<br><br><b>Total RAM: </b>")
                append(memoryUsageStr)
            }

            append("<br>")
        }.toString()
    }

    private fun getCPUDetails(): List<String> {
        val processBuilder: ProcessBuilder
        val cpuDetails: MutableList<String> = mutableListOf()
        val cmd = arrayOf("/system/bin/cat", "/proc/cpuinfo")
        val process: Process
        try {
            processBuilder = ProcessBuilder(*cmd)
            process = processBuilder.start()
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?

            bufferedReader.use {
                while (bufferedReader.readLine().apply { line = this } != null) {
                    cpuDetails.add(line!!)
                }
            }

            process.destroy()
        } catch (e: IOException) {
            Timber.e(e)
        }
        return cpuDetails
    }

    private fun getProcValue(unparsedCpuInfo: List<String>, header: String): String {
        var cpuName = ""
        for (line in unparsedCpuInfo) {
            if (line.contains("$header\t:")) {
                cpuName = line.replace("$header\t: ", "")
                break
            }
        }
        return cpuName
    }

    override fun executeUseCase(requestValues: RequestValues): Resource<ResponseValue> {
        return try {
            val text = getDeviceTextViewHtml(requestValues.gpuInfo)
            Resource.success(ResponseValue(text))
        } catch (e: Exception) {
            Timber.e(e)
            Resource.error(e.message, e)
        }
    }

    class RequestValues(val gpuInfo: GPUInfo?) : UseCase.RequestValues
    class ResponseValue(val text: SpannableStringBuilder) : UseCase.ResponseValue
}