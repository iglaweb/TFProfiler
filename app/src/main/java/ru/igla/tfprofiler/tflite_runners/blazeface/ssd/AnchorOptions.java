package ru.igla.tfprofiler.tflite_runners.blazeface.ssd;

class AnchorOptions {
    public int numLayers = 4;
    public float minScale = 0.1484375f;
    public float maxScale = 0.75f;
    public int inputSizeHeight = 128;
    public int inputSizeWidth = 128;
    public float anchorOffsetX = 0.5f;
    public float anchorOffsetY = 0.5f;
    public int[] strides = new int[]{8, 16, 16, 16};
    public float[] aspectRatios = new float[]{1.0f};
    public boolean fixedAnchorSize = true;

    public boolean reduceBoxesInLowestLayer = false;
    public float interpolatedScaleAspectRatio = 1.0f;
    public int[] featureMapHeight = new int[]{};
    public int[] featureMapWidth = new int[]{};
}