# TFProfiler (work-in-progress 👷🔧️)

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)


# Overview

TFProfiler is an app that aims to profile [TensorFlow Lite](https://www.tensorflow.org/lite) model and measure its performance using FPS, inference time, memory consumption with different delegates (CPU, GPU, NNAPI, HEXAGON).

- **API SDK 21+**
- **Written in [Kotlin](https://kotlinlang.org)**


# Features

The app displays a list of models built inside the app that can be used to measure its performance on device. Also, you can upload your *.tflite model and see how it works on the smartphone.

<div>
  <img align="center" src="img/screenshot_config_options.jpg" alt="Configure options" width="220"> &nbsp;&nbsp;&nbsp;
   <img align="center" src="img/screenshot_report.jpg" alt="Report" width="220">
</div>

<br/>


# Performance tips

We can boost model performance and energy efficiency using various optimization techniques.

Model optimization aims to create smaller models that are generally faster and more energy efficient, so that they can be deployed on mobile devices. 

1. Use [XNNPACK](https://blog.tensorflow.org/2020/07/accelerating-tensorflow-lite-xnnpack-integration.html) to boost float-point inference.

2. Use [GPU](https://www.tensorflow.org/lite/performance/gpu) delegate to compare with CPU and other options. If some of the ops are not supported by the GPU delegate, the TF Lite will only run a part of the graph on the GPU and the remaining part on the CPU.

3. Experiment with [NNAPI](https://www.tensorflow.org/lite/performance/nnapi) (Android API 27+) delegate to see whether the models works faster on Android.

4. Try to use [quantization](https://www.tensorflow.org/lite/performance/model_optimization#quantization) to optimize model.

5. Accelerate TFLite model on Qualcomm [Hexagon DSPs](https://blog.tensorflow.org/2019/12/accelerating-tensorflow-lite-on-qualcomm.html)


# Credits
Launcher icon: Icons made by [Becris](https://www.flaticon.com/authors/becris) from [https://www.flaticon.com/](www.flaticon.com)


# Issues

If you find any problems or would like to suggest a feature, please
feel free to file an [issue](https://github.com/iglaweb/TFProfiler/issues)

## License

    Copyright 2020 Igor Lashkov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 [license-svg]: https://img.shields.io/badge/license-APACHE-lightgrey.svg
 [license-link]: https://github.com/iglaweb/TFProfiler/blob/master/LICENSE