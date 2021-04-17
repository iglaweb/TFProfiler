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
import kotlinx.android.synthetic.main.bottom_sheet_model_custom_input.*
import kotlinx.android.synthetic.main.bottom_sheet_model_custom_input.view.*
import kotlinx.android.synthetic.main.bottom_sheet_model_short_info.view.*
import kotlinx.android.synthetic.main.bottom_sheet_model_short_info.view.delegateDetails
import kotlinx.android.synthetic.main.fragment_main_models_list.*
import kotlinx.coroutines.*
import ru.igla.tfprofiler.BuildConfig
import ru.igla.tfprofiler.R
import ru.igla.tfprofiler.TFProfilerApp
import ru.igla.tfprofiler.core.*
import ru.igla.tfprofiler.media_track.VideoRecognizeActivity
import ru.igla.tfprofiler.model_in_camera.DetectorActivity
import ru.igla.tfprofiler.ui.BaseFragment
import ru.igla.tfprofiler.ui.pick_inference_type.InferenceLaunchListener
import ru.igla.tfprofiler.ui.pick_inference_type.InferenceTypeLauncher
import ru.igla.tfprofiler.ui.widgets.toast.Toaster
import ru.igla.tfprofiler.utils.IntentUtils
import ru.igla.tfprofiler.utils.StringUtils
import ru.igla.tfprofiler.utils.ViewUtils
import ru.igla.tfprofiler.utils.extension
import ru.igla.tfprofiler.video.FileUtils
import java.io.File
import kotlin.coroutines.CoroutineContext


class NeuralModelsListFragment :
    BaseFragment(R.layout.fragment_main_models_list),
    CoroutineScope {

    companion object {
        private const val FRAGMENT_DIALOG = "dialog"

        const val MODEL_OPTIONS = "model_options"
        const val MEDIA_ITEM = "media_item"
        const val EXTRA_CAMERA_TYPE = "camera_type"

        private const val REQUEST_PICK_MODEL = 42
        private const val REQUEST_SELECT_VIDEO = 43
    }

    private var configureRunTFDialog: Dialog? = null

    private var selectedModelOptionsVideo: ModelEntity? = null

    private val listNeuralModelsViewModel: ListNeuralModelsViewModel by viewModels()

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main

    override val coroutineContext: CoroutineContext
        get() = Job() + uiDispatcher

    private lateinit var modelsListAdapter: ModelsListRecyclerViewAdapter

    private val mToaster: Toaster by lazy {
        Toaster(
            TFProfilerApp.instance
        )
    }

    private fun MaterialDialog.interceptCustomDialogClick(
        item: ModelEntity,
        click: (item: ModelEntity) -> Unit
    ) {
        if (item.modelType == ModelType.CUSTOM_OPENCV) {
            val customView = getCustomView()
            val modelWidth = customView.etWidth.text.toString().toInt()
            val modelHeight = customView.etHeight.text.toString().toInt()
            val floatingType = customView.scModelTypeFloating.isChecked
            val grayColor =
                customView.radioGroupChannels.checkedRadioButtonId == R.id.radio_color_1
            val nhwcFormat = customView.radioGroupInputShape.checkedRadioButtonId == R.id.radio_nhwc
            launch(Dispatchers.IO) {
                val newConfig = item.modelConfig.copy(
                    inputWidth = modelWidth,
                    inputHeight = modelHeight,
                    modelFormat = if (floatingType) ModelFormat.FLOATING else ModelFormat.QUANTIZED,
                    colorSpace = if (grayColor) ColorSpace.GRAYSCALE else ColorSpace.COLOR,
                    inputShapeType = if (nhwcFormat) InputShapeType.NHWC else InputShapeType.NCHW
                )
                val newModel = item.copy(modelConfig = newConfig)
                listNeuralModelsViewModel.updateCustomModel(newModel)
                withContext(Dispatchers.Main) {
                    click(item)
                }
            }
        } else {
            click(item)
        }
    }

    private fun createCustomDialog(
        res: Int, dialogBehavior: DialogBehavior = ModalDialog,
        item: ModelEntity
    ): MaterialDialog {
        val context = requireContext()
        return MaterialDialog(context, dialogBehavior).show {
            title(text = item.name)
            customView(
                res,
                scrollable = true,
                horizontalPadding = true
            )
            positiveButton(text = "Camera") {
                interceptCustomDialogClick(item) {
                    openModel(RequestMode.CAMERA, item)
                }
            }
            neutralButton(text = "Dataset") {
                interceptCustomDialogClick(item) {
                    openModel(RequestMode.DATASET, item)
                }
            }
            negativeButton(text = "Video") {
                interceptCustomDialogClick(item) {
                    openModel(RequestMode.VIDEO, item)
                }
            }
            debugMode(false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showCustomInputViewDialog(
        dialogBehavior: DialogBehavior = ModalDialog,
        item: ModelEntity
    ) {
        val dialog =
            createCustomDialog(R.layout.bottom_sheet_model_custom_input, dialogBehavior, item)
        // Setup custom view content
        dialog.getCustomView().let { customView ->
            val tvModelDetails: TextView = customView.delegateDetails
            tvModelDetails.text = item.details

            val modelConfig = item.modelConfig
            customView.etWidth.setText(modelConfig.inputWidth.toString())
            customView.etHeight.setText(modelConfig.inputHeight.toString())

            if (modelConfig.colorSpace == ColorSpace.GRAYSCALE) {
                customView.radioGroupChannels.check(R.id.radio_color_1)
            } else {
                customView.radioGroupChannels.check(R.id.radio_color_3)
            }

            if (modelConfig.inputShapeType == InputShapeType.NHWC) {
                customView.radioGroupInputShape.check(R.id.radio_nhwc)
            } else {
                customView.radioGroupInputShape.check(R.id.radio_nchw)
            }

            customView.scModelTypeFloating.isChecked =
                modelConfig.modelFormat == ModelFormat.FLOATING
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showCustomViewDialog(
        dialogBehavior: DialogBehavior = ModalDialog,
        item: ModelEntity
    ) {
        val dialog =
            createCustomDialog(
                R.layout.bottom_sheet_model_short_info,
                dialogBehavior,
                item
            )
        // Setup custom view content
        dialog.getCustomView().let { customView ->
            val tvModelDetails: TextView = customView.delegateDetails
            tvModelDetails.text = item.details

            val modelConfig = item.modelConfig
            val tvModelSize: TextView = customView.modelSize
            tvModelSize.text = "${modelConfig.inputWidth}x${modelConfig.inputHeight}"

            val tvModelType: TextView = customView.tvModelTypeFloating
            tvModelType.text = modelConfig.quantizedStr()

            if (StringUtils.isNullOrEmpty(item.source)) {
                customView.modelSource.visibility = View.GONE
                customView.modelSourceTitle.visibility = View.GONE
            } else {
                val tvModelSource: TextView = customView.modelSource
                tvModelSource.text = item.source
                tvModelSource.visibility = View.VISIBLE
                customView.modelSourceTitle.visibility = View.VISIBLE
            }
        }
    }

    private fun openModel(requestMode: RequestMode, item: ModelEntity) {
        val cameraType = listNeuralModelsViewModel.getCameraType()
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
        fab.apply {
            setOnClickListener {
                pickInferenceType()
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
                requestIntentChooseModelFile()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }

    private fun requestIntentChooseModelFile() {
        runWithPermissions(Permission.WRITE_EXTERNAL_STORAGE) {// to copy model
            val intent = Intent().apply {
                type = "file/*"
                action = Intent.ACTION_GET_CONTENT
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            IntentUtils.startFragmentForResultSafely(
                this,
                REQUEST_PICK_MODEL,
                Intent.createChooser(intent, "Select model")
            )
        }
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

    private fun pickInferenceType() {
        InferenceTypeLauncher.showInferenceTypeDialog(
            requireContext(), object : InferenceLaunchListener {
                override fun onSelectedOption(selectedOption: InferenceTypeLauncher.InferenceType) {
                    when (selectedOption) {
                        InferenceTypeLauncher.InferenceType.TFLITE -> {
                            pickTfliteModel()
                        }
                        InferenceTypeLauncher.InferenceType.OPENCV -> {
                            requestIntentChooseModelFile()
                        }
                    }
                }
            }
        )
    }

    private fun onSelectTfliteModel(uri: Uri) {
        launch(Dispatchers.IO) {
            val filePath =
                FileUtils.getRealFilePath(requireContext(), uri).trim()

            if (filePath.endsWith(".tflite")) {
                withContext(Dispatchers.Main) {
                    mToaster.showToast("TFlite path: $filePath")
                }
                listNeuralModelsViewModel.addCustomTfliteModel(filePath)
            } else {
                /***
                 * https://docs.opencv.org/4.5.2/d6/d0f/group__dnn.html#ga3b34fe7a29494a6a4295c169a7d32422
                 */
                val opencvSupported = supportedModels.contains(File(filePath).extension)
                if (opencvSupported) {
                    withContext(Dispatchers.Main) {
                        mToaster.showToast("OpenCV model path: $filePath")
                    }
                    listNeuralModelsViewModel.addCustomOpenCVModel(filePath)
                } else {
                    withContext(Dispatchers.Main) {
                        mToaster.showToast("Selected file not supported by opencv: $filePath")
                    }
                }
            }
        }
    }

    private val supportedModels = listOf("caffemodel", "pb", "t7", "onnx", "bin")

    private fun openVideo(selectedImageUri: Uri) {
        launch(Dispatchers.IO) {
            try {
                val mediaRequest = listNeuralModelsViewModel.getMediaRequest(
                    selectedImageUri,
                    selectedModelOptionsVideo
                )

                val delegateRunRequest = listNeuralModelsViewModel.getDelegateRequest()
                if (delegateRunRequest.deviceList.isEmpty()) {
                    throw Exception("None of the delegates selected")
                }

                val intent = Intent(context, VideoRecognizeActivity::class.java).apply {
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
                        if (item.modelType == ModelType.CUSTOM_OPENCV) {
                            showCustomInputViewDialog(BottomSheet(LayoutMode.MATCH_PARENT), item)
                        } else {
                            showCustomViewDialog(BottomSheet(LayoutMode.MATCH_PARENT), item)
                        }
                    }

                    override fun onDeleteItem(item: ModelEntity) {
                        launch(Dispatchers.IO) {
                            listNeuralModelsViewModel.deleteCustomModel(item)
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
        val delegateRunRequest = listNeuralModelsViewModel.getDelegateRequest()
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
}
