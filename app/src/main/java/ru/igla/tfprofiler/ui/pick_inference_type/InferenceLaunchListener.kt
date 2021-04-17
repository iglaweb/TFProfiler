package ru.igla.tfprofiler.ui.pick_inference_type

interface InferenceLaunchListener {
    fun onSelectedOption(selectedOption: InferenceTypeLauncher.InferenceType)
}