//
// Created by Machenike on 2026/5/14.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_TIMEUTILS_H
#define ANDROID_STUDY_DEMO_PROJECT_TIMEUTILS_H

#include "LogUtils.h"

#ifdef DEBUG // 在 LogUtils.h 中定义了，也可以单独使用另一个宏开控制开关
#include <ctime>
#include <chrono>
//获取开始时间
#define __TIC__(tag) auto time_##tag##_start = std::chrono::high_resolution_clock::now()
//获取结束时间，打印时间差
#define __TOC__(tag) auto time_##tag##_end = std::chrono::high_resolution_clock::now();\
        std::chrono::duration<double> time_##tag##_elapsed = std::chrono::duration_cast<std::chrono::duration<double>>(time_##tag##_end - time_##tag##_start);\
        LOGD(#tag " write time: %.3f ms", time_##tag##_elapsed.count() * 1000)
#else
#define __TIC__(tag)
#define __TOC__(tag)
#endif // DEBUG


#endif //ANDROID_STUDY_DEMO_PROJECT_TIMEUTILS_H
