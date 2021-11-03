package ru.igla.tfprofiler.models_list.domain

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import org.tensorflow.lite.support.metadata.MetadataExtractor
import ru.igla.tfprofiler.core.*
import ru.igla.tfprofiler.core.domain.ColorSpace
import ru.igla.tfprofiler.core.domain.InputShapeType
import ru.igla.tfprofiler.core.domain.ModelOptimizedType
import ru.igla.tfprofiler.core.domain.Size

@Parcelize
data class ModelConfig(
    val tableId: Long,
    val inputSize: @RawValue Size,
    val modelFormat: ModelOptimizedType,
    val colorSpace: ColorSpace,
    val inputShapeType: InputShapeType
) : Parcelable {
    fun quantizedStr() = modelFormat.strVal
}

@Parcelize
data class ModelEntity(
    val id: Long,
    val modelType: ModelType,
    val name: String,
    val details: String,

    val modelConfig: ModelConfig,

    val modelFile: String,
    val labelFile: String,
    val source: String? = null
) : Parcelable {
    @IgnoredOnParcel
    private var tensorInputCount = 0

    @IgnoredOnParcel
    private var tensorOutputCount = 0

    fun setMeta(meta: MetadataExtractor?) {
        meta?.apply {
            tensorInputCount = meta.inputTensorCount
            tensorOutputCount = meta.outputTensorCount
        }
    }
}