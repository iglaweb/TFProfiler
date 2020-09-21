package ru.igla.tfprofiler.models_list

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.TypeParceler
import ru.igla.tfprofiler.core.Device
import ru.igla.tfprofiler.core.IntRangeParceler

@Parcelize
@TypeParceler<IntRange, IntRangeParceler>()
class DelegateRunRequest(
    var threadsRange: IntRange,
    val deviceList: List<Device>,
    val xnnpack: Boolean
) : Parcelable