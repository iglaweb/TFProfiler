package ru.igla.tfprofiler.models_list

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.igla.tfprofiler.core.RequestMode
import ru.igla.tfprofiler.models_list.domain.ModelEntity

@Parcelize
class ExtraMediaRequest(
    val requestMode: RequestMode,
    val mediaPath: String,
    val modelEntity: ModelEntity
) : Parcelable