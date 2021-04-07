package ru.igla.tfprofiler.report_details

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.igla.tfprofiler.R

class ModelReportItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val statsContainer: ViewGroup = itemView.findViewById(R.id.statsContainer)
    val errorContainer: TextView = itemView.findViewById(R.id.tvError)

    val textViewDevice: TextView = itemView.findViewById(R.id.device)
    val textViewThreads: TextView = itemView.findViewById(R.id.threads)
    val textViewXnnpack: TextView = itemView.findViewById(R.id.xnnpack_enabled)
    val textViewBatchCount: TextView = itemView.findViewById(R.id.batch_image_count);

    val textViewFps: TextView = itemView.findViewById(R.id.fps_info)
    val textViewInitTime: TextView = itemView.findViewById(R.id.tvInitTime)
    val textViewMeanTime: TextView = itemView.findViewById(R.id.tvMeanInterferenceTime)
    val textViewMemoryUsageMin: TextView = itemView.findViewById(R.id.tvMemoryUsageMin)
    val textViewMemoryUsageMax: TextView = itemView.findViewById(R.id.tvMemoryUsageMax)
    val textViewTimeMin: TextView = itemView.findViewById(R.id.inference_info_time_min)
    val textViewTimeMax: TextView = itemView.findViewById(R.id.inference_info_time_max)
    val textViewPercentile: TextView = itemView.findViewById(R.id.inferenceTimePercentile)
    val textViewInterferenceRuns: TextView = itemView.findViewById(R.id.tvInterferenceRuns)
    val textViewWarmupRuns: TextView = itemView.findViewById(R.id.tvWarmupRuns)
}