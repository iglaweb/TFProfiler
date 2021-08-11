package ru.igla.tfprofiler.reports_list

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_main_report_list.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import ru.igla.tfprofiler.R
import ru.igla.tfprofiler.core.SharedViewModel
import ru.igla.tfprofiler.report_details.ModelReportFragment
import ru.igla.tfprofiler.ui.BaseFragment
import ru.igla.tfprofiler.utils.inTransaction
import kotlin.coroutines.CoroutineContext


class ReportsListFragment : BaseFragment(R.layout.fragment_main_report_list), CoroutineScope {

    companion object {
        const val TAG = "report_detail"
    }

    private val sharedItem: SharedViewModel by activityViewModels()

    private val reportListViewModel: ReportListViewModel by viewModels()

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main

    override val coroutineContext: CoroutineContext
        get() = Job() + uiDispatcher

    private lateinit var modelsListAdapter: ReportsListRecyclerViewAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter(requireContext())
        subscribeUi(reportListViewModel.reportsLiveData)
    }

    private fun openReport(reportData: ListReportEntity) {
        sharedItem.setModelData(reportData)

        activity?.supportFragmentManager?.let { fr ->
            val fragment = ModelReportFragment().withArguments(
                reportData,
                false
            )
            fr.inTransaction {
                addToBackStack(TAG)
                add(R.id.content, fragment)
                val contentFragment = fr.findFragmentById(R.id.content)
                hide(contentFragment!!)
            }
        }
    }

    private fun initAdapter(context: Context) {
        modelsListAdapter =
            ReportsListRecyclerViewAdapter(
                context,
                object :
                    ReportsListRecyclerViewAdapter.ClickModelItemListener {
                    override fun onClickItem(item: ListReportEntity) {
                        openReport(item)
                    }

                    override fun onDeleteItem(item: ListReportEntity) {
                        reportListViewModel.deleteReport(item)
                    }
                })


        listModels.run {
            setEmptyView(empty_view)
            setHasFixedSize(true)
            isNestedScrollingEnabled = true
            val linearLayoutManager = LinearLayoutManager(context).apply {
                reverseLayout = true
                stackFromEnd = true
            }
            this.layoutManager = linearLayoutManager
            this.adapter = modelsListAdapter

            val itemDecor = DividerItemDecoration(
                context,
                linearLayoutManager.orientation
            ).apply {
                val drawable = ContextCompat.getDrawable(context, R.drawable.list_divider)
                if (drawable != null) {
                    setDrawable(drawable)
                }
            }
            addItemDecoration(itemDecor)
        }
    }

    private fun subscribeUi(liveData: LiveData<List<ListReportEntity>>) {
        // Update the list when the data changes
        liveData.observe(
            viewLifecycleOwner
        ) { modelsList: List<ListReportEntity> ->
            modelsListAdapter.notifyAdapterItems(modelsList)
        }
    }
}
