package ru.igla.tfprofiler.models_list

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.igla.tfprofiler.R
import ru.igla.tfprofiler.core.ModelType
import ru.igla.tfprofiler.core.tflite.TensorFlowUtils
import ru.igla.tfprofiler.utils.StringUtils

class ModelsListRecyclerViewAdapter(
    private val context: Context,
    private val listener: ClickModelItemListener
) :
    RecyclerView.Adapter<ModelItemHolder>() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val modelEntityList: MutableList<ModelEntity> = mutableListOf()

    fun notifyAdapterItems(data: List<ModelEntity>) {
        setData(data)
        notifyDataSetChanged()
    }

    private fun setData(data: List<ModelEntity>) {
        modelEntityList.clear()
        modelEntityList.addAll(data)
    }

    fun clear() {
        modelEntityList.clear()
    }

    private fun remove(position: Int) {
        modelEntityList.removeAt(position)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelItemHolder {
        val layoutView = layoutInflater.inflate(R.layout.list_neural_model_row, parent, false)
        return ModelItemHolder(layoutView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ModelItemHolder, position: Int) {
        val modelEntity = modelEntityList[position]

        val fileModelSize =
            TensorFlowUtils.getModelFileSize(context.applicationContext, modelEntity)
        val fileSizeStr = StringUtils.getReadableFileSize(fileModelSize, true)
        holder.textViewTitle.text = modelEntity.name
        holder.textViewQuantized.text = if (modelEntity.quantized) "Quantized" else "Floating"
        val fileSize = "File size: $fileSizeStr"
        holder.textViewModelSize.text =
            "Image: " + modelEntity.inputWidth + "x" + modelEntity.inputHeight + ", " + fileSize
        holder.itemView.setOnClickListener {
            listener.onClickItem(modelEntity)
        }

        if (modelEntity.modelType == ModelType.CUSTOM) {
            holder.ivDelete.visibility = View.VISIBLE
            holder.ivDelete.setOnClickListener {
                remove(position)
                listener.onDeleteItem(modelEntity)
            }
        } else {
            holder.ivDelete.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return modelEntityList.size
    }

    interface ClickModelItemListener {
        fun onClickItem(item: ModelEntity)
        fun onDeleteItem(item: ModelEntity)
    }
}