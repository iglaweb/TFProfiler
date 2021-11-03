package ru.igla.tfprofiler.media_track

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_video_layout.*
import kotlinx.android.synthetic.main.inference_info.*
import kotlinx.coroutines.*
import ru.igla.tfprofiler.R
import ru.igla.tfprofiler.TFProfilerApp
import ru.igla.tfprofiler.core.RequestMode
import ru.igla.tfprofiler.core.Status
import ru.igla.tfprofiler.core.intents.IntentManager
import ru.igla.tfprofiler.models_list.DelegateRunRequest
import ru.igla.tfprofiler.models_list.ExtraMediaRequest
import ru.igla.tfprofiler.models_list.NeuralModelsListFragment
import ru.igla.tfprofiler.report_details.ModelReportActivity
import ru.igla.tfprofiler.report_details.ModelReportFragment
import ru.igla.tfprofiler.tflite_runners.base.ModelOptions
import ru.igla.tfprofiler.ui.BaseFragment
import ru.igla.tfprofiler.ui.widgets.toast.Toaster
import ru.igla.tfprofiler.utils.*
import ru.igla.tfprofiler.video.FileUtils
import ru.igla.tfprofiler.video.ImageIntentUtils
import java.util.*
import kotlin.coroutines.CoroutineContext


class VideoRecognizeFragment :
    BaseFragment(R.layout.fragment_video_layout),
    CoroutineScope {

    private val exceptionHandler by lazy { ExceptionHandler { _, _ -> } } // for cancellation exception

    private lateinit var recognitionViewModel: VideoRecognitionViewModel

    override val coroutineContext: CoroutineContext
        get() = dispatcherProvider.background

    private val dispatcherProvider by lazy { DispatcherProviderImpl() }

    private var outputFileUri: Uri? = null

    private val mToaster: Toaster by lazy {
        Toaster(
            TFProfilerApp.instance
        )
    }

    fun withArguments(
        mediaRequest: ExtraMediaRequest,
        delegateRunRequest: DelegateRunRequest
    ): VideoRecognizeFragment =
        apply {
            arguments = bundleOf(
                NeuralModelsListFragment.MEDIA_ITEM to mediaRequest,
                NeuralModelsListFragment.MODEL_OPTIONS to delegateRunRequest
            )
        }

    private fun renderModelDelegateDetails(modelOptions: ModelOptions) {
        delegate_details.text = recognitionViewModel.getDelegateDetails(modelOptions)
    }

    private fun openReport() {
        val intent = Intent(context, ModelReportActivity::class.java).apply {
            putExtra(
                ModelReportFragment.EXTRA_KEY_REPORT_DATA,
                recognitionViewModel.getReportData()
            )
        }
        IntentUtils.startFragmentForResultSafely(
            this,
            ModelReportFragment.REPORT_REQUEST_CODE,
            intent
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnSelectPhoto.setOnClickListener {
            val intentManager = IntentManager.create(this)
            outputFileUri = ImageIntentUtils.openImageIntent(intentManager)
        }
        btnSettings.setOnClickListener {
            openReport()
        }

        val mediaRequest: ExtraMediaRequest? =
            arguments?.getParcelable(NeuralModelsListFragment.MEDIA_ITEM)
        if (mediaRequest == null) {
            mToaster.showToast("Passed request is null")
            activity?.finish()
            return
        }

        if (mediaRequest.mediaPath.isEmpty()) {
            mToaster.showToast("Media path is empty")
            activity?.finish()
            return
        }

        val factory = ModelFactory(
            requireActivity().application,
            mediaRequest.modelEntity
        )
        recognitionViewModel = ViewModelProvider(
            this,
            factory
        ).get(
            VideoRecognitionViewModel::class.java
        ).apply {

            val delegateRunRequest: DelegateRunRequest? =
                arguments?.getParcelable(NeuralModelsListFragment.MODEL_OPTIONS)
            val queue: Queue<ModelOptions> =
                resolveRunDelegatesExtra(delegateRunRequest, mediaRequest.modelEntity)

            previewImageLiveData.observe(viewLifecycleOwner) {
                ivLastImagePreview.clearAndSetBitmapNoRefresh(it)
            }

            //observe frame processing for progress
            livedataProcessFrameInfo.observe(
                viewLifecycleOwner
            ) {
                logI { "Progress: ${it.progress}%" }

                progressBar.progress = it.progress
                val frameNumber = it.frameNumber
                val totalFrames = it.framesCount
                tvExtraInformation.text = "Frame #$frameNumber/$totalFrames"
            }

            observeDelegateCreator(mediaRequest, queue)

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

    private fun VideoRecognitionViewModel.observeDelegateCreator(
        mediaRequest: ExtraMediaRequest,
        queue: Queue<ModelOptions>
    ) {
        liveDataCreateDelegate.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    renderModelDelegateDetails(it.data!!)
                    runRecognition(mediaRequest, queue)
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
                            recognitionViewModel.setModelOptions(this)
                        } ?: showFinalizeDialog()
                    }
                }
            }
        }
    }

    private fun VideoRecognitionViewModel.observeRecognitionResult() {
        // recognition result
        liveDataBitmapOutput.observe(
            viewLifecycleOwner
        ) {
            inferenceInfoLayout.showBitmapInfo(it)
            val state = it.statOutResult
            inferenceInfoLayout.showStat(state)
        }
    }

    private fun runRecognition(
        mediaRequest: ExtraMediaRequest,
        queue: Queue<ModelOptions>
    ) {
        launch(Dispatchers.Default + exceptionHandler) {
            try {
                if (mediaRequest.requestMode == RequestMode.VIDEO) {
                    recognitionViewModel.recognizeVideo(mediaRequest.mediaPath)
                } else if (mediaRequest.requestMode == RequestMode.DATASET) {
                    recognitionViewModel.recognizeImageDataset(mediaRequest.mediaPath)
                }
            } catch (e: CancellationException) {
                //ignore
            }

            withContext(Dispatchers.Main) {
                onEndRecognition()
                if (isActive) {
                    // run remaining options
                    queue.poll()?.apply {
                        recognitionViewModel.setModelOptions(this)
                    } ?: showFinalizeDialog()
                }
            }
        }
    }

    private fun showFinalizeDialog() {
        val reportData = recognitionViewModel.getReportData()
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

    private fun recognizeUri(context: Context, selectedImageUri: Uri) {
        onStartRecognition()
        launch(Dispatchers.Default) {
            val filePath = FileUtils.getRealFilePath(context, selectedImageUri)
            logI { filePath }

            val type = ImageIntentUtils.getMimeType(context, selectedImageUri)
            if (recognitionViewModel.isVideoFile(type)) {
                recognizeVideo(context, selectedImageUri)
            } else {
                val isSuccess = recognitionViewModel.recognizePhoto(context, selectedImageUri)
                if (!isSuccess) {
                    return@launch
                }
            }
            onEndRecognition()
        }
    }

    private suspend fun recognizeVideo(context: Context, selectedImageUri: Uri) {
        logI { "Read image frames..." }
        val filePath = FileUtils.getRealFilePath(context, selectedImageUri).trim()
        recognitionViewModel.recognizeVideo(filePath)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ImageIntentUtils.SELECT_PICTURE_REQUEST_CODE) {
                onSelectRequestCode(data)
            } else if (requestCode == ModelReportFragment.REPORT_REQUEST_CODE) {
                activity?.finish()
            }
        }
    }

    private fun onSelectRequestCode(data: Intent?) {
        val isCamera: Boolean = if (data == null || data.data == null) {
            true
        } else {
            val action = data.action
            if (action == null) {
                false
            } else {
                action == android.provider.MediaStore.ACTION_IMAGE_CAPTURE
            }
        }
        val selectedImageUri = if (isCamera) {
            outputFileUri
        } else {
            data?.data
        }
        val context = context?.applicationContext
        if (selectedImageUri == null || context == null) {
            Snackbar.make(btnSelectPhoto, "Cannot select photo!", Snackbar.LENGTH_LONG).show()
            return
        }

        recognizeUri(context, selectedImageUri)
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
            ivLastImagePreview.setImageDrawable(null)
        }
    }
}
