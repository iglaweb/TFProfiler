package ru.igla.tfprofiler.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.log10
import kotlin.math.pow

object StringUtils {

    private val units by lazy { arrayOf("B", "KB", "MB", "GB", "TB") }

    /***
     * Check string is null or empty
     * @param cs string to check
     * @return true if null or empty, otherwise false
     */
    @JvmStatic
    fun isNullOrEmpty(cs: CharSequence?): Boolean {
        return cs == null || cs.toString().trim().isEmpty()
    }

    @JvmStatic
    fun getReadableFileSize(byteSize: Long, includeUnit: Boolean): String {
        if (byteSize < 1024L) return "0" //less than 1 Kb not to show
        val digitGroups = (log10(byteSize.toDouble()) / log10(1024.0)).toInt()
        val unit = if (includeUnit) " " + units[digitGroups] else ""
        val otherSymbols = DecimalFormatSymbols(Locale.US)
        otherSymbols.decimalSeparator = '.'
        otherSymbols.groupingSeparator = '.'
        val s = byteSize / 1024.0.pow(digitGroups.toDouble())
        return DecimalFormat("#,##0.#", otherSymbols).format(s.toLong()) + unit
    }
}