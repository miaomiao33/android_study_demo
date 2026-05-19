//
// Created by Machenike on 2026/5/15.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DATAMODEL_H
#define ANDROID_STUDY_DEMO_PROJECT_DATAMODEL_H
typedef struct jni_rect_t {
    int left;
    int top;
    int right;
    int bottom;
} jni_rect;

typedef struct jni_point_t {
    float x;
    float y;
} jni_point;

typedef struct jni_data_model_t {
    jni_rect rect; // Rect
    jni_point points[4]; // PointF[]
    const char *message; // String
    int id; // int
    float score; // float
    signed char data[4]; // byte[]
    int double_dimen_array[2][2]; // int[][]
} jni_data_model;

#endif //ANDROID_STUDY_DEMO_PROJECT_DATAMODEL_H
