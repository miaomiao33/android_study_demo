//
// Created by Machenike on 2026/5/14.
//

#include <jni.h>
#include <stdio.h>
#include "headerFile/TimeUtils.h" // 包含这个头文件
#include <jni.h>
#include <string.h>
#include <iostream>
#include <dirent.h>
#include "headerFile/DataModel.h"
#include "headerFile/register_class_and_variables.h"

/***
 * 使用统计耗时工具TimeUtils
 */
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_android_1study_1demo_1project_JNI_JNIJavaCallC_cFileWriteTime(
        JNIEnv *env,jobject jniJavaCallCThis,jstring path,jstring jdata,jint len) {
    char *filePath = (char *)env->GetStringUTFChars(path,NULL);
    FILE *file = fopen(filePath, "a+");//读写模式，追加文末
    if (file == NULL) {
        LOGE("fopen %s failed.", filePath);
        return -1;
    }
    __TIC__(CWriteTimeTage); // 耗时统计起始处
    //写入的字符串
    char* data = (char*)env->GetStringUTFChars(jdata,JNI_FALSE);
    int size = fwrite(data, sizeof(data), len, file);
    __TOC__(CWriteTimeTage); // 耗时统计终止处，注意括号内的内容必须一致

    fclose(file);
    return 0;
}

/***
 * C++文件夹遍历
 */
extern "C" {

using namespace std;

//显示文件夹下的所有文件
void showAllFiles(string dir_name) {
    // check the parameter
    if (dir_name.empty()) {
        LOGE("dir_name is null !");
        return;
    }
    DIR *dir = opendir(dir_name.c_str());
    // check is dir ?
    if (dir == NULL) {
        LOGE("Can not open dir. Check path or permission!");
        return;
    }
    struct dirent *file;
    // read all the files in dir
    while ((file = readdir(dir)) != NULL) {
        // skip "." and ".."
        if (strcmp(file->d_name, ".") == 0 || strcmp(file->d_name, "..") == 0) {
            LOGV("ignore . and ..");
            continue;
        }
        if (file->d_type == DT_DIR) {
            string filePath = dir_name + "/" + file->d_name;
            showAllFiles(filePath); // 递归执行
        } else {
            // 如果需要把路径保存到集合中去，就在这里执行 add 的操作
            LOGI("filePath: %s/%s", dir_name.c_str(), file->d_name);
        }
    }
    closedir(dir);
 }
}

//C++文件夹遍历
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_JNI_JNIJavaCallC_cShowDir(
        JNIEnv *env, jobject jniJavaCallCThis,jstring dir_path) {
    const char *dirPath = env->GetStringUTFChars(dir_path, JNI_FALSE);
    showAllFiles(string(dirPath));
    env->ReleaseStringUTFChars(dir_path, dirPath);
}

//JNI 加载动态库的时会自动调用 JNI_OnLoad 方法
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    jint result = JNI_ERR;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return result;
    }
    register_classes(env); // 注册所有的类
    return JNI_VERSION_1_6;
}

/***
 * Java和C++的model相互转换
 * @param data_model
 */

void print(jni_data_model *data_model);

// 将C结构体转为Java类
extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_android_1study_1demo_1project_JNI_JNIJavaCallC_getDataFromNative(
        JNIEnv *env,jclass jniJavaClass) {
    jni_data_model data_model = {
            .rect = {0, 0, 640, 480},
            .points = {
                    {0.0, 1.0},
                    {1.0, 2.0},
                    {2.0, 3.0},
                    {3.0, 4.0}
            },
            .message = "data from C++ native",
            .id = 0,
            .score = 1.0,
            .data = {0, 1, 2, 3},
            .double_dimen_array = {
                    {0, 1},
                    {2, 3}
            }
    };
    print(&data_model);
    jobject obj = data_model_c_to_java(env, &data_model);
    return obj;
}

// 将Java类转为C结构体
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_JNI_JNIJavaCallC_transferDataToNative(
        JNIEnv *env,jclass jniJavaClass,jobject java_data_model) {
    //C++本地model
    jni_data_model data_model;
    //转换
    data_model_java_to_c(env, java_data_model, &data_model);
    //打印
    print(&data_model);
}

void print(jni_data_model *data_model) {
    LOGD("rect: [%d, %d, %d, %d]",
         data_model->rect.left, data_model->rect.top, data_model->rect.right, data_model->rect.bottom);
    LOGD("points: (%f, %f), (%f, %f), (%f, %f), (%f, %f)",
         data_model->points[0].x, data_model->points[0].y,
         data_model->points[1].x, data_model->points[1].y,
         data_model->points[2].x, data_model->points[2].y,
         data_model->points[3].x, data_model->points[3].y);
    LOGD("message: %s", data_model->message);
    LOGD("id: %d", data_model->id);
    LOGD("score: %f", data_model->score);
    for (int i = 0; i < 4; i++) {
        LOGD("data[%d]: %d", i, data_model->data[i]);
    }
    for (int i = 0; i < 2; ++i) {
        for (int j = 0; j < 2; ++j) {
            LOGD("double_dimen_array[%d][%d]: %d", i, j, data_model->double_dimen_array[i][j]);
        }
    }
}