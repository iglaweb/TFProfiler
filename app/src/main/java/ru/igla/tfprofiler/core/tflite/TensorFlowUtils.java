package ru.igla.tfprofiler.core.tflite;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.RectF;
import android.util.Log;

import androidx.annotation.NonNull;

import org.tensorflow.lite.support.metadata.MetadataExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Vector;

import ru.igla.tfprofiler.core.ModelType;
import ru.igla.tfprofiler.core.Timber;
import ru.igla.tfprofiler.models_list.ModelEntity;
import ru.igla.tfprofiler.utils.IOUtils;


public class TensorFlowUtils {

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
    public static Vector<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException {
        Vector<String> labelList = new Vector<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(assetManager.open(labelPath)))) {
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

    public static MetadataExtractor getMetaData(Context context, String modelFilename) {
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

    public static long getModelFileSize(Context context, ModelEntity modelEntity) {
        if (modelEntity.getModelType() == ModelType.CUSTOM) {
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

    public static float box_iou(RectF a, RectF b) {
        return box_intersection(a, b) / box_union(a, b);
    }

    private static float box_intersection(RectF a, RectF b) {
        float w = overlap((a.left + a.right) / 2, a.right - a.left,
                (b.left + b.right) / 2, b.right - b.left);
        float h = overlap((a.top + a.bottom) / 2, a.bottom - a.top,
                (b.top + b.bottom) / 2, b.bottom - b.top);
        if (w < 0 || h < 0) return 0;
        float area = w * h;
        return area;
    }

    private static float box_union(RectF a, RectF b) {
        float i = box_intersection(a, b);
        float u = (a.right - a.left) * (a.bottom - a.top) + (b.right - b.left) * (b.bottom - b.top) - i;
        return u;
    }

    private static float overlap(float x1, float w1, float x2, float w2) {
        float l1 = x1 - w1 / 2;
        float l2 = x2 - w2 / 2;
        float left = l1 > l2 ? l1 : l2;
        float r1 = x1 + w1 / 2;
        float r2 = x2 + w2 / 2;
        float right = r1 < r2 ? r1 : r2;
        return right - left;
    }

    public static void softmax(final float[] vals) {
        float max = Float.NEGATIVE_INFINITY;
        for (final float val : vals) {
            max = Math.max(max, val);
        }
        float sum = 0.0f;
        for (int i = 0; i < vals.length; ++i) {
            vals[i] = (float) Math.exp(vals[i] - max);
            sum += vals[i];
        }
        for (int i = 0; i < vals.length; ++i) {
            vals[i] = vals[i] / sum;
        }
    }

    public static float expit(final float x) {
        return (float) (1. / (1. + Math.exp(-x)));
    }
}
