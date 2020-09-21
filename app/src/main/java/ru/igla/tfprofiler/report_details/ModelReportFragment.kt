package ru.igla.tfprofiler.report_details

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_model_report_details_dialog.*
import kotlinx.coroutines.*
import ru.igla.tfprofiler.NeuralModelApp
import ru.igla.tfprofiler.R
import ru.igla.tfprofiler.SharedViewModel
import ru.igla.tfprofiler.UseCase
import ru.igla.tfprofiler.core.Timber
import ru.igla.tfprofiler.media_track.MediaPathProvider
import ru.igla.tfprofiler.reports_list.ListReportEntity
import ru.igla.tfprofiler.ui.BaseFragment
import ru.igla.tfprofiler.ui.widgets.toast.Toaster
import ru.igla.tfprofiler.utils.IntentUtils
import kotlin.coroutines.CoroutineContext


const val EXTRA_KEY_REPORT_DATA = "data_report"
const val EXTRA_KEY_EDIT_DATA = "edit_report"

const val TAG_REPORT = "report_tag"

const val REPORT_REQUEST_CODE = 100

class ModelReportFragment :
    BaseFragment(),
    CoroutineScope {

    fun withArguments(data: ListReportEntity, edit: Boolean): ModelReportFragment =
        apply {
            arguments = bundleOf(
                EXTRA_KEY_REPORT_DATA to data,
                EXTRA_KEY_EDIT_DATA to edit
            )
        }

    private val mToaster: Toaster by lazy { Toaster(NeuralModelApp.instance) }

    private val reportDetailsViewModel: ReportDetailsViewModel by viewModels()

    private val sharedItem: SharedViewModel by activityViewModels()

    private lateinit var reportsListAdapterDetails: ModelsReportDetailsRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_model_report_details_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar?.title = "Model Report"

        ContextCompat.getDrawable(requireContext(), R.drawable.ic_close)?.let { closeDrawable ->
            tintToolbarButton(toolbar, closeDrawable)
            toolbar.navigationIcon = closeDrawable
            toolbar.setNavigationOnClickListener {
                Timber.i("Navigation clicked")
                activity?.onBackPressed()
            }
        }

        val editReport: Boolean =
            arguments?.getBoolean(EXTRA_KEY_EDIT_DATA) == true

        val menu = toolbar.menu
        if (editReport) {
            val itemConfirmButtonId = 1
            menu.add(0, itemConfirmButtonId, 0, "Save").apply {
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                setOnMenuItemClickListener { item ->
                    if (item.itemId == itemConfirmButtonId) {
                        sharedItem.modelsLiveData.value?.apply {
                            reportDetailsViewModel.saveReportDb(this)
                        }
                        true
                    } else false
                }
            }

            reportDetailsViewModel.liveDataSaveDb.observe(viewLifecycleOwner, { status ->
                if (status == UseCase.Status.SUCCESS) {
                    mToaster.showToast("Report saved!")
                    activity?.setResult(Activity.RESULT_OK)
                    activity?.finish()
                } else {
                    mToaster.showToast("Failed to save report in db. Report possibly contains invalid values")
                }
            })
        }


        val itemExportButtonId = 1
        menu.add(0, itemExportButtonId, 0, "Export CSV").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER) //overflow
            setOnMenuItemClickListener { item ->
                if (item.itemId == itemExportButtonId) {
                    launch(Dispatchers.Main) {
                        val csvPath = MediaPathProvider.getRootPath(requireContext()) + "/file.csv"
                        sharedItem.modelsLiveData.value?.apply {
                            reportDetailsViewModel.saveReportCsv(this)
                        }
                        withContext(Dispatchers.Main) {
                            openCsvWith(csvPath)
                        }
                    }
                    true
                } else false
            }
        }

        reportDetailsViewModel.liveDataCsvReport.observe(viewLifecycleOwner, {
            mToaster.showToast("Report csv saved")
        })

        initAdapter(requireContext())
        sharedItem.modelsLiveData.observe(viewLifecycleOwner, {

            reportsListAdapterDetails.notifyAdapterItems(it)
        })
    }

    private fun openCsvWith(fileUriString: String) {
        Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, Uri.parse(fileUriString))
            type = "text/csv"
            IntentUtils.startActivitySafely(
                requireContext(),
                Intent.createChooser(this, "Open file with")
            )
        }
    }

    private fun initAdapter(context: Context) {
        reportsListAdapterDetails =
            ModelsReportDetailsRecyclerViewAdapter(
                context
            )

        listReports.run {
            setEmptyView(empty_view)
            setHasFixedSize(true)
            isNestedScrollingEnabled = true
            val linearLayoutManager = LinearLayoutManager(context).apply {
                reverseLayout = true
                stackFromEnd = true
            }
            this.layoutManager = linearLayoutManager
            this.adapter = reportsListAdapterDetails
        }
    }

    private fun tintToolbarButton(toolbar: Toolbar, buttonDrawable: Drawable) {
        val colorAttrs = intArrayOf(R.attr.colorControlNormal)
        toolbar.context.obtainStyledAttributes(colorAttrs).apply {
            val color = getColor(0, -1)
            recycle()
            DrawableCompat.setTint(DrawableCompat.wrap(buttonDrawable), color)
        }
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main

    override val coroutineContext: CoroutineContext
        get() = Job() + uiDispatcher
}
