package ru.igla.tfprofiler.models_list

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.TypeParceler
import ru.igla.tfprofiler.core.domain.Device
import ru.igla.tfprofiler.core.domain.IntRangeParceler

@Parcelize
@TypeParceler<IntRange, IntRangeParceler>()
class DelegateRunRequest(
    var threadsRange: IntRange,
    val deviceList: List<Device>,
    val xnnpack: Boolean,
    val batchImageCount: Int,
    val cpuStress: Boolean
) : Parcelable