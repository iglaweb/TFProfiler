package ru.igla.tfprofiler.db

import androidx.room.TypeConverter
import ru.igla.tfprofiler.core.ModelType

class ModelTypeConverter {
    @TypeConverter
    fun toModelType(value: Int): ModelType {
        return ModelType.values()[value]
    }

    @TypeConverter
    fun fromModelType(modelType: ModelType): Int {
        return modelType.ordinal
    }
}