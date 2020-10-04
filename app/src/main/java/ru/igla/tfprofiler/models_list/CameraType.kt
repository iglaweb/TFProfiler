package ru.igla.tfprofiler.models_list

import java.util.*
import java.util.concurrent.ConcurrentHashMap

enum class CameraType(val value: String) {
    FRONT("front"), REAR("rear");

    companion object {
        val map = ConcurrentHashMap<String, CameraType>()

        init {
            for (instance in values()) {
                map[instance.value] = instance
            }
        }

        private val ENUM_MAP: Map<String, CameraType> = Collections.unmodifiableMap(map)

        fun get(name: String?): CameraType? {
            return ENUM_MAP[name]
        }
    }
}