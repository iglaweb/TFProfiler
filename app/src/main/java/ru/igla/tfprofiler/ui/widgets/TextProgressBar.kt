package ru.igla.tfprofiler.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.ProgressBar
import ru.igla.tfprofiler.utils.dpF

class TextProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ProgressBar(context, attrs, defStyleAttr) {

    private var progressText = "0%"

    private var textPaint = Paint().apply {
        color = Color.RED
        textSize = context.dpF(15f)
    }
    private val bounds = Rect()

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        textPaint.getTextBounds(progressText, 0, progressText.length, bounds)
        val x = width / 2f - bounds.exactCenterX()
        val y = height / 2f - bounds.exactCenterY()
        canvas.drawText(progressText, x, y, textPaint)
    }

    override fun setProgress(progress: Int) {
        super.setProgress(progress)
        progressText = "$progress%"
    }
}