package ru.igla.tfprofiler.models_list

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.tensorflow.lite.support.metadata.MetadataExtractor
import ru.igla.tfprofiler.core.ModelType

@Parcelize
data class ModelItem(
    val id: Long,
    val modelType: ModelType,
    val details: String,
    val inputSize: Int,
    val quantized: Boolean,
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