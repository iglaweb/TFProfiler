package ru.igla.tfprofiler.core.blazeface.ssd;

class CalculatorOptions {
    public int numClasses = 1;
    public int numBoxes = 896;
    public int numCoords = 16;
    public int boxCoordOffset = 0;
    public int keypointCoordOffset = 4;
    public int numKeypoints = 6;
    public int numValuesPerKeypoint = 2;
    public boolean sigmoidScore = true;
    public float scoreClippingThresh = 100;
    public boolean reverseOutputOrder = true;
    public float xScale = 128.0f;
    public float yScale = 128.0f;
    public float hScale = 128.0f;
    public float wScale = 128.0f;
    public double minScoreThresh = 0.75f;

    public boolean applyExponentialOnBoxSize = false;
    public boolean flipVertically = false;
    public FilterMethod filterMethod = FilterMethod.HIGHEST_SCORE;
    public float nmsThreshold = 0.3f;
}