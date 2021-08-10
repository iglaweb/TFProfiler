package ru.igla.tfprofiler.db

import androidx.room.TypeConverter
import ru.igla.tfprofiler.core.InputShapeType

class InputShapeTypeConverter {
    @TypeConverter
    fun toInputShapeType(value: Int): InputShapeType = InputShapeType.values()[value]

    @TypeConverter
    fun fromInputShapeType(colorSpace: InputShapeType): Int = colorSpace.ordinal
}