package ru.igla.tfprofiler.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.inference_info.view.*
import ru.igla.tfprofiler.R
import ru.igla.tfprofiler.media_track.BitmapResult
import ru.igla.tfprofiler.text_track.StatOutResult
import ru.igla.tfprofiler.utils.StringUtils
import java.util.*

class InferenceInfoLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        inflateView()
    }

    private fun inflateView() {
        //set programmatically as we use merge tag
        isFocusable = true
        isFocusableInTouchMode = true
        orientation = VERTICAL
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
        )
        inflate(context, R.layout.inference_info, this)
    }

    fun setTextMode(textMode: Boolean) {
        if (textMode) {
            //hide image specific fields
            frameInfoContainer.visibility = View.GONE
            cropInfoContainer.visibility = View.GONE
        }
    }

    fun showStat(stat: StatOutResult) {
        showInference(stat.inferenceTime.toString() + " ms")
        showFps(stat.fps.toString())

        val memoryStr = StringUtils.getReadableFileSize(stat.memoryUsage, true)
        showMemoryUsage(memoryStr)
        showInitTime("" + stat.initTime + " ms")
        showMeanTime(stat.meanTime, stat.stdTime)
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

    private fun showFrameInfo(frameInfo: String) {
        frame_info.text = frameInfo
    }

    private fun showCropInfo(cropInfo: String) {
        crop_info.text = cropInfo
    }

    fun showBitmapInfo(bitmapResult: BitmapResult) {
        showFrameInfo(bitmapResult.previewWidth.toString() + "x" + bitmapResult.previewHeight)
        showCropInfo(
            bitmapResult.croppedWidth.toString() + "x" + bitmapResult.croppedHeight
        )
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
}