package ru.igla.tfprofiler.report_details

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.igla.tfprofiler.R
import ru.igla.tfprofiler.core.analytics.DataAnalytics
import ru.igla.tfprofiler.reports_list.ListReportEntity
import ru.igla.tfprofiler.reports_list.ReportDelegateItem
import ru.igla.tfprofiler.utils.StringUtils
import java.util.*

class ModelsReportDetailsRecyclerViewAdapter(val context: Context) :
    RecyclerView.Adapter<ModelReportItemHolder>() {

    companion object {
        const val NOT_DEFINED = "N/A"
    }

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val modelHolderList: MutableList<ReportDelegateItem> = mutableListOf()

    fun notifyAdapterItems(data: ListReportEntity) {
        setData(data)
        notifyDataSetChanged()
    }

    private fun setData(data: ListReportEntity) {
        modelHolderList.clear()
        modelHolderList.addAll(data.reportDelegateItems)
    }

    fun clear() {
        modelHolderList.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelReportItemHolder {
        val layoutView = layoutInflater.inflate(R.layout.interference_report_row, parent, false)
        return ModelReportItemHolder(layoutView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ModelReportItemHolder, position: Int) {
        val modelHolder = modelHolderList[position]

        holder.textViewDevice.text = modelHolder.device.name
        holder.textViewThreads.text = if (modelHolder.threads == 1) {
            "" + modelHolder.threads + " Thread"
        } else {
            "" + modelHolder.threads + " Threads"
        }

        if (modelHolder.useXnnpack) {
            holder.textViewXnnpack.text = "XNNPACK"
        } else {
            holder.textViewXnnpack.visibility = View.GONE
        }

        if (modelHolder.exception.isNullOrEmpty()) {
            holder.errorContainer.visibility = View.GONE
            holder.statsContainer.visibility = View.VISIBLE

            val hasRuns = modelHolder.interferenceRuns > 0
            if (hasRuns) {
                holder.textViewFps.text = modelHolder.fps.toString()
            } else {
                holder.textViewFps.text = NOT_DEFINED
            }
            holder.textViewInitTime.text = modelHolder.modelInitTime.toString() + " ms"

            if (!hasRuns || java.lang.Double.isNaN(modelHolder.meanTime) || java.lang.Double.isNaN(
                    modelHolder.stdTime
                )
            ) {
                holder.textViewMeanTime.text = NOT_DEFINED
            } else {
                val statsStr = String.format(
                    Locale.getDefault(),
                    "%.2f ± %.2f ms",
                    modelHolder.meanTime,
                    modelHolder.stdTime
                )
                holder.textViewMeanTime.text = statsStr
            }

            if (!hasRuns || java.lang.Double.isNaN(modelHolder.percentile99Time)) {
                holder.textViewPercentile.text = NOT_DEFINED
            } else {
                holder.textViewPercentile.text =
                    modelHolder.percentile99Time.toInt().toString() + " ms"
            }

            holder.textViewInterferenceRuns.text = modelHolder.interferenceRuns.toString()
            holder.textViewWarmupRuns.text = modelHolder.warmupRuns.toString()

            if (modelHolder.memoryUsageMin != DataAnalytics.INVALID_MEMORY) {
                val memoryStr = StringUtils.getReadableFileSize(modelHolder.memoryUsageMin, true)
                holder.textViewMemoryUsageMin.text = memoryStr
            } else {
                holder.textViewMemoryUsageMin.text = NOT_DEFINED
            }
            if (modelHolder.memoryUsageMax != DataAnalytics.INVALID_MEMORY) {
                val memoryStr = StringUtils.getReadableFileSize(modelHolder.memoryUsageMax, true)
                holder.textViewMemoryUsageMax.text = memoryStr
            } else {
                holder.textViewMemoryUsageMax.text = NOT_DEFINED
            }

            if (modelHolder.minTime == DataAnalytics.INVALID_TIME) {
                holder.textViewTimeMin.text = NOT_DEFINED
            } else {
                holder.textViewTimeMin.text = modelHolder.minTime.toString() + " ms"
            }
            if (modelHolder.maxTime == DataAnalytics.INVALID_TIME) {
                holder.textViewTimeMax.text = NOT_DEFINED
            } else {
                holder.textViewTimeMax.text = modelHolder.maxTime.toString() + " ms"
            }
        } else {
            holder.errorContainer.visibility = View.VISIBLE
            holder.statsContainer.visibility = View.GONE

            holder.errorContainer.text = modelHolder.exception
        }
    }

    override fun getItemCount(): Int {
        return modelHolderList.size
    }
}