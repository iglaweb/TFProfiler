package ru.igla.tfprofiler.utils;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public final class StringUtils {

    private StringUtils() {
    }

    /***
     * Check string is null or empty
     * @param cs string to check
     * @return true if null or empty, otherwise false
     */
    public static boolean isNullOrEmpty(final @Nullable CharSequence cs) {
        return (cs == null || String.valueOf(cs).trim().length() == 0);
    }

    public static boolean isNullOrEmpty(final @Nullable List<String> list) {
        return list == null || list.isEmpty();
    }

    @NotNull
    public static String getReadableFileSize(long byteSize, boolean includeUnit) {
        if (byteSize < 1024) return "0"; //less than 1 Kb not to show
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(byteSize) / Math.log10(1024));
        String unit = includeUnit ? " " + units[digitGroups] : "";

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator('.');
        return new DecimalFormat("#,##0.#", otherSymbols).format((int) (byteSize / Math.pow(1024, digitGroups))) + unit;
    }
}
