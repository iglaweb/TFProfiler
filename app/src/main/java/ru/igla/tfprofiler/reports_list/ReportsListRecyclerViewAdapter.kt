package ru.igla.tfprofiler.reports_list

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.igla.tfprofiler.R
import ru.igla.tfprofiler.core.domain.Device
import ru.igla.tfprofiler.utils.DateUtils
import ru.igla.tfprofiler.utils.forEachNoIterator

class ReportsListRecyclerViewAdapter(
    context: Context,
    private val listener: ClickModelItemListener
) :
    RecyclerView.Adapter<ListReportHolder>() {

    private val devices: MutableSet<Device> = hashSetOf()

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val modelHolderList: MutableList<ListReportEntity> = mutableListOf()

    fun notifyAdapterItems(data: List<ListReportEntity>) {
        setData(data)
        notifyDataSetChanged()
    }

    private fun setData(data: List<ListReportEntity>) {
        modelHolderList.clear()
        modelHolderList.addAll(data)
    }

    fun clear() {
        modelHolderList.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListReportHolder {
        val layoutView = layoutInflater.inflate(R.layout.list_report_model_row, parent, false)
        return ListReportHolder(layoutView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ListReportHolder, position: Int) {
        val modelHolder = modelHolderList[position]

        val modelConfig = modelHolder.modelConfig
        val title: String = if (modelHolder.modelType.isTextModel()) {
            modelHolder.modelName
        } else {
            val sizes = modelConfig.inputSize.toString()
            val floating = modelConfig.quantizedStr()
            String.format("%s (%s, %s)", modelHolder.modelName, floating, sizes)
        }

        holder.textViewTitle.text = title
        val date = DateUtils.getSimpleReadableDateTime(modelHolder.createdAt)

        holder.textViewCreatedAt.text = "Created at $date"
        holder.textViewDescription.text = getText(modelHolder)

        holder.textViewDelete.setOnClickListener {
            remove(position)
            listener.onDeleteItem(modelHolder)
        }

        holder.itemView.setOnClickListener {
            listener.onClickItem(modelHolder)
        }
    }

    private fun getText(listReportEntity: ListReportEntity): String {
        val reportItems = listReportEntity.reportDelegateItems


        var maxThreads = reportItems.firstOrNull()?.threads ?: 1
        var minThreads = reportItems.firstOrNull()?.threads ?: 1
        var useXnnpack = false

        devices.clear()
        val strDetails = StringBuilder()
        reportItems.forEachNoIterator { item ->
            if (item.useXnnpack) {
                useXnnpack = true
            }
            minThreads = minOf(minThreads, item.threads)
            maxThreads = maxOf(maxThreads, item.threads)
            if (!devices.contains(item.device)) {
                if (strDetails.isNotEmpty()) {
                    strDetails.append(", ")
                } else {
                    strDetails.append("Delegates: ")
                }
                strDetails.append(item.device.name)
                devices.add(item.device)
            }
        }

        if (strDetails.isNotEmpty()) {
            strDetails.append(". ")
        }

        strDetails.append("Threads ")
        if (maxThreads - minThreads > 1) {
            strDetails.apply {
                append(minThreads)
                append("-")
                append(maxThreads)
            }
        } else {
            strDetails.append(maxThreads)
        }

        if (useXnnpack) {
            strDetails.append(", XNNPACK")
        }

        val imageCount = reportItems.firstOrNull()?.batchImageCount ?: 1
        strDetails.apply {
            append(", Batch count ")
            append(imageCount)
        }
        return strDetails.toString()
    }

    override fun getItemCount(): Int {
        return modelHolderList.size
    }

    private fun remove(position: Int) {
        modelHolderList.removeAt(position)
        notifyDataSetChanged()
    }

    interface ClickModelItemListener {
        fun onClickItem(item: ListReportEntity)
        fun onDeleteItem(item: ListReportEntity)
    }
}