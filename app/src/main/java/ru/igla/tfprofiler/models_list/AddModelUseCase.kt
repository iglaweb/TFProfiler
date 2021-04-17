package ru.igla.tfprofiler.models_list

import ru.igla.tfprofiler.core.UseCase

object AddModelUseCase  {
    class RequestValues(val modelPath: String) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}
