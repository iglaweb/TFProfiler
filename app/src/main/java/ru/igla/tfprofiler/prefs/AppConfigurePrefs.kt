package ru.igla.tfprofiler.prefs

import ru.igla.tfprofiler.models_list.CameraType


class AppConfigurePrefs(private val pref: PrefsBase) {

    companion object {
        const val PREFS_NAME = "tfprofiler"
        private const val PREF_IMAGE_MAXSIZE = "image_max_size"
        private const val PREF_WARMUP_RUNS = "warmup_runs"
        private const val PREF_IS_CAMERA_SELECTED_LAST = "is_camera_selected_last"

        private const val PREF_CPU_ENABLED = "cpu_enabled"
        private const val PREF_GPU_ENABLED = "gpu_enabled"
        private const val PREF_HEXAGON_ENABLED = "hexagon_enabled"
        private const val PREF_NNAPI_ENABLED = "nnapi_enabled"

        private const val PREF_XNNPACK_ENABLED = "xnnpack_enabled"

        private const val PREF_THREAD_RANGE_MIN = "thread_range_min"
        private const val PREF_THREAD_RANGE_MAX = "thread_range_max"
    }

    var maxImageSize: Int
        get() = pref.getIntValue(PREF_IMAGE_MAXSIZE, 350)
        set(value) = pref.editIntValue(PREF_IMAGE_MAXSIZE, value)

    var warmupRuns: Int
        get() = pref.getIntValue(PREF_WARMUP_RUNS, 10)
        set(value) = pref.editIntValue(PREF_WARMUP_RUNS, value)


    var threadRangeMin: Int
        get() = pref.getIntValue(PREF_THREAD_RANGE_MIN, 1)
        set(value) = pref.editIntValue(PREF_THREAD_RANGE_MIN, value)

    var threadRangeMax: Int
        get() = pref.getIntValue(PREF_THREAD_RANGE_MAX, 4)
        set(value) = pref.editIntValue(PREF_THREAD_RANGE_MAX, value)


    var cpuDelegateEnabled: Boolean
        get() = pref.getBoolValue(PREF_CPU_ENABLED, true)
        set(value) = pref.editBoolValue(PREF_CPU_ENABLED, value)

    var gpuDelegateEnabled: Boolean
        get() = pref.getBoolValue(PREF_GPU_ENABLED, true)
        set(value) = pref.editBoolValue(PREF_GPU_ENABLED, value)

    var hexagonDelegateEnabled: Boolean
        get() = pref.getBoolValue(PREF_HEXAGON_ENABLED, true)
        set(value) = pref.editBoolValue(PREF_HEXAGON_ENABLED, value)

    var nnapiDelegateEnabled: Boolean
        get() = pref.getBoolValue(PREF_NNAPI_ENABLED, true)
        set(value) = pref.editBoolValue(PREF_NNAPI_ENABLED, value)

    var xnnpackEnabled: Boolean
        get() = pref.getBoolValue(PREF_XNNPACK_ENABLED, false)
        set(value) = pref.editBoolValue(PREF_XNNPACK_ENABLED, value)

    var cameraType: CameraType
        get() {
            val algoStr = pref.getStringValue(PREF_IS_CAMERA_SELECTED_LAST, CameraType.FRONT.name)
            return CameraType.get(algoStr) ?: CameraType.FRONT
        }
        set(value) {
            val algoStr = value.value
            pref.editStringValue(PREF_IS_CAMERA_SELECTED_LAST, algoStr)
        }
}
