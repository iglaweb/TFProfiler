package ru.igla.tfprofiler.db

import androidx.room.TypeConverter
import ru.igla.tfprofiler.core.domain.ColorSpace

class ColorSpaceTypeConverter {
    @TypeConverter
    fun toModelType(value: Int): ColorSpace = ColorSpace.values()[value]

    @TypeConverter
    fun fromModelType(colorSpace: ColorSpace): Int = colorSpace.ordinal
}