package ru.igla.tfprofiler.models_list

import android.app.Application
import android.text.SpannableStringBuilder
import androidx.lifecycle.AndroidViewModel


class MainViewPagerViewModel(application: Application) : AndroidViewModel(application) {

    private val getDeviceInfoUseCase by lazy {
        GetDeviceInfoUseCase(application)
    }

    fun getDeviceTextViewHtml(gpuInfo: GPUInfo?): SpannableStringBuilder {
        val resource = getDeviceInfoUseCase.executeUseCase(
            GetDeviceInfoUseCase.RequestValues(gpuInfo)
        )
        checkNotNull(resource.data)
        return resource.data.text
    }
}