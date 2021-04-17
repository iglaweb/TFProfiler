LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_ANDROID_SDK := /Users/igla/Downloads/OpenCV-android-sdk-5
ifdef OPENCV_ANDROID_SDK
  ifneq ("","$(wildcard $(OPENCV_ANDROID_SDK)/OpenCV.mk)")
    include ${OPENCV_ANDROID_SDK}/OpenCV.mk
  else
    include ${OPENCV_ANDROID_SDK}/sdk/native/jni/OpenCV.mk
  endif
else
  include ../../sdk/native/jni/OpenCV.mk
endif

LOCAL_MODULE    := dnn_cv_lib
LOCAL_SRC_FILES := jni_part.cpp
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)
