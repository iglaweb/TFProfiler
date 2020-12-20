package ru.igla.tfprofiler.utils;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Closeable;
import java.io.File;


public final class IOUtils {

    private IOUtils() {
        //ignore
    }

    private static String combinePaths(@NonNull String... paths) {
        if (paths.length == 0) return "";
        File file = new File(paths[0]);
        for (int i = 1; i < paths.length; i++) {
            file = new File(file, paths[i]);
        }
        return file.getPath();
    }

    private static String getFilesDirAbsolutePath(Context context, String basePath) {
        return combinePaths(
                basePath,
                "/Android/data/",
                context.getPackageName(),
                "/files/"
        );
    }

    @Nullable
    private static String getExternalFileDir(Context c) {
        File file = c.getExternalFilesDir(null);
        if (file == null) {
            return null;
        }
        String path = file.getAbsolutePath(); //we can receive with missing '/' in the end of path
        if (!path.endsWith("/")) {
            return path + "/";
        }
        return path; //return file sdcard directory
    }

    public static String getAppSpecificFilesDirAbsolutePath(Context context) {
        String externalDir = getExternalFileDir(context);
        if (StringUtils.isNullOrEmpty(externalDir)) {
            return getFilesDirAbsolutePath(context, getBaseExternalStoragePath());
        }
        return externalDir;
    }

    private static String getBaseExternalStoragePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * Closes 'closeable', ignoring any checked exceptions. Does nothing if 'closeable' is null.
     */
    public static void closeQuietly(@Nullable final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
                //ignore
            }
        }
    }
}