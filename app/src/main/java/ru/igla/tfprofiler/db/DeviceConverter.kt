package ru.igla.tfprofiler.db

import androidx.room.TypeConverter
import ru.igla.tfprofiler.core.domain.Device

class DeviceConverter {
    @TypeConverter
    fun toDevice(value: Int): Device {
        return Device.values()[value]
    }

    @TypeConverter
    fun fromDevice(device: Device): Int {
        return device.ordinal
    }
}