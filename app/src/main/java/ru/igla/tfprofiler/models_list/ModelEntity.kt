package ru.igla.tfprofiler.models_list

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.tensorflow.lite.support.metadata.MetadataExtractor
import ru.igla.tfprofiler.core.ColorSpace
import ru.igla.tfprofiler.core.ModelType

@Parcelize
data class ModelEntity(
    val id: Long,
    val tableId: Long,
    val modelType: ModelType,
    val name: String,
    val details: String,

    val inputWidth: Int,
    val inputHeight: Int,
    val quantized: Boolean,
    val colorSpace: ColorSpace,

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