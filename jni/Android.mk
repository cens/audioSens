LOCAL_PATH := $(call my-dir)
 
include $(CLEAR_VARS)
 
# Here we give our module name and source file(s)
LOCAL_MODULE    := computeFeatures
LOCAL_SRC_FILES := computeFeatures.c voice_features.c kiss_fft.c kiss_fftr.c mvnpdf.c viterbi.c
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
 
include $(BUILD_SHARED_LIBRARY)