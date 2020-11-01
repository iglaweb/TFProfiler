package ru.igla.tfprofiler.models_list

import android.app.AlertDialog
import android.app.Dialog
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.RelativeLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import io.apptik.widget.MultiSlider
import kotlinx.android.synthetic.main.dialog_configure_run_tf.*
import kotlinx.android.synthetic.main.fragment_neural_pager.*
import kotlinx.coroutines.*
import ru.igla.tfprofiler.R
import ru.igla.tfprofiler.prefs.AndroidPreferenceManager
import ru.igla.tfprofiler.ui.AboutDialog
import ru.igla.tfprofiler.ui.BaseFragment
import ru.igla.tfprofiler.utils.SystemUtils
import ru.igla.tfprofiler.utils.ViewUtils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.coroutines.CoroutineContext


class MainViewPagerFragment :
    BaseFragment(R.layout.fragment_neural_pager),
    CoroutineScope {

    private var gpuInfo: GPUInfo? = null

    private val preferenceManager by lazy { AndroidPreferenceManager(requireContext().applicationContext).defaultPrefs }

    private var deviceInfoDialog: Dialog? = null

    private val mainViewPagerViewModel: MainViewPagerViewModel by viewModels()

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main

    override val coroutineContext: CoroutineContext
        get() = Job() + uiDispatcher

    override fun onStop() {
        ViewUtils.dismissDialogSafety(deviceInfoDialog)
        super.onStop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbar()
        setAdapter()

        configureSettings()
        getGPUInformation()
    }

    private fun setAdapter() {
        viewpager.adapter = MainModelsViewPagerAdapter(this)
        TabLayoutMediator(tabLayout, viewpager) { tab, position ->
            if (position == 0) {
                tab.text = "Models"
            } else {
                tab.text = "Reports"
            }
        }.attach()
    }

    private fun getGPUInformation() {
        val glSurfaceView = GLSurfaceView(requireContext())
        glSurfaceView.setRenderer(object : GLSurfaceView.Renderer {
            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                gl?.apply {
                    val gpuCardName = gl.glGetString(GL10.GL_RENDERER)
                    val vendorName = gl.glGetString(GL10.GL_VENDOR)

                    val glEsVersion = SystemUtils.getGLESVersionFromPackageManager(requireContext())
                    val major = SystemUtils.getMajorVersionGLES(glEsVersion)
                    val minor = SystemUtils.getMinorVersionGLES(glEsVersion)
                    val glEsVersionStr = "$major.$minor"
                    gpuInfo = GPUInfo(glEsVersionStr, vendorName, gpuCardName)
                }
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                //no impl
            }

            override fun onDrawFrame(gl: GL10?) {
                //no impl
            }
        })

        RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            width = 1
            height = 1
            contentContainer.addView(glSurfaceView, this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                drawer_layout.openDrawer(GravityCompat.END)
            }
            R.id.action_info -> {
                showDeviceDialog()
            }
            R.id.action_about -> {
                AboutDialog.show(requireActivity())
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private fun setToolbar() {
        toolbar.title = getString(R.string.app_name)
        (activity as AppCompatActivity?)?.setSupportActionBar(toolbar) //show options menu
    }

    private fun showDeviceDialog() {
        launch(Dispatchers.IO) {
            val text = mainViewPagerViewModel.getDeviceTextViewHtml(gpuInfo)
            withContext(Dispatchers.Main) {
                deviceInfoDialog = AlertDialog.Builder(context).create().apply {
                    setTitle("Device information")
                    setMessage(text)
                    setButton(
                        AlertDialog.BUTTON_NEUTRAL, getString(android.R.string.ok)
                    ) { dialog, _ -> dialog.dismiss() }
                    show()
                }
            }
        }
    }

    private fun saveRunDelegatePrefs() {
        launch(Dispatchers.IO) {
            preferenceManager.apply {
                warmupRuns = etWarmupRuns.text.toString().toInt()

                cpuDelegateEnabled = sc_cpu_delegate.isChecked
                gpuDelegateEnabled = sc_gpu_delegate.isChecked
                nnapiDelegateEnabled = sc_nnapi_delegate.isChecked
                hexagonDelegateEnabled = sc_hexagon_delegate.isChecked
                xnnpackEnabled = sc_xnnpack.isChecked

                threadRangeMin = minValue.text.toString().toInt()
                threadRangeMax = maxValue.text.toString().toInt()
            }
        }
    }

    private fun configureSettings() {
        etWarmupRuns.setText(preferenceManager.warmupRuns.toString())

        sc_cpu_delegate.isChecked = preferenceManager.cpuDelegateEnabled
        sc_gpu_delegate.isChecked = preferenceManager.gpuDelegateEnabled
        sc_hexagon_delegate.isChecked = preferenceManager.hexagonDelegateEnabled
        sc_xnnpack.isChecked = preferenceManager.xnnpackEnabled

        val isNNApiEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        sc_nnapi_delegate.isChecked =
            isNNApiEnabled && preferenceManager.nnapiDelegateEnabled
        sc_nnapi_delegate.isEnabled =
            isNNApiEnabled && preferenceManager.nnapiDelegateEnabled

        minValue.text = preferenceManager.threadRangeMin.toString()
        maxValue.text = preferenceManager.threadRangeMax.toString()

        range_slider.getThumb(0).value = preferenceManager.threadRangeMin
        range_slider.getThumb(1).value = preferenceManager.threadRangeMax

        range_slider.setOnThumbValueChangeListener(object : MultiSlider.SimpleChangeListener() {
            override fun onValueChanged(
                multiSlider: MultiSlider,
                thumb: MultiSlider.Thumb,
                thumbIndex: Int,
                value: Int
            ) {
                if (thumbIndex == 0) {
                    minValue.text = value.toString()
                } else {
                    maxValue.text = value.toString()
                }
            }
        })

        //allow range bar to intercept touch events
        range_slider.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN ->  // Disallow Drawer to intercept touch events.
                    v.parent.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> // Allow Drawer to intercept touch events.
                    v.parent.requestDisallowInterceptTouchEvent(false)
            }
            // Handle seekbar touch events.
            v.onTouchEvent(event)
            true
        }

        drawer_layout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {
                onBackPressedCallback.isEnabled = true // allow to close drawer
            }

            override fun onDrawerClosed(drawerView: View) {
                onBackPressedCallback.isEnabled = false
                saveRunDelegatePrefs()
            }

            override fun onDrawerStateChanged(newState: Int) {
            }
        })
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (drawer_layout.isDrawerOpen(GravityCompat.END)) {
                    drawer_layout.closeDrawer(GravityCompat.END)
                    isEnabled = false //allow to react back
                }
            }
        }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // This callback will only be called when MyFragment is at least Started.
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }
}
