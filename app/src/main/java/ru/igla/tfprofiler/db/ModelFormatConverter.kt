package ru.igla.tfprofiler.db

import androidx.room.TypeConverter
import ru.igla.tfprofiler.core.ModelFormat

class ModelFormatConverter {
    @TypeConverter
    fun toModelFormat(value: Int): ModelFormat = ModelFormat.values()[value]

    @TypeConverter
    fun fromModelFormat(colorSpace: ModelFormat): Int = colorSpace.ordinal
}