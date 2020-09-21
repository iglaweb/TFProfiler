package ru.igla.tfprofiler.models_list

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.igla.tfprofiler.R

class ModelItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textViewTitle: TextView = itemView.findViewById(R.id.model_title)
    val textViewModelSize: TextView = itemView.findViewById(R.id.model_size)
    val textViewQuantized: TextView = itemView.findViewById(R.id.model_quantized)
    val ivDelete: ImageView = itemView.findViewById(R.id.iv_delete)
}