package ru.igla.tfprofiler.core.tflite;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.tensorflow.lite.support.metadata.MetadataExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import ru.igla.tfprofiler.models_list.domain.ModelEntity;
import ru.igla.tfprofiler.utils.IOUtils;
import timber.log.Timber;


public final class TensorFlowUtils {

    private TensorFlowUtils() {
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
        try {
            Process sysProcess =
                    new ProcessBuilder("/system/bin/getprop", propName).
                            redirectErrorStream(true).start();
            try (BufferedReader reader = new BufferedReader
                    (new InputStreamReader(sysProcess.getInputStream()))) {
                String currentLine;
                while ((currentLine = reader.readLine()) != null) {
                    boardPlatform = currentLine;
                }
            }
            sysProcess.destroy();
        } catch (IOException e) {
            Timber.e(e);
        }
        Log.d("Board Platform", boardPlatform);
        return boardPlatform;
    }

    @NonNull
    public static List<String> loadLabelList(@NonNull AssetManager assetManager, String labelPath) throws IOException {
        try (InputStream is = assetManager.open(labelPath)) {
            return loadLabelList(is);
        }
    }

    @NonNull
    public static List<String> loadLabelList(@NonNull InputStream inputStream) throws IOException {
        List<String> labelList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Timber.w(line);
                labelList.add(line);
            }
        }
        return labelList;
    }

    /**
     * Memory-map the model file in Assets.
     * Preload and memory map the model file, returning a MappedByteBuffer containing the model.
     */
    public static MappedByteBuffer loadModelFileFromAssets(Context context, String modelFilename)
            throws IOException {
        AssetManager assetManager = context.getAssets();
        try (AssetFileDescriptor fileDescriptor = assetManager.openFd(modelFilename)) {
            try (FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {
                FileChannel fileChannel = inputStream.getChannel();
                long startOffset = fileDescriptor.getStartOffset();
                long declaredLength = fileDescriptor.getDeclaredLength();
                return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
            }
        }
    }

    public static MappedByteBuffer loadModelFileFromExternal(String modelFilename)
            throws IOException {
        File file = new File(modelFilename);
        try (FileInputStream inputStream = new FileInputStream(file)) {
            FileChannel fileChannel = inputStream.getChannel();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
        }
    }

    public static boolean isAssetFileExists(Context context, String filename) {
        AssetManager mg = context.getResources().getAssets();
        InputStream is = null;
        try {
            is = mg.open(filename);
            return true;
        } catch (IOException ex) {
            //ignore
            return false;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    @Nullable
    public static MetadataExtractor getMetaData(@NonNull Context context, @NonNull String modelFilename) {
        try {
            MappedByteBuffer m = loadModelFileFromAssets(context, modelFilename);
            MetadataExtractor metadataExtractor;
            metadataExtractor = new MetadataExtractor(m);
            if (!metadataExtractor.hasMetadata()) {
                return null;
            }
            return metadataExtractor;
        } catch (IOException e) {
            Timber.e(e);
        }
        return null;
    }

    public static long getModelFileSize(@NonNull Context context, @NonNull ModelEntity modelEntity) {
        if (modelEntity.getModelType().isCustomModel()) {
            return new File(modelEntity.getModelFile()).length();
        }
        try {
            String modelFilename = modelEntity.getModelFile();
            try (AssetFileDescriptor fd = context.getAssets().openFd(modelFilename)) {
                return fd.getLength();
            }
        } catch (IOException e) {
            Timber.e(e);
            return AssetFileDescriptor.UNKNOWN_LENGTH;
        }
    }

    public static float sigmoid(final float x) {
        return (float) (1. / (1. + Math.exp(-x)));
    }
}
