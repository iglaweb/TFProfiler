package ru.igla.tfprofiler.media_track

import android.app.Application
import ru.igla.tfprofiler.core.Device
import ru.igla.tfprofiler.core.UseCase
import ru.igla.tfprofiler.models_list.DelegateRunRequest
import ru.igla.tfprofiler.tflite_runners.base.ModelOptions
import ru.igla.tfprofiler.utils.forEachNoIterator
import java.util.*

class ResolveRunDelegatesExtrasUseCase(val application: Application) :
    UseCase<ResolveRunDelegatesExtrasUseCase.RequestValues,
            ResolveRunDelegatesExtrasUseCase.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Resource<ResponseValue> {
        val data = resolveRunDelegatesExtra(requestValues.delegateRunRequest)

        val responseValue = ResponseValue(data)
        return Resource.success(responseValue)
    }

    private fun resolveRunDelegatesExtra(delegateRunRequest: DelegateRunRequest?): Queue<ModelOptions> {
        return if (delegateRunRequest == null || delegateRunRequest.deviceList.isEmpty()) {
            ArrayDeque(
                listOf(
                    ModelOptions(
                        device = Device.CPU,
                        numThreads = 4,
                        useXnnpack = false,
                        numberOfInputImages = 1
                    )
                )
            )
        } else {
            val deque: ArrayDeque<ModelOptions> = ArrayDeque()
            delegateRunRequest.deviceList.forEachNoIterator { device ->
                if (device == Device.CPU) {
                    delegateRunRequest.threadsRange.forEach {
                        deque.add(
                            ModelOptions(
                                device = device,
                                numThreads = it,
                                useXnnpack = delegateRunRequest.xnnpack,
                                numberOfInputImages = delegateRunRequest.batchImageCount
                            )
                        )
                    }
                } else {
                    deque.add(
                        ModelOptions(
                            device = device,
                            useXnnpack = delegateRunRequest.xnnpack,
                            numberOfInputImages = delegateRunRequest.batchImageCount
                        )
                    )
                }
            }
            deque
        }
    }

    class RequestValues(val delegateRunRequest: DelegateRunRequest?) : UseCase.RequestValues
    class ResponseValue(val queue: Queue<ModelOptions>) : UseCase.ResponseValue
}