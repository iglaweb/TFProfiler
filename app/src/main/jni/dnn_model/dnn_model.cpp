#include "dnn_model.h"

namespace dnnmodel {

DnnModel::DnnModel(const std::string& modelName, bool useNHWC, bool cuda, int channels, int inputWidth, int inputHeight) {
    this->useNHWC = useNHWC;
    this->channelsCount = channels;
    this->isRgb = channels >= 3;
    this->IMAGE_SIZE_PAIR = cv::Size(inputWidth, inputHeight);
    this->predictor = cv::dnn::readNet(modelName); //onnx or pb?
    if(cuda) {
        predictor.setPreferableBackend(cv::dnn::DNN_BACKEND_CUDA);
        predictor.setPreferableTarget(cv::dnn::DNN_TARGET_CUDA_FP16);
    } else {
        predictor.setPreferableBackend(cv::dnn::DNN_BACKEND_OPENCV);
        predictor.setPreferableTarget(cv::dnn::DNN_TARGET_CPU);
    }
}

void DnnModel::execute(std::vector<cv::Mat>& images) {
    if(images.empty()) {
        return;
    }

    std::vector<cv::Mat> readyImages;
    for (int i = 0; i < images.size(); i++) {
        cv::Mat image = images[i];

        cv::Mat ret;
        if(this->channelsCount == 1 && image.channels() >= 3) {
            cv::cvtColor(image, ret, cv::COLOR_BGR2GRAY);
        } else {
            ret = image;
        }

        if(ret.cols != IMAGE_SIZE_PAIR.width || ret.rows != IMAGE_SIZE_PAIR.height) {
            cv::resize(ret, ret, IMAGE_SIZE_PAIR);
        }
        readyImages.push_back(ret);
    }

    //optimization
    cv::Mat blob;
    if(readyImages.size() > 1) {
        blob = cv::dnn::blobFromImages(readyImages, scaleFactor);
    } else {
        blob = cv::dnn::blobFromImage(readyImages.at(0), scaleFactor);
    }

    if(useNHWC) {
        // e.g. (1, 3, 128, 128) -> (1, 128, 128, 3)
        int imageSize = (int)readyImages.size();
        blob = blob.reshape(1, cv::dnn::MatShape({
            imageSize, IMAGE_SIZE_PAIR.width, IMAGE_SIZE_PAIR.height, this->channelsCount}));
    }

    predictor.setInput(blob);
    predictor.forward(out);

    // release
    for (int i = 0; i < out.size(); i++) {
        out[i].release();
    }

    for(cv::Mat& faceImg : readyImages) {
        faceImg.release();
    }
    blob.release();
  }
}
