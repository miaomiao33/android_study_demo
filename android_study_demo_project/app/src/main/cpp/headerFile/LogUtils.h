//
// Created by Machenike on 2026/5/14.
//

// 自定义JNI输出工具
#ifndef ANDROID_STUDY_DEMO_PROJECT_LOGUTILS_H //唯一标识符，一般是文件名称转变
#define ANDROID_STUDY_DEMO_PROJECT_LOGUTILS_H

#ifdef __ANDROID__ // Android平台
#include <android/log.h>
#else // 其它平台
#include <stdio.h>
#endif // __ANDROID__

#include <string.h>

#define DEBUG // 通过 CmakeLists.txt 等方式来定义这个宏，实现动态打开和关闭LOG

// Windows 和 Linux 这两个宏是在 CMakeLists.txt 通过 ADD_DEFINITIONS 定义
//用于获取文件名称
#ifdef Windows
#define __FILENAME__ strrchr(__FILE__, '\\') ? strrchr(__FILE__, '\\') + 1 : __FILE__  // Windows下文件目录层级是'\\'
#elif Linux
#define __FILENAME__ strrchr(__FILE__, '/') ? strrchr(__FILE__, '/') + 1 : __FILE__ // Linux下文件目录层级是'/'
#else
#define __FILENAME__ strrchr(__FILE__, '/') ? strrchr(__FILE__, '/') + 1 : __FILE__ + 1) // 默认方式
#endif

#ifdef DEBUG
#define TAG "JNI"
#define LOGV(format, ...) __android_log_print(ANDROID_LOG_VERBOSE, TAG,\
        "[%s][%s][%d]: " format, __FILENAME__, __FUNCTION__, __LINE__, ##__VA_ARGS__);
#define LOGD(format, ...) __android_log_print(ANDROID_LOG_DEBUG, TAG, \
        "[%s][%s][%d]: " format, __FILENAME__, __FUNCTION__, __LINE__, ##__VA_ARGS__);
#define LOGI(format, ...) __android_log_print(ANDROID_LOG_INFO, TAG,\
        "[%s][%s][%d]: " format, __FILENAME__, __FUNCTION__, __LINE__, ##__VA_ARGS__);
#define LOGW(format, ...) __android_log_print(ANDROID_LOG_WARN, TAG,\
        "[%s][%s][%d]: " format, __FILENAME__, __FUNCTION__, __LINE__, ##__VA_ARGS__);
#define LOGE(format, ...) __android_log_print(ANDROID_LOG_ERROR, TAG,\
        "[%s][%s][%d]: " format, __FILENAME__, __FUNCTION__, __LINE__, ##__VA_ARGS__);
#else
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE,TAG ,__VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型，
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) // 定义LOGF类型
#endif // DEBUG

#endif //ANDROID_STUDY_DEMO_PROJECT_LOGUTILS_H