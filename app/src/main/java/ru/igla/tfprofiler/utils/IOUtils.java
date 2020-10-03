package ru.igla.tfprofiler.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import ru.igla.tfprofiler.TFProfilerApp;
import ru.igla.tfprofiler.media_track.MediaPathProvider;


public final class IOUtils {

    private IOUtils() {
    }

    /***
     * Represents the end-of-file (or stream).
     */
    private static final int EOF = -1;

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

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

    public static int copy(final InputStream input, final OutputStream output) throws IOException {
        final long count = copyLarge(input, output, new byte[DEFAULT_BUFFER_SIZE]);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    public static long copyLarge(final InputStream input, final OutputStream output, final byte[] buffer)
            throws IOException {
        long count = 0;
        int n = 0;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /***
     * Read stream is
     * @param is input stream
     * @return result as a string
     */
    public static String readStream(InputStream is) throws IOException {
        BufferedReader isr = null;
        int charRead;
        final int bufSize = 1024 * 8;
        char[] inputBuffer = new char[bufSize];

        StringBuilder str = new StringBuilder();
        try {
            isr = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            while ((charRead = isr.read(inputBuffer, 0, bufSize)) != EOF) {
                str.append(String.copyValueOf(
                        inputBuffer, 0, charRead));
            }
        } finally {
            closeQuietly(isr);
        }
        return str.toString();
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

    /***
     * Explicit cursor close due to issue http://stackoverflow.com/questions/13878908/sqlitedatabase-does-not-implement-interface
     * @param c cursor to close
     */
    public static void closeQuietly(@Nullable Cursor c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception ignored) {
            }
        }
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