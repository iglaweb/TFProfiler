package ru.igla.tfprofiler.models_list

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.igla.tfprofiler.core.ModelType

@Parcelize
data class ModelEntity(
    val id: Long,
    val tableId: Long,
    val modelType: ModelType,
    val name: String,
    val details: String,
    val inputSize: Int,
    val quantized: Boolean,
    val modelFile: String,
    val labelFile: String,
    val source: String? = null
) : Parcelable