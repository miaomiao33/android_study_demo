//
// Created by Machenike on 2026/10/3.
//

#include "music_player.h"
#include "DZJNICall.h"
#include "DZFFmpeg.h"

DZJNICall *pJniCall;
DZFFmpeg *pFFmpeg;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_MusicPlayer_DarrenPlayer_nPlay(
        JNIEnv *env, jobject thiz, jstring url_) {
    pJniCall = new DZJNICall(NULL,env);
    const char *url = (*env).GetStringUTFChars(url_,JNI_FALSE);
    pFFmpeg = new DZFFmpeg(pJniCall,url);
    pFFmpeg->play();

    delete pJniCall;
    delete pFFmpeg;
    (*env).ReleaseStringUTFChars(url_,url);
}