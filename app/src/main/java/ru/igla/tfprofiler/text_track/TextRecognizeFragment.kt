package ru.igla.tfprofiler.text_track

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.fragment_text_layout.*
import kotlinx.android.synthetic.main.inference_info.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.igla.tfprofiler.R
import ru.igla.tfprofiler.TFProfilerApp
import ru.igla.tfprofiler.core.Status
import ru.igla.tfprofiler.models_list.DelegateRunRequest
import ru.igla.tfprofiler.models_list.ExtraTextRequest
import ru.igla.tfprofiler.models_list.NeuralModelsListFragment
import ru.igla.tfprofiler.report_details.ModelReportActivity
import ru.igla.tfprofiler.report_details.ModelReportFragment
import ru.igla.tfprofiler.tflite_runners.base.ModelOptions
import ru.igla.tfprofiler.ui.BaseFragment
import ru.igla.tfprofiler.ui.widgets.toast.Toaster
import ru.igla.tfprofiler.utils.*
import java.util.*
import kotlin.coroutines.CoroutineContext


class TextRecognizeFragment :
    BaseFragment(R.layout.fragment_text_layout),
    CoroutineScope {

    private val exceptionHandler by lazy { ExceptionHandler { _, _ -> } } // for cancellation exception

    private lateinit var viewModel: TextRecognitionViewModel

    override val coroutineContext: CoroutineContext
        get() = dispatcherProvider.background

    private val dispatcherProvider by lazy { DispatcherProviderImpl() }

    private val mToaster: Toaster by lazy {
        Toaster(
            TFProfilerApp.instance
        )
    }

    // Coroutine listening for Query
    private var queryUpdatesJob: Job? = null

    // Coroutine to process dataset
    private var detectDatasetJob: Job? = null

    fun withArguments(
        extraTextRequest: ExtraTextRequest,
        delegateRunRequest: DelegateRunRequest
    ): TextRecognizeFragment =
        apply {
            arguments = bundleOf(
                NeuralModelsListFragment.MEDIA_ITEM to extraTextRequest,
                NeuralModelsListFragment.MODEL_OPTIONS to delegateRunRequest
            )
        }

    private fun renderModelDelegateDetails(modelOptions: ModelOptions) {
        delegate_details.text = viewModel.getDelegateDetails(modelOptions)
    }

    private fun showInference(inferenceTime: String) {
        inference_info.text = inferenceTime
    }

    private fun showFps(fps: String) {
        fps_info.text = fps
    }

    private fun showMemoryUsage(memoryUsage: String) {
        memory_info.text = memoryUsage
    }

    private fun showInitTime(time: String) {
        tvInitTime.text = time
    }

    @SuppressLint("SetTextI18n")
    private fun showMeanTime(mean: Double, std: Double) {
        if (java.lang.Double.isNaN(mean) || java.lang.Double.isNaN(std)) {
            tvMeanInterferenceTime.text = "Not defined"
        } else {
            val statsStr = String.format(
                Locale.getDefault(),
                "%.2f ± %.2f ms",
                mean,
                std
            )
            tvMeanInterferenceTime.text = statsStr
        }
    }

    private fun openReport() {
        val intent = Intent(context, ModelReportActivity::class.java).apply {
            putExtra(
                ModelReportFragment.EXTRA_KEY_REPORT_DATA,
                viewModel.getReportData()
            )
        }
        IntentUtils.startFragmentForResultSafely(
            this,
            ModelReportFragment.REPORT_REQUEST_CODE,
            intent
        )
    }

    private fun SearchView.getQueryTextChangeStateFlow(): StateFlow<String> {
        val query = MutableStateFlow("")
        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                query.value = newText
                return true
            }
        })
        return query
    }

    @ExperimentalCoroutinesApi
    override fun onStart() {
        super.onStart()
        queryUpdatesJob = launch(Dispatchers.Default + exceptionHandler) {
            viewModel.startSearchFlow(searchView.getQueryTextChangeStateFlow())
        }
    }

    override fun onStop() {
        queryUpdatesJob?.cancel()
        detectDatasetJob?.cancel()
        super.onStop()
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //hide image specific fields
        frameInfoContainer.visibility = View.GONE
        cropInfoContainer.visibility = View.GONE

        textViewResult.text = ""
        btnSettings.setOnClickListener {
            openReport()
        }

        val extraTextRequest: ExtraTextRequest? =
            arguments?.getParcelable(NeuralModelsListFragment.MEDIA_ITEM)
        if (extraTextRequest == null) {
            mToaster.showToast("Model is null")
            activity?.finish()
            return
        }

        val factory = ModelFactory(
            requireActivity().application,
            extraTextRequest.modelEntity
        )

        viewModel = ViewModelProvider(this, factory)
            .get(TextRecognitionViewModel::class.java).apply {
                val delegateRunRequest: DelegateRunRequest? =
                    arguments?.getParcelable(NeuralModelsListFragment.MODEL_OPTIONS)
                val queue: Queue<ModelOptions> =
                    resolveRunDelegatesExtra(delegateRunRequest, extraTextRequest.modelEntity)

                observeDelegateCreator(queue)

                liveDataShowRecognitionError.observe(viewLifecycleOwner) {
                    mToaster.showToast(it.message)
                }

                observeRecognitionResult()

                //start first recognition
                queue.poll()?.apply {
                    setModelOptions(this)
                }
            }
    }

    private fun goNextConfigRequest(queue: Queue<ModelOptions>) {
        detectDatasetJob = launch(Dispatchers.Default + exceptionHandler) {
            try {
                viewModel.startRecognitionDataset()
            } catch (e: CancellationException) {
                //ignore
            }

            withContext(Dispatchers.Main) {
                onEndRecognition()
                if (isActive) {
                    // run remaining options
                    queue.poll()?.apply {
                        viewModel.setModelOptions(this)
                    } ?: showFinalizeDialog()
                }
            }
        }
    }

    private fun TextRecognitionViewModel.observeDelegateCreator(
        queue: Queue<ModelOptions>
    ) {
        liveDataCreateDelegate.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    renderModelDelegateDetails(it.data!!)
                    onFinishInitClassifier()

                    goNextConfigRequest(queue)
                }
                Status.LOADING -> {
                    onStartRecognition()
                }
                Status.ERROR -> {
                    //may be classifier is not supported
                    onEndRecognition()

                    //get next request
                    if (isActive) {
                        mToaster.showToast("Classifier failed to create. \n" + it.message)
                        // run remaining options
                        queue.poll()?.apply {
                            viewModel.setModelOptions(this)
                        } ?: showFinalizeDialog()
                    }
                }
            }
        }
    }

    private fun TextRecognitionViewModel.observeRecognitionResult() {
        // recognition result
        liveDataTextOutput.observe(
            viewLifecycleOwner
        ) {

            if (it.status == Status.SUCCESS) {
                textViewResult.text = it.data?.text?.let { results ->
                    val sb = StringBuilder()
                    results.forEachNoIterator { res ->
                        val conf = res.confidence
                        sb.append(
                            String.format("%s with score %.2f", res.label.label, (100 * conf)) + "%"
                        )
                        sb.append("\n")
                    }
                    sb.toString()
                } ?: ""

                it.data?.statOutResult?.let { stat ->
                    showInference(stat.inferenceTime.toString() + " ms")
                    showFps(stat.fps.toString())

                    val memoryStr = StringUtils.getReadableFileSize(stat.memoryUsage, true)
                    showMemoryUsage(memoryStr)

                    showInitTime("" + stat.initTime + " ms")
                    showMeanTime(stat.meanTime, stat.stdTime)
                }
            } else if (it.status == Status.ERROR) {
                mToaster.showToast(it.message ?: "Error occurred")
                textViewResult.text = ""
            }
        }
    }

    private fun showFinalizeDialog() {
        val reportData = viewModel.getReportData()
        var success = 0
        var fail = 0
        reportData.reportDelegateItems.forEachNoIterator {
            if (it.exception.isNullOrEmpty()) {
                success++
            } else {
                fail++
            }
        }
        runOnUiThreadIfFragmentAlive {
            val dialog = MaterialDialog(requireContext())
                .title(text = "Ready report")
                .message(text = "Report is ready to explore!\r\nSuccess runs: $success, failed: $fail")
                .positiveButton(text = "See report") {
                    openReport()
                }
                .negativeButton(R.string.dismiss)
            dialog.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ModelReportFragment.REPORT_REQUEST_CODE) {
                activity?.finish()
            }
        }
    }

    private fun onFinishInitClassifier() {
        runOnUiThreadIfFragmentAlive {
            tvExtraInformation.text = ""
            progressBar1.visibility = View.GONE
        }
    }

    private fun onEndRecognition() {
        runOnUiThreadIfFragmentAlive {
            tvExtraInformation.text = ""
            progressBar1.visibility = View.GONE
        }
    }

    private fun onStartRecognition() {
        runOnUiThreadIfFragmentAlive {
            errorTrace.text = "" //clear before start recognition
            progressBar1.visibility = View.VISIBLE
            textViewResult.text = ""
        }
    }
}
