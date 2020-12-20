package ru.igla.tfprofiler.utils;


import ru.igla.tfprofiler.BuildConfig;

@SuppressWarnings("ALL")
public final class Log {
    private static final String TAG = "TFProfiler";

    private static boolean ENABLED = BuildConfig.DEBUG;
    private static final boolean LOCATION_ENABLED = BuildConfig.DEBUG;

    private Log() {
        //not called
    }

    public static String tag(String tag) {
        return TAG + "-" + tag;
    }

    public static void vt(String tag, String format, Object... args) {
        if (ENABLED) {
            try {
                android.util.Log.v(tag, String.format(format, args) + getLocation());
            } catch (Exception ignored) {
            }
        }
    }

    public static void dt(String tag, String format, Object... args) {
        if (ENABLED) {
            try {
                android.util.Log.d(tag, String.format(format, args) + getLocation());
            } catch (Exception ignored) {
            }
        }
    }

    public static void it(String tag, String format, Object... args) {
        if (ENABLED) {
            try {
                android.util.Log.i(tag, String.format(format, args) + getLocation());
            } catch (Exception ignored) {

            }
        }
    }

    public static void wt(String tag, String format, Object... args) {
        if (ENABLED) {
            try {
                android.util.Log.w(tag, String.format(format, args) + getLocation());
            } catch (Exception ignored) {
            }
        }
    }

    public static void wt(String tag, String message, Throwable e) {
        if (ENABLED) {
            try {
                android.util.Log.w(tag, message + getLocation(), e);
            } catch (Exception ignored) {
            }
        }
    }

    public static void wt(String tag, Throwable e) {
        if (ENABLED) {
            try {
                android.util.Log.w(tag, e);
            } catch (Exception ignored) {
            }
        }
    }

    public static void et(String tag, Throwable e) {
        if (ENABLED) {
            try {
                android.util.Log.e(tag, e.getMessage() + getLocation(), e);
            } catch (Exception ignored) {
            }
        }
    }

    public static void et(String tag, String format, Object... args) {
        if (ENABLED) {
            try {
                android.util.Log.e(tag, String.format(format, args) + getLocation());
            } catch (Exception ignored) {
            }
        }
    }

    public static void v(String format, Object... args) {
        vt(TAG, format, args);
    }

    public static void d(String format, Object... args) {
        dt(TAG, format, args);
    }

    public static void i(String format, Object... args) {
        it(TAG, format, args);
    }

    public static void w(String format, Object... args) {
        wt(TAG, format, args);
    }

    public static void w(String message, Throwable e) {
        wt(TAG, message, e);
    }

    public static void w(Throwable e) {
        wt(TAG, e);
    }

    public static void e(String format, Object... args) {
        et(TAG, format, args);
    }

    public static void e(Throwable e) {
        et(TAG, e);
    }

    public static void vtrace(int traceLength, String format, Object... args) {
        vtrace(TAG, traceLength, format, args);
    }

    public static void dtrace(int traceLength, String format, Object... args) {
        dtrace(TAG, traceLength, format, args);
    }

    public static void itrace(int traceLength, String format, Object... args) {
        itrace(TAG, traceLength, format, args);
    }

    public static void wtrace(int traceLength, String format, Object... args) {
        wtrace(TAG, traceLength, format, args);
    }

    public static void etrace(int traceLength, String format, Object... args) {
        etrace(TAG, traceLength, format, args);
    }

    public static void vtrace(String tag, int traceLength, String format, Object... args) {
        if (ENABLED) android.util.Log.v(tag, String.format(format, args) + getTrace(traceLength));
    }

    public static void dtrace(String tag, int traceLength, String format, Object... args) {
        if (ENABLED) android.util.Log.d(tag, String.format(format, args) + getTrace(traceLength));
    }

    public static void itrace(String tag, int traceLength, String format, Object... args) {
        if (ENABLED) android.util.Log.i(tag, String.format(format, args) + getTrace(traceLength));
    }

    public static void wtrace(String tag, int traceLength, String format, Object... args) {
        if (ENABLED) android.util.Log.w(tag, String.format(format, args) + getTrace(traceLength));
    }

    public static void etrace(String tag, int traceLength, String format, Object... args) {
        if (ENABLED) android.util.Log.e(tag, String.format(format, args) + getTrace(traceLength));
    }

    private static String getTrace(int length) {
        if (!LOCATION_ENABLED)
            return "";

        final String logClassName = Log.class.getName();
        final StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        int foundIndex = -1;

        for (int i = 0; i < traces.length; i++) {
            StackTraceElement trace = traces[i];

            if (trace.getClassName().startsWith(logClassName)) {
                foundIndex = i;
            } else {
                if (foundIndex > 0)
                    break;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (int i = foundIndex + 1; i < foundIndex + length + 1; ++i) {
            if (i > traces.length)
                break;

            StackTraceElement trace = traces[i];
            sb.append(String.format("    at %s.%s:%s\n", trace.getClassName(), trace.getMethodName(), trace.getLineNumber()));
        }
        sb.delete(sb.length() - 1, sb.length());
        return "\n" + sb.toString();
    }

    private static String getLocation() {
        if (!LOCATION_ENABLED)
            return "";

        final String logClassName = Log.class.getName();
        final StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        boolean found = false;

        for (int i = 0; i < traces.length; i++) {
            StackTraceElement trace = traces[i];

            try {
                if (found) {
                    if (!trace.getClassName().startsWith(logClassName)) {
                        Class<?> clazz = Class.forName(trace.getClassName());

                        String clazzName = clazz.getSimpleName();
                        if (StringUtils.isNullOrEmpty(clazzName))
                            clazzName = clazz.getName();

                        return String.format(" [%s.%s:%d]", clazzName, trace.getMethodName(), trace.getLineNumber());
                    }
                } else if (trace.getClassName().startsWith(logClassName)) {
                    found = true;
                }
            } catch (ClassNotFoundException e) {
            }
        }

        return " []";
    }
}