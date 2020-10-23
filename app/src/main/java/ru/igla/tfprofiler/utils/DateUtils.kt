package ru.igla.tfprofiler.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by lashkov on 20/10/20.
 * Copyright (c) 2020 igla LLC. All rights reserved.
 */
object DateUtils {
    private const val FULL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

    fun getSimpleReadableDateTime(timestamp: Long): String? {
        return try {
            val sdf: DateFormat = SimpleDateFormat(FULL_DATE_FORMAT, Locale.getDefault())
            val netDate = Date(timestamp)
            sdf.format(netDate)
        } catch (ex: Exception) {
            null
        }
    }

    /**
     * Returns the current time in milliseconds.  Note that
     * while the unit of time of the return value is a millisecond,
     * the granularity of the value depends on the underlying
     * operating system and may be larger.  For example, many
     * operating systems measure time in units of tens of
     * milliseconds.
     *
     *
     *  See the description of the class `Date` for
     * a discussion of slight discrepancies that may arise between
     * "computer time" and coordinated universal time (UTC).
     *
     * @return the difference, measured in milliseconds, between
     * the current time and midnight, January 1, 1970 UTC.
     * @see java.util.Date
     */
    fun getCurrentDateInMs(): Long {
        return System.currentTimeMillis()
    }
}