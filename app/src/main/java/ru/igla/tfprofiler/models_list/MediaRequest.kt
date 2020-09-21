package ru.igla.tfprofiler.models_list

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.igla.tfprofiler.core.RequestMode

@Parcelize
class MediaRequest(
    val requestMode: RequestMode,
    val mediaPath: String,
    val modelEntity: ModelEntity
) : Parcelable