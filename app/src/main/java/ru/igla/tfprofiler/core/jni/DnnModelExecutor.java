package ru.igla.tfprofiler.core.jni;


import java.util.logging.Level;
import java.util.logging.Logger;

public class DnnModelExecutor {

    private static final Logger logger = Logger.getLogger(DnnModelExecutor.class.getName());

    private boolean initialized = false;
    private boolean initializing = false;

    public boolean init(String modelPath, boolean nhwc, boolean cuda, int channels, int inputWidth, int inputHeight) {
        if (!initializing) {
            logger.log(Level.INFO, " *** Requested a fresh initialization with jniInit() ***");
            initializing = true;
            if (jniInitModel(modelPath, nhwc, cuda, channels, inputWidth, inputHeight) == 0) { // If all went ok, state as initialized true
                logger.log(Level.INFO, " *** jniInit() OK ***");
                initializing = false;
                initialized = true;
                return true;
            } else {
                initializing = false;
                initialized = false;
                logger.log(Level.SEVERE, " *** jniInit() ERROR ***");
                return false;
            }
        } else {
            logger.log(Level.INFO, " *** Requested initialization with jniInit() while already initializing ***");
            return true;
        }
    }

    public void deInit() {
        logger.log(Level.INFO, " *** Requested deinitialization with jniDeInit() ***");
        if (jniDeInitModel() == 0) {
            logger.log(Level.INFO, " *** jniDeInit() DnnModelExecutor OK ***");
            initialized = false;
        } else {
            logger.log(Level.SEVERE, " *** jniDeInit() DnnModelExecutor ERROR ***");
        }
    }

    public void executeModel(long[] bitmap) {
        if (!initialized) {
            logger.log(Level.SEVERE, " *** DnnModelExecutor is not initialized, use jniInit() ***");
            return;
        }
        try {
            jniExecuteModel(bitmap);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Exception", e);
        }
    }

    private static native int jniDeInitModel();

    private static native int jniInitModel(String modelPath, boolean nhwc, boolean cuda, int channels, int inputWidth, int inputHeight);

    private native void jniExecuteModel(long[] bitmap);
}