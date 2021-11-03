package ru.igla.tfprofiler.models_list

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.igla.tfprofiler.models_list.domain.ModelEntity

@Parcelize
class ExtraTextRequest(
    val modelEntity: ModelEntity
) : Parcelable