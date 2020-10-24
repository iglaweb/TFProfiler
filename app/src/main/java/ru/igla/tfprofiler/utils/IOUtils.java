package ru.igla.tfprofiler.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;

import ru.igla.tfprofiler.TFProfilerApp;
import ru.igla.tfprofiler.media_track.MediaPathProvider;


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

    public static boolean isPathCorrectAndWritable(String path) {
        if (StringUtils.isNullOrEmpty(path)) {
            return false;
        }
        File f = new File(path);
        return f.exists() && f.canWrite();
    }

    private static boolean checkFileExistsInternalStorage(Context ctx, String filename) {
        File file = ctx.getFileStreamPath(filename);
        return file.exists();
    }

    public static boolean writeBitmapExternalStorage(String filename, Bitmap bmp) {
        FileOutputStream outputStream = null;
        File file = new File(MediaPathProvider.INSTANCE.getRootPath(TFProfilerApp.Companion.getInstance()), filename);
        try {
            outputStream = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
        } catch (Exception e) {
            Log.w(e.getMessage());
            return false;
        } finally {
            closeQuietly(outputStream);
        }
        return true;
    }

    /**
     * Closes 'closeable', ignoring any checked exceptions. Does nothing if 'closeable' is null.
     */
    public static void closeQuietly(@Nullable final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }
}