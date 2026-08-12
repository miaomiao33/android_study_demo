#include <jni.h>
#include "../headerFile/LogUtils.h"
#include "OpenSL_ES_Core.h"

//
// Created by Machenike on 2026/8/5.
//

extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_FFmpegCoderNativePlayer_play(
        JNIEnv *env, jclass clazz, jstring url_j) {
    char *url= const_cast<char *>(env->GetStringUTFChars(url_j, JNI_FALSE));
    LOGD("start play audio ... url=%s",url);
    play(url);
    env->ReleaseStringUTFChars(url_j,url);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_FFmpegCoderNativePlayer_stop(
        JNIEnv *env, jclass clazz) {
    LOGD("Stop");
    stop();
}