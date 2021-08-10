package ru.igla.tfprofiler.db

import androidx.room.TypeConverter
import ru.igla.tfprofiler.core.ModelOptimizedType

class ModelFormatConverter {
    @TypeConverter
    fun toModelFormat(value: Int): ModelOptimizedType = ModelOptimizedType.values()[value]

    @TypeConverter
    fun fromModelFormat(colorSpace: ModelOptimizedType): Int = colorSpace.ordinal
}