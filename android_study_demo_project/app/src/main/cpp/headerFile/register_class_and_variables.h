//
// Created by Machenike on 2026/5/15.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_REGISTER_CLASS_AND_VARIABLES_H
#define ANDROID_STUDY_DEMO_PROJECT_REGISTER_CLASS_AND_VARIABLES_H

#include <jni.h>
#include "DataModel.h"
//定义一些结构体

// 对应 java 的 android.graphics.Rect 类
typedef struct rect_block_t {
    jclass clazz;//存储找到的对应jclass
    jfieldID left;
    jfieldID top;
    jfieldID right;
    jfieldID bottom;
    jmethodID constructor;//构造方法id
} rect_block;

// 对应 java 的 android.graphics.PointF 类
typedef struct point_block_t {
    jclass clazz;
    jfieldID x;
    jfieldID y;
    jmethodID constructor;
} point_block;

// 对应 java 的 DataModel$Inner 类
typedef struct inner_block_t {
    jclass clazz;
    jfieldID message;
    jmethodID constructor;
} inner_block;

// 对应 java 的 DataModel 类
typedef struct data_model_block_t {
    jclass clazz;
    jfieldID rect;
    jfieldID points;
    jfieldID inner;

    jfieldID id;
    jfieldID score;
    jfieldID data;
    jfieldID double_dimen_array;

    jmethodID constructor;
} data_model_block;

// 注册
void register_classes(JNIEnv *env);

// C结构体转Java类
jobject data_model_c_to_java(JNIEnv *env, jni_data_model *data_model);

// Java类转C结构体
void data_model_java_to_c(JNIEnv *env, jobject data_model_in, jni_data_model *data_model_out);

#endif //ANDROID_STUDY_DEMO_PROJECT_REGISTER_CLASS_AND_VARIABLES_H
