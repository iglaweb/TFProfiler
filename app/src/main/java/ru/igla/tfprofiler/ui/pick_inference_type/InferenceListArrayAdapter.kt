package ru.igla.tfprofiler.ui.pick_inference_type

import android.content.Context
import android.widget.ArrayAdapter

class InferenceListArrayAdapter(
    context: Context,
    resource: Int,
    textViewResourceId: Int,
    packages: List<String>
) : ArrayAdapter<String>(
    context, resource, textViewResourceId, packages
)