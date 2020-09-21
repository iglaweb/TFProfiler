package ru.igla.tfprofiler.utils

import android.graphics.*
import ru.igla.tfprofiler.env.ImageUtils

class DebugDrawer {
    private val SAVE_PREVIEW_BITMAP = false

    private var canvas1: Canvas? = null
    private var cropCopyBitmap: Bitmap? = null

    fun saveBitmap(bitmap: Bitmap) {
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(bitmap, "preview.png")
        }
    }

    fun prepareOutput(croppedBitmap: Bitmap) {
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            cropCopyBitmap = Bitmap.createBitmap(croppedBitmap)
            canvas1 = Canvas(cropCopyBitmap!!)
        }
    }

    fun writeOutput() {
        // For examining the actual TF output.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(cropCopyBitmap, "preview.png")
        }
    }

    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 2.0f
    }

    fun draw(location: RectF) {
        if (SAVE_PREVIEW_BITMAP) {
            canvas1?.drawRect(location, paint)
        }
    }
}