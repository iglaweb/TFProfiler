package ru.igla.tfprofiler.models_list

import android.app.Application
import android.content.Context
import android.text.SpannableStringBuilder
import androidx.lifecycle.AndroidViewModel
import ru.igla.tfprofiler.BuildConfig
import ru.igla.tfprofiler.models_list.domain.GPUInfo
import ru.igla.tfprofiler.utils.IntentUtils
import ru.igla.tfprofiler.utils.logI


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

    fun openAppGooglePlay(context: Context) {
        val flag = IntentUtils.openGooglePlayPage(
            BuildConfig.APPLICATION_ID,
            context.applicationContext
        )
        if (!flag) {
            logI { "Failed to proceed Google Play" }
        }
    }
}