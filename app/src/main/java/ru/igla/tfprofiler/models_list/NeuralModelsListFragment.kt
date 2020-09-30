package ru.igla.tfprofiler.models_list

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.assent.Permission
import com.afollestad.assent.runWithPermissions
import com.afollestad.materialdialogs.DialogBehavior
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.ModalDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_main_models_list.*
import kotlinx.coroutines.*
import ru.igla.tfprofiler.BuildConfig
import ru.igla.tfprofiler.R
import ru.igla.tfprofiler.TFProfilerApp
import ru.igla.tfprofiler.core.Device
import ru.igla.tfprofiler.core.ErrorDialog
import ru.igla.tfprofiler.core.RequestMode
import ru.igla.tfprofiler.core.Timber
import ru.igla.tfprofiler.media_track.MediaTrackUtils
import ru.igla.tfprofiler.media_track.VideoRecognizeActivity
import ru.igla.tfprofiler.model_in_camera.DetectorActivity
import ru.igla.tfprofiler.prefs.AndroidPreferenceManager
import ru.igla.tfprofiler.ui.BaseFragment
import ru.igla.tfprofiler.ui.widgets.toast.Toaster
import ru.igla.tfprofiler.utils.IntentUtils
import ru.igla.tfprofiler.utils.StringUtils
import ru.igla.tfprofiler.utils.ViewUtils
import kotlin.coroutines.CoroutineContext


class NeuralModelsListFragment : BaseFragment(R.layout.fragment_main_models_list), CoroutineScope {

    companion object {
        private const val FRAGMENT_DIALOG = "dialog"

        private const val REQUEST_PICK_MODEL = 42
        private const val REQUEST_SELECT_VIDEO = 43

        const val MODEL_OPTIONS = "model_options"
        const val MEDIA_ITEM = "media_item"
        const val EXTRA_CAMERA_TYPE = "camera_type"
    }

    private val modelDeleteUseCase by lazy {
        ModelDeleteUseCase(TFProfilerApp.instance)
    }

    private var configureRunTFDialog: Dialog? = null

    private var selectedModelOptionsVideo: ModelEntity? = null

    private val listNeuralModelsViewModel: ListNeuralModelsViewModel by viewModels()

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main

    override val coroutineContext: CoroutineContext
        get() = Job() + uiDispatcher

    private val preferenceManager by lazy { AndroidPreferenceManager(TFProfilerApp.instance).defaultPrefs }

    private lateinit var modelsListAdapter: ModelsListRecyclerViewAdapter

    private val mToaster: Toaster by lazy {
        Toaster(
            TFProfilerApp.instance
        )
    }

    @SuppressLint("SetTextI18n")
    private fun showCustomViewDialog(
        dialogBehavior: DialogBehavior = ModalDialog,
        item: ModelEntity
    ) {
        val context = requireContext()
        val dialog = MaterialDialog(context, dialogBehavior).show {
            title(text = item.name)
            customView(
                R.layout.bottom_sheet_model_short_info,
                scrollable = true,
                horizontalPadding = true
            )
            positiveButton(text = "Camera") {
                openModel(RequestMode.CAMERA, item)
            }
            neutralButton(text = "Dataset") {
                openModel(RequestMode.DATASET, item)
            }
            negativeButton(text = "Video") {
                openModel(RequestMode.VIDEO, item)
            }
            debugMode(false)
        }
        // Setup custom view content
        dialog.getCustomView().let { customView ->

            val tvModelDetails: TextView = customView.findViewById(R.id.delegateDetails)
            tvModelDetails.text = item.details

            val tvModelSize: TextView = customView.findViewById(R.id.modelSize)
            tvModelSize.text = "${item.inputSize}x${item.inputSize}"

            val tvModelType: TextView = customView.findViewById(R.id.tvModelTypeFloating)
            tvModelType.text = if (item.quantized) "Quantized" else "Floating"

            if (StringUtils.isNullOrEmpty(item.source)) {
                customView.findViewById<View>(R.id.modelSource).visibility = View.GONE
                customView.findViewById<View>(R.id.modelSourceTitle).visibility = View.GONE
            } else {
                val tvModelSource: TextView = customView.findViewById(R.id.modelSource)
                tvModelSource.text = item.source
                tvModelSource.visibility = View.VISIBLE
                customView.findViewById<View>(R.id.modelSourceTitle).visibility = View.VISIBLE
            }
        }
    }

    private fun openModel(requestMode: RequestMode, item: ModelEntity) {
        val cameraType = preferenceManager.cameraType
        openModelItem(item, requestMode, cameraType)
    }

    private fun showFileChooserButtons(modelEntity: ModelEntity) =
        runWithPermissions(Permission.READ_EXTERNAL_STORAGE) {
            selectedModelOptionsVideo = modelEntity
            Intent().apply {
                type = "file/*"
                action = Intent.ACTION_GET_CONTENT
                IntentUtils.startFragmentForResultSafely(
                    this@NeuralModelsListFragment,
                    REQUEST_SELECT_VIDEO,
                    Intent.createChooser(this, "Select video")
                )
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (view.findViewById(R.id.fab) as FloatingActionButton).apply {
            setOnClickListener {
                pickTfliteModel()
            }
        }
        initAdapter(requireContext())
        subscribeUi(listNeuralModelsViewModel.modelsListLiveData)

        listNeuralModelsViewModel.liveDataAddNewModel.observe(
            viewLifecycleOwner
        ) { resource ->
            if (resource.isSuccess()) {
                mToaster.showToast("Successfully added model")
            } else {
                ErrorDialog
                    .newInstance("Failed to add model.\r\nError: " + resource.t?.message)
                    .show(childFragmentManager, FRAGMENT_DIALOG)
            }
        }
    }

    private fun pickTfliteModel() {
        val dialog = AlertDialog.Builder(requireContext())
            .setMessage("Select TFLite model from your phone. Model interference will be measured.")
            .setPositiveButton("Pick") { _, _ ->
                val intent = Intent().apply {
                    type = "file/*"
                    action = Intent.ACTION_GET_CONTENT
                }
                IntentUtils.startFragmentForResultSafely(
                    this,
                    REQUEST_PICK_MODEL,
                    Intent.createChooser(intent, "Select model")
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_PICK_MODEL -> {
                    data?.apply {
                        val uri = data.data
                        uri?.apply {
                            onSelectTfliteModel(uri)
                        }
                    }
                }
                REQUEST_SELECT_VIDEO -> {
                    data?.apply {
                        val selectedImageUri = data.data
                        selectedImageUri?.apply {
                            openVideo(this)
                        }
                    }
                }
            }
        }
    }

    private fun onSelectTfliteModel(uri: Uri) {
        launch(Dispatchers.IO) {
            val filePath =
                MediaTrackUtils.getRealFilePath(requireContext(), uri).trim()

            if (filePath.endsWith(".tflite")) {
                withContext(Dispatchers.Main) {
                    mToaster.showToast("TFlite path: $filePath")
                }
                listNeuralModelsViewModel.addCustomTfliteModel(filePath)
            } else {
                withContext(Dispatchers.Main) {
                    mToaster.showToast("Selected file is not .tflite model: $filePath")
                }
            }
        }
    }

    private fun openVideo(selectedImageUri: Uri) {
        launch(Dispatchers.IO) {
            try {
                val selectedImagePath =
                    MediaTrackUtils.getRealFilePath(requireContext(), selectedImageUri).trim()
                withContext(Dispatchers.Main) {
                    mToaster.showToast("Selected file: $selectedImagePath")
                }

                val modelEntity =
                    selectedModelOptionsVideo ?: throw Exception("Passed model entity is null")

                val delegateRunRequest = getDelegateRequest()
                if (delegateRunRequest.deviceList.isEmpty()) {
                    throw Exception("None of the delegates selected")
                }

                val intent = Intent(context, VideoRecognizeActivity::class.java).apply {
                    val mediaRequest =
                        MediaRequest(RequestMode.VIDEO, selectedImagePath, modelEntity)
                    putExtra(MEDIA_ITEM, mediaRequest)
                    putExtra(MODEL_OPTIONS, delegateRunRequest)
                }
                IntentUtils.startActivitySafely(requireContext(), intent)
            } catch (e: Exception) {
                Timber.d(e)
                withContext(Dispatchers.Main) {
                    mToaster.showToast(e.message)
                }
            }
        }
    }

    private fun initAdapter(context: Context) {
        modelsListAdapter =
            ModelsListRecyclerViewAdapter(
                context,
                object :
                    ModelsListRecyclerViewAdapter.ClickModelItemListener {
                    override fun onClickItem(item: ModelEntity) {
                        showCustomViewDialog(BottomSheet(LayoutMode.MATCH_PARENT), item)
                    }

                    override fun onDeleteItem(item: ModelEntity) {
                        launch(Dispatchers.IO) {
                            modelDeleteUseCase.executeUseCase(
                                ModelDeleteUseCase.RequestValues(item.tableId)
                            )
                        }
                    }
                }
            )

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

    private fun subscribeUi(liveData: LiveData<List<ModelEntity>>) {
        // Update the list when the data changes
        liveData.observe(
            viewLifecycleOwner
        ) { modelsList: List<ModelEntity> ->
            modelsListAdapter.notifyAdapterItems(modelsList)
            listModels.layoutManager?.scrollToPosition(0)
        }
    }

    private fun openModelItem(
        modelEntity: ModelEntity,
        requestMode: RequestMode,
        cameraType: CameraType
    ) {
        when (requestMode) {
            RequestMode.VIDEO -> {
                showFileChooserButtons(modelEntity)
            }
            RequestMode.CAMERA -> {
                onClickCamera(requestMode, modelEntity, cameraType)
            }
            RequestMode.DATASET -> {
                onClickDataset(requestMode, modelEntity)
            }
        }
    }

    private fun onClickCamera(
        requestMode: RequestMode,
        modelEntity: ModelEntity,
        cameraType: CameraType
    ) {
        val mediaRequest = MediaRequest(requestMode, "", modelEntity)
        val intent = Intent(context, DetectorActivity::class.java).apply {
            putExtra(EXTRA_CAMERA_TYPE, cameraType.name)
            putExtra(MEDIA_ITEM, mediaRequest)
        }
        IntentUtils.startActivitySafely(requireContext(), intent)
    }

    override fun onStop() {
        ViewUtils.dismissDialogSafety(configureRunTFDialog)
        super.onStop()
    }

    private fun onClickDataset(requestMode: RequestMode, modelEntity: ModelEntity) {
        val delegateRunRequest = getDelegateRequest()
        if (delegateRunRequest.deviceList.isEmpty()) {
            mToaster.showToast("None of the delegates selected")
            return
        }

        val mediaRequest = MediaRequest(
            requestMode, BuildConfig.ASSET_IMG_DATASET,
            modelEntity
        )
        val intent = Intent(context, VideoRecognizeActivity::class.java).apply {
            putExtra(MEDIA_ITEM, mediaRequest)
            putExtra(MODEL_OPTIONS, delegateRunRequest)
        }
        IntentUtils.startActivitySafely(
            requireContext(),
            intent
        )
    }

    private fun getDelegateRequest(): DelegateRunRequest {
        val modelList = mutableListOf<Device>().apply {
            if (preferenceManager.cpuDelegateEnabled) {
                add(Device.CPU)
            }
            if (preferenceManager.gpuDelegateEnabled) {
                add(Device.GPU)
            }
            if (preferenceManager.nnapiDelegateEnabled) {
                add(Device.NNAPI)
            }
            if (preferenceManager.hexagonDelegateEnabled) {
                add(Device.HEXAGON)
            }
        }

        return DelegateRunRequest(
            IntRange(
                preferenceManager.threadRangeMin,
                preferenceManager.threadRangeMax
            ),
            modelList,
            preferenceManager.xnnpackEnabled
        )
    }
}
