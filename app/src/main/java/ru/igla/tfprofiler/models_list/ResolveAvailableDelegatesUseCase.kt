package ru.igla.tfprofiler.models_list

import android.app.Application
import ru.igla.tfprofiler.core.Device
import ru.igla.tfprofiler.core.UseCase
import ru.igla.tfprofiler.prefs.AndroidPreferenceManager

class ResolveAvailableDelegatesUseCase(val application: Application) :
    UseCase<ResolveAvailableDelegatesUseCase.RequestValues,
            ResolveAvailableDelegatesUseCase.ResponseValue>() {

    private val preferenceManager by lazy {
        AndroidPreferenceManager(application).defaultPrefs
    }

    override fun executeUseCase(requestValues: RequestValues): Resource<ResponseValue> {
        val modelList = mutableListOf<Device>().apply {
            if (preferenceManager.cpuDelegateEnabled) {
                add(Device.CPU)
            }
            if (preferenceManager.gpuDelegateEnabled) {
                add(Device.GPU)
            }
            if (preferenceManager.nnapiDelegateEnabled) {
                add(Device.NNAPI)
            }
            if (preferenceManager.hexagonDelegateEnabled) {
                add(Device.HEXAGON)
            }
        }

        val delegateRunRequest = DelegateRunRequest(
            IntRange(
                preferenceManager.threadRangeMin,
                preferenceManager.threadRangeMax
            ),
            modelList,
            preferenceManager.xnnpackEnabled,
            preferenceManager.batchImageCount
        )

        val responseValue = ResponseValue(delegateRunRequest)
        return Resource.success(responseValue)
    }

    class RequestValues : UseCase.RequestValues
    class ResponseValue(val data: DelegateRunRequest) : UseCase.ResponseValue
}