//
// Created by Machenike on 2026/10/15.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZLIVEPUSH_H
#define ANDROID_STUDY_DEMO_PROJECT_DZLIVEPUSH_H

#include "DZJNICall5.h"

class DZLivePush {
public:
    DZJNICall5 *pJniCall;
    char *liveUrl;

public:
    DZLivePush(const char *liveUrl, DZJNICall5 *pJniCall);
    ~DZLivePush();

};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZLIVEPUSH_H
