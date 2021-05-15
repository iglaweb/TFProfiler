#include <jni.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/features2d.hpp>
#include <vector>
#include "LOG.h"


#include "dnn_model/dnn_model.cpp"

using namespace std;
using namespace cv;


namespace {
  std::shared_ptr<dnnmodel::DnnModel> gDnnModelPtr;
}

extern "C" {

#define DLIB_JNI_DNNMODEL_METHOD(METHOD_NAME) \
  Java_ru_igla_tfprofiler_core_jni_DnnModelExecutor_##METHOD_NAME

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
  JNIEnv* env = NULL;
  if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
    LOG(T_FATAL) << "JNI_OnLoad ERROR";
    return JNI_ERR;
  }
  LOG(T_INFO) << "JNI_OnLoad OK";
  return JNI_VERSION_1_6;
}

static void throwJavaException(JNIEnv *env, const std::exception *e, const char *method) {
  std::string what = "unknown exception";
  jclass je = 0;

  if(e) {
    std::string exception_type = "std::exception";

    if(dynamic_cast<const cv::Exception*>(e)) {
      exception_type = "native::Exception";
      je = env->FindClass("java/lang/Exception");
    }

    what = exception_type + ": " + e->what();
  }

  if(!je) je = env->FindClass("java/lang/Exception");
  env->ThrowNew(je, what.c_str());

  (void)method;        // avoid "unused" warning
}

void rethrow_cpp_exception_as_java_exception(JNIEnv* env)
{
  try
  {
    throw; // This allows to determine the type of the exception
  }
  catch (const std::bad_alloc& e) {
    jclass jc = env->FindClass("java/lang/OutOfMemoryError");
    if(jc) env->ThrowNew (jc, e.what());
  }
  catch (const std::ios_base::failure& e) {
    jclass jc = env->FindClass("java/io/IOException");
    if(jc) env->ThrowNew (jc, e.what());
  }
  catch (const std::exception& e) {
    /* unknown exception (may derive from std::exception) */
    jclass jc = env->FindClass("java/lang/Error");
    if(jc) env->ThrowNew (jc, e.what());
  }
  catch (...) {
    /* Oops I missed identifying this exception! */
    jclass jc = env->FindClass("java/lang/Error");
    if(jc) env->ThrowNew (jc, "Unidentified exception => "
      "Improve rethrow_cpp_exception_as_java_exception()" );
  }
}

jint JNIEXPORT JNICALL DLIB_JNI_DNNMODEL_METHOD(jniDeInitModel)(JNIEnv* env, jobject thiz) {
  if(!gDnnModelPtr) {
    LOG(T_INFO) << "DnnModel already released";
    return JNI_ERR;
  }
  gDnnModelPtr.reset();
  gDnnModelPtr = nullptr;
  return JNI_OK;
}

jint JNIEXPORT JNICALL DLIB_JNI_DNNMODEL_METHOD(jniInitModel)(JNIEnv* env, jobject thiz,
            jstring m_modelPath, jboolean nhwc, jboolean cuda, jint channels, jint inputWidth, jint inputHeight) {
  if (!gDnnModelPtr) {
    const char* model_configuration_path = env->GetStringUTFChars(m_modelPath, 0);
    LOG(T_INFO) << "Initializing new DnnModel, configuration path "<< model_configuration_path;
    try {
        gDnnModelPtr = std::make_shared<dnnmodel::DnnModel>(model_configuration_path, nhwc, cuda, channels, inputWidth, inputHeight);
    } catch(const std::exception &e) {
      static const char method_name[] = "jniInitModel";
      throwJavaException(env, &e, method_name);
    } catch(...) {
      rethrow_cpp_exception_as_java_exception(env);
    }
    env->ReleaseStringUTFChars(m_modelPath, model_configuration_path);
  }
  LOG(T_INFO) << "Classes and method references init DnnModel OK";
  return JNI_OK;
}

cv::Mat readJavaMat(long addrInputImage) {
    cv::Mat bgrMat;
    cv::Mat* pInputImage = (cv::Mat*) addrInputImage;
    bgrMat = *pInputImage;

    if(bgrMat.channels() == 4) { //rgba? convert
      cv::Mat temp;
      cv::cvtColor(bgrMat, temp, cv::COLOR_RGBA2BGR);
      bgrMat.release();
      bgrMat = temp;
    }
    return bgrMat;
}

void JNIEXPORT JNICALL DLIB_JNI_DNNMODEL_METHOD(jniExecuteModel)(JNIEnv* env, jobject thiz,
            jlongArray matArray) {
    try {
      if (!gDnnModelPtr) {
        throw std::runtime_error("Dnn Model detector is not created!");
      }

      // construct image list
      std::vector<cv::Mat> mat_rects;
      jlong* lng_arr = env->GetLongArrayElements(matArray, 0);
      int len_mat = env->GetArrayLength(matArray);
      if(len_mat == 0) {
        throw std::runtime_error("Empty image array");
      }
      for(int i = 0; i < len_mat; i++) {
         auto addrInputImage = lng_arr[i];
         cv::Mat bgrMat = readJavaMat(addrInputImage);
         mat_rects.push_back(bgrMat);
      }
      env->ReleaseLongArrayElements(matArray, lng_arr, 0);

      //detection
      gDnnModelPtr->execute(mat_rects);

      for(auto img : mat_rects) {
        img.release();
      }
      mat_rects.clear();
    }
    catch(const std::exception &e) {
      static const char method_name[] = "jniExecuteModel";
      throwJavaException(env, &e, method_name);
    }
    catch(...) {
        rethrow_cpp_exception_as_java_exception(env);
    }
}
}
