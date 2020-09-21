package ru.igla.tfprofiler.core

enum class ModelType(val title: String, val id: String) {
    BLAZEFACE_MEDIAPIPE("BlazeFace MediaPipe", "blazeface"),
    MOBILENET_V1_OBJECT_DETECT("MobileNetV1 ObjectDetect", "mobilenetv1"),
    MOBILENET_V2_OBJECT_DETECT("MobileNetV2 ObjectDetect", "mobilenetv2"),
    LANDMARKS468_MEDIAPIPE("FaceMesh MediaPipe", "landmarks468"),
    YOLOV4("YOLOv4", "yolov4"),
    CUSTOM("Custom Model", "custom")
}