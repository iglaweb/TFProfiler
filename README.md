# TFProfiler – ready to use 🚀 

[![Build Status][build-status-svg]][build-status-link]
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)


# Overview

TFProfiler is an app that aims to profile your [TensorFlow Lite](https://www.tensorflow.org/lite) and [OpenCV DNN][opencv] supported model right on the smartphone. 

Measure model performance with model initialization time, inference time, FPS, memory consumption, etc. 

Tweak model runs with different delegates (CPU, GPU, NNAPI, HEXAGON), XNNPACK option, and number of threads. 

Apply a batch processing, adjusting the number of input images. TFLite batch inference is [partially supported](https://github.com/tensorflow/tensorflow/issues/37706) by TensorFlow.

Run neural models (*.caffemodel, *.onnx, *.pb, etc) supported by [OpenCV DNN module][opencv].

- **Android API SDK 21+**
- **Written in [Kotlin](https://kotlinlang.org)**
- **TFLite 2.5.0 inside**
- **OpenCV 4.5.2 inside**



# Features

The app displays a list of models built inside the app that can be used to measure its performance on device. Also, you can upload your *.tflite model and see how it works on the smartphone.

<div>
  <img align="center" src="img/screenshot_config_options.jpg" alt="Configure delegate options" width="220"> &nbsp;&nbsp;&nbsp;
   <img align="center" src="img/screenshot_run_model.jpg" alt="Run model" width="220"> &nbsp;&nbsp;&nbsp;
   <img align="center" src="img/screenshot_report.jpg" alt="Report" width="220">
</div>
<br/>


## Download

Latest `apk` release can be found on release [page](https://github.com/iglaweb/TFProfiler/releases).

Or download app via Google Play:

<a href="https://play.google.com/store/apps/details?id=ru.igla.tfprofiler" target="_blank">
<img src="https://play.google.com/intl/en_gb/badges/static/images/badges/en_badge_web_generic.png" width=240 />
</a>


## Built-in models

App has built-in models that you can play with to see how inference performance is estimated.

* [MobileNetV1][tf-models]
* [MobileNetV2][tf-models]
* [YOLOV4][yolov4]
* [Blazeface][blazeface]
* [Facemesh][facemesh]
* [TextClassification][textclassification]

[tf-models]: https://www.tensorflow.org/lite/guide/hosted_models
[yolov4]: https://github.com/hunglc007/tensorflow-yolov4-tflite
[facemesh]: https://google.github.io/mediapipe/solutions/face_mesh.html
[blazeface]: https://google.github.io/mediapipe/solutions/face_detection.html
[textclassification]: https://www.tensorflow.org/lite/examples/text_classification/overview

## Image Dataset
The app has built-in a subset of image dataset [Caltech 101](http://www.vision.caltech.edu/Image_Datasets/Caltech101/). It is used for running model inteferences.

## Libraries
* [TensorFlow Lite][tf-lite]
* [Android Architecture Components][arch]
* [Timber][timber] for logging
* [Room][room] - Access your app's SQLite database with in-app objects and compile-time checks.

[opencv]: https://docs.opencv.org/4.5.2/d6/d0f/group__dnn.html
[tf-lite]: https://www.tensorflow.org/lite
[arch]: https://developer.android.com/arch
[room]: https://developer.android.com/topic/libraries/architecture/room
[timber]: https://github.com/JakeWharton/timber


# Performance tips

We can boost model performance and energy efficiency using various optimization techniques.

Model optimization aims to create smaller models that are generally faster and more energy efficient, so that they can be deployed on mobile devices. 

1. Check ops compatibility for each delegate, [CPU](https://github.com/tensorflow/tensorflow/blob/master/tensorflow/lite/tools/versioning/runtime_version.cc), [GPU](https://www.tensorflow.org/lite/performance/gpu_advanced#supported_ops), [XNNPACK](https://developer.android.com/ndk/guides/neuralnetworks#operands).
1. Use [XNNPACK](https://blog.tensorflow.org/2020/07/accelerating-tensorflow-lite-xnnpack-integration.html) to boost float-point inference.

2. Use [GPU](https://www.tensorflow.org/lite/performance/gpu) delegate to compare with CPU and other options. If some of the ops are not supported by the GPU delegate, the TF Lite will only run a part of the graph on the GPU and the remaining part on the CPU.

3. Experiment with [NNAPI](https://www.tensorflow.org/lite/performance/nnapi) (Android API 27+) delegate to see whether the models works faster on Android.

4. Try to use [quantization](https://www.tensorflow.org/lite/performance/model_optimization#quantization) to optimize model.

5. Accelerate TFLite model on Qualcomm [Hexagon DSPs](https://blog.tensorflow.org/2019/12/accelerating-tensorflow-lite-on-qualcomm.html)

6. Experiment with version of TensorFlow Lite library

7. Deploy different models for different devices


# Mentions

Featured in [AppTractor](https://apptractor.ru/info/github/tfprofiler.html)

# Credits
Launcher icon was built using [this one](https://www.flaticon.com/free-icon/neural_2103620). Icons made by [Becris](https://www.flaticon.com/authors/becris) from [https://www.flaticon.com/](www.flaticon.com)


# Issues

If you find any problems or would like to suggest a feature, please
feel free to file an [issue](https://github.com/iglaweb/TFProfiler/issues)


 [build-status-svg]: https://travis-ci.com/iglaweb/TFProfiler.svg?branch=master
 [build-status-link]: https://travis-ci.com/github/iglaweb/TFProfiler
 [license-svg]: https://img.shields.io/badge/license-APACHE-lightgrey.svg
 [license-link]: https://github.com/iglaweb/TFProfiler/blob/master/LICENSE