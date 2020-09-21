package ru.igla.tfprofiler.utils;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Created by lashkov on 13/02/16.
 * Copyright (c) 2016 igla LLC. All rights reserved.
 */
public final class DateUtils {

    private static final String FULL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private DateUtils() {
        //no impl
    }

    public static String getSimpleReadableDateTime(long timestamp) {
        try {
            DateFormat sdf = new SimpleDateFormat(FULL_DATE_FORMAT, Locale.getDefault());
            Date netDate = new Date(timestamp);
            return sdf.format(netDate);
        } catch (Exception ex) {
            return null;
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
     * <p> See the description of the class <code>Date</code> for
     * a discussion of slight discrepancies that may arise between
     * "computer time" and coordinated universal time (UTC).
     *
     * @return the difference, measured in milliseconds, between
     * the current time and midnight, January 1, 1970 UTC.
     * @see java.util.Date
     */
    public static long getCurrentDateInMs() {
        return System.currentTimeMillis();
    }
}
