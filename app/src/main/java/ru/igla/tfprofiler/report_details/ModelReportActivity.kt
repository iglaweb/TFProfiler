package ru.igla.tfprofiler.report_details

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.igla.tfprofiler.R
import ru.igla.tfprofiler.SharedViewModel
import ru.igla.tfprofiler.reports_list.ListReportEntity
import ru.igla.tfprofiler.utils.inTransaction

class ModelReportActivity : AppCompatActivity() {

    private val sharedItem: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_model)

        if (savedInstanceState == null) {
            val reportData = intent?.getParcelableExtra<ListReportEntity>(EXTRA_KEY_REPORT_DATA)
            check(reportData != null) { "Report data is not provided" }

            reportData.apply {
                sharedItem.setModelData(reportData)
                supportFragmentManager.inTransaction {
                    replace(
                        R.id.content,
                        ModelReportFragment().withArguments(
                            reportData,
                            true
                        ),
                        TAG_REPORT
                    )
                }
            }
        }
    }
}
