package ru.igla.tfprofiler.reports_list

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.igla.tfprofiler.R

class ListReportHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textViewTitle: TextView = itemView.findViewById(R.id.model_title)
    val textViewCreatedAt: TextView = itemView.findViewById(R.id.created_at)
    val textViewDescription: TextView = itemView.findViewById(R.id.description)
    val textViewDelete: ImageView = itemView.findViewById(R.id.iv_delete)
}