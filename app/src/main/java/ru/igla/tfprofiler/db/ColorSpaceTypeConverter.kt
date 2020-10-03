package ru.igla.tfprofiler.db

import androidx.room.TypeConverter
import ru.igla.tfprofiler.core.ColorSpace

class ColorSpaceTypeConverter {
    @TypeConverter
    fun toModelType(value: Int): ColorSpace {
        return ColorSpace.values()[value]
    }

    @TypeConverter
    fun fromModelType(colorSpace: ColorSpace): Int {
        return colorSpace.ordinal
    }
}