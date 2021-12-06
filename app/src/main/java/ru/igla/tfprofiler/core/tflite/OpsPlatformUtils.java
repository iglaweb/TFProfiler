package ru.igla.tfprofiler.core.tflite;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import timber.log.Timber;


public final class OpsPlatformUtils {

    private OpsPlatformUtils() {
    }

    /***
     * https://www.tensorflow.org/lite/performance/nnapi#create_a_device_exclusion_list
     * In production, there may be cases where NNAPI does not perform as expected.
     * We recommend developers maintain a list of devices that should not use NNAPI acceleration in combination with particular models.
     * @return board platform
     */
    public static String getBoardPlatform() {
        return getProp("ro.board.platform");
    }

    public static String getProductDevice() {
        return getProp("ro.product.device");
    }

    public static String getProp(String propName) {
        String boardPlatform = "";
        Process sysProcess = null;
        try {
            sysProcess =
                    new ProcessBuilder("/system/bin/getprop", propName).
                            redirectErrorStream(true).start();
            try (BufferedReader reader = new BufferedReader
                    (new InputStreamReader(sysProcess.getInputStream()))) {
                String currentLine;
                while ((currentLine = reader.readLine()) != null) {
                    boardPlatform = currentLine;
                }
            }
        } catch (IOException e) {
            Timber.e(e);
        } finally {
            if (sysProcess != null) {
                sysProcess.destroy();
            }
        }
        Log.d("Board Platform", boardPlatform);
        return boardPlatform;
    }
}
