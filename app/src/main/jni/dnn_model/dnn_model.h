#ifndef __DNN_MODEL
#define __DNN_MODEL
#include <iostream>
#include <cstring>
#include <chrono>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/dnn/dnn.hpp>
#include <stdio.h>
#include <opencv2/core/types_c.h>
#include <opencv2/opencv.hpp>

namespace dnnmodel {
	class DnnModel {
	private:
        bool useNHWC;
        int channelsCount;
        bool isRgb;
		std::vector<cv::Mat> out;
		cv::dnn::Net predictor;
        cv::Size IMAGE_SIZE_PAIR;
		const float scaleFactor = 1 / 256.0;
	public:
        DnnModel(const std::string& modelName, bool useNHWC, bool cuda, int channels, int inputWidth, int inputHeight);
        void execute(std::vector<cv::Mat>& images);
	};
}
#endif // __DNN_MODEL
