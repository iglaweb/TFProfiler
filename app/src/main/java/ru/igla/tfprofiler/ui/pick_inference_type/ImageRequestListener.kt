package ru.igla.tfprofiler.ui.pick_inference_type

import ru.igla.tfprofiler.core.RequestMode

interface ImageRequestListener {
    fun onSelectedOption(selectedOption: RequestMode)
}