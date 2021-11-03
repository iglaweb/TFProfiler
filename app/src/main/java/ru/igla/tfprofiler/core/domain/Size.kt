package ru.igla.tfprofiler.core.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
open class Size(open val width: Int, open val height: Int) : Parcelable {
    override fun toString(): String {
        return "" + width + "x" + height
    }
}