package ru.igla.tfprofiler.core

import android.os.Parcel
import kotlinx.android.parcel.Parceler

object IntRangeParceler : Parceler<IntRange> {
    override fun create(parcel: Parcel) = IntRange(parcel.readInt(), parcel.readInt())

    override fun IntRange.write(parcel: Parcel, flags: Int) {
        parcel.apply {
            writeInt(start)
            writeInt(endInclusive)
        }
    }
}