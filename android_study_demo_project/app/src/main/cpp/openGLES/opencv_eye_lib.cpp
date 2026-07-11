#include <jni.h>
#include "opencv2/opencv.hpp"
#include "../headerFile/LogUtils.h"
#include "../opencv/opencv_utils.cpp"

//
// Created by Machenike on 2026/7/9.
//

using namespace cv;
using namespace std;

struct EyeDetector {
    CascadeClassifier faceCascade;
    CascadeClassifier eyeCascade;
    bool inited = false;
};
//bitmap转Mat、Mat转bitmap工具方法
extern "C"
{
extern JNIEXPORT void JNICALL Java_org_opencv_android_Utils_nBitmapToMat2
        (JNIEnv *env, jclass, jobject bitmap, jlong m_addr, jboolean needUnPremultiplyAlpha);
extern JNIEXPORT void JNICALL Java_org_opencv_android_Utils_nMatToBitmap2
        (JNIEnv *env, jclass, jlong m_addr, jobject bitmap, jboolean needPremultiplyAlpha);
}
Point2f getPupilCenter(const Mat& eyeBgr);
// 拟合瞳孔中心
Point2f getPupilCenter(const Mat& eyeBgr){
    Mat gray, blur, bin;
    cvtColor(eyeBgr, gray, COLOR_BGR2GRAY);
    GaussianBlur(gray, blur, Size(5,5), 0);
    threshold(blur, bin, 40, 255, THRESH_BINARY_INV);

    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;
    findContours(bin, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

    Point2f bestPt(-1,-1);
    double maxArea = 0;

    for (auto& cnt : contours) {
        double area = contourArea(cnt);
        if (area < 10) continue;
        if (area > maxArea) {
            maxArea = area;
            RotatedRect ell = fitEllipse(cnt);
            bestPt = ell.center;
        }
    }
    return bestPt;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_android_1study_1demo_1project_opencv_OpenGLUsage_EyeDectorManager_nativeCreateDetector(
        JNIEnv *env, jobject thiz) {
    EyeDetector* det = new EyeDetector();

    return (jlong)det;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_OpenGLUsage_EyeDectorManager_nativeLoadCascade(
        JNIEnv *env, jobject thiz, jlong ptr, jstring face_xml, jstring eye_xml) {

    EyeDetector* det = (EyeDetector*)ptr;
    const char* facePath = env->GetStringUTFChars(face_xml, nullptr);
    const char* eyePath = env->GetStringUTFChars(eye_xml, nullptr);

    bool fOk = det->faceCascade.load(facePath);
    bool eOk = det->eyeCascade.load(eyePath);
    det->inited = fOk && eOk;

    env->ReleaseStringUTFChars(face_xml, facePath);
    env->ReleaseStringUTFChars(eye_xml, eyePath);
    LOGD("load cascade face=%d eye=%d", fOk, eOk);
}
extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_example_android_1study_1demo_1project_opencv_OpenGLUsage_EyeDectorManager_nativeDetectFrame(
        JNIEnv *env, jobject thiz, jlong ptr, jbyteArray jBgr, jint w, jint h) {
    EyeDetector* det = (EyeDetector*)ptr;
    if (!det->inited) return nullptr;

    // 1. 从Java byte[]构造BGR Mat
    jbyte* buf = env->GetByteArrayElements(jBgr, nullptr);
    // 用外部内存构造Mat，不复制数据（零拷贝）

    // C++中调用带参数的构造函数生成一个Mat对象frame
    Mat frame(h, w, CV_8UC3, (uchar*)buf);

    Mat gray;
    /**灰度化*/
    cvtColor(frame, gray, COLOR_BGR2GRAY);
    equalizeHist(gray, gray);

    // 2. 检测人脸
    vector<Rect> faces;
    det->faceCascade.detectMultiScale(gray, faces, 1.1, 2, 0, Size(80,80));
    if (faces.empty()) {
        env->ReleaseByteArrayElements(jBgr, buf, JNI_ABORT);
        return nullptr;
    }

    Rect face = faces[0];
    Mat faceGray = gray(face);

    // 3. 人脸区域检测双眼
    vector<Rect> eyesLocal;
    det->eyeCascade.detectMultiScale(faceGray, eyesLocal, 1.1, 2, 0, Size(20,20));
    if (eyesLocal.size() < 2) {
        env->ReleaseByteArrayElements(jBgr, buf, JNI_ABORT);
        return nullptr;
    }

    // 转换到原图全局坐标
    vector<Rect> eyesGlobal;
    for (auto& r : eyesLocal) {
        eyesGlobal.emplace_back(r.x + face.x, r.y + face.y, r.width, r.height);
    }

    // 取左右眼，分别求瞳孔
    Point2f pup1 = getPupilCenter(frame(eyesGlobal[0]));
    Point2f pup2 = getPupilCenter(frame(eyesGlobal[1]));
    if (pup1.x < 0 || pup2.x < 0) {
        env->ReleaseByteArrayElements(jBgr, buf, JNI_ABORT);
        return nullptr;
    }

    // 区分左右眼（x坐标小的为左眼）
    float lx, ly, rx, ry;
    if (pup1.x < pup2.x) {
        lx = pup1.x + eyesGlobal[0].x;
        ly = pup1.y + eyesGlobal[0].y;
        rx = pup2.x + eyesGlobal[1].x;
        ry = pup2.y + eyesGlobal[1].y;
    } else {
        lx = pup2.x + eyesGlobal[1].x;
        ly = pup2.y + eyesGlobal[1].y;
        rx = pup1.x + eyesGlobal[0].x;
        ry = pup1.y + eyesGlobal[0].y;
    }

    // 封装返回float[4]
    jfloatArray res = env->NewFloatArray(4);
    float arr[4] = {lx, ly, rx, ry};
    env->SetFloatArrayRegion(res, 0, 4, arr);

    env->ReleaseByteArrayElements(jBgr, buf, JNI_ABORT);
    return res;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_OpenGLUsage_EyeDectorManager_nativeRelease(
        JNIEnv *env, jobject thiz, jlong ptr) {
    EyeDetector* det = (EyeDetector*)ptr;
    if (det) delete det;
}
extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_example_android_1study_1demo_1project_opencv_OpenGLUsage_EyeDectorManager_nativeDetectFrame2(
        JNIEnv *env, jobject thiz, jlong ptr, jobject bitmap, jint w, jint h) {

    EyeDetector* det = (EyeDetector*)ptr;
    if (!det->inited || bitmap == nullptr) return nullptr;

    // 1. 从Java byte[]构造BGR Mat
//    AndroidBitmapInfo info;
//    int stat = AndroidBitmap_getInfo(env, bitmap, &info);
//    if (stat != 0) return nullptr;
//    int width = info.width;
//    int height = info.height;
//    void* pixels = nullptr;
//    AndroidBitmap_lockPixels(env, bitmap, &pixels);
//
//    // 从Bitmap RGBA构造mat并转BGR
//    // C++中调用带参数的构造函数生成一个Mat对象rgba
//    Mat rgba(height, width, CV_8UC4, pixels);
//    Mat frame;
//    cvtColor(rgba, frame, COLOR_RGBA2BGR);

    void* pixels = nullptr;
    AndroidBitmap_lockPixels(env, bitmap, &pixels);
    Mat frame;
    //将传过来的源bitmap转为Mat
    Java_org_opencv_android_Utils_nBitmapToMat2(env, env->GetObjectClass(thiz),
                                                bitmap,
                                                (jlong) &frame,
                                                false);

    Mat gray;
    /**灰度化*/
    cvtColor(frame, gray, COLOR_BGR2GRAY);
    //自动拉伸图像明暗对比度，让暗部细节更清晰，提升人脸、人眼在暗光下的检测成功率
    equalizeHist(gray, gray);

    // 2. 检测人脸
    vector<Rect> faces;
    det->faceCascade.detectMultiScale(gray, faces, 1.1,
                                      2, 0, Size(80,80));
    if (faces.empty()) {
        AndroidBitmap_unlockPixels(env, bitmap);
        return nullptr;
    }
    Rect face = faces[0];
    Mat faceGray = gray(face);

    // 3. 人脸区域检测双眼
    vector<Rect> eyesLocal;
    det->eyeCascade.detectMultiScale(faceGray, eyesLocal,
                                     1.1, 2, 0, Size(20,20));
    if (eyesLocal.size() < 2) {
        AndroidBitmap_unlockPixels(env, bitmap);
        return nullptr;
    }

    // 转换到原图全局坐标
    vector<Rect> eyesGlobal;
    for (auto& r : eyesLocal) {
        eyesGlobal.emplace_back(r.x + face.x, r.y + face.y, r.width, r.height);
    }

    // 取左右眼，分别求瞳孔
    gray(eyesGlobal[0]);
    //pup1和pup2是内部相对坐标
    Point2f pup1 = getPupilCenter(frame(eyesGlobal[0]));
    Point2f pup2 = getPupilCenter(frame(eyesGlobal[1]));
    if (pup1.x < 0 || pup2.x < 0) {
        AndroidBitmap_unlockPixels(env, bitmap);
        return nullptr;
    }

    // 区分左右眼（x坐标小的为左眼）
    float lx, ly, rx, ry;
    Point2f globalPup1 = Point2f(pup1.x + (float)eyesGlobal[0].x, pup1.y + (float)eyesGlobal[0].y);
    Point2f globalPup2 = Point2f(pup2.x + (float)eyesGlobal[1].x, pup2.y + (float)eyesGlobal[1].y);

    if (globalPup1.x < globalPup2.x) {
        lx = globalPup1.x;
        ly = globalPup1.y;
        rx = globalPup2.x;
        ry = globalPup2.y;
    } else {
        lx = globalPup2.x;
        ly = globalPup2.y;
        rx = globalPup1.x;
        ry = globalPup1.y;
    }

//    if (pup1.x < pup2.x) {
//        lx = pup1.x + eyesGlobal[0].x;
//        ly = pup1.y + eyesGlobal[0].y;
//        rx = pup2.x + eyesGlobal[1].x;
//        ry = pup2.y + eyesGlobal[1].y;
//    } else {
//        lx = pup2.x + eyesGlobal[1].x;
//        ly = pup2.y + eyesGlobal[1].y;
//        rx = pup1.x + eyesGlobal[0].x;
//        ry = pup1.y + eyesGlobal[0].y;
//    }

    LOGI("_eye pup1.x : %f pup2.x: %f",pup1.x,pup2.x)
    LOGI("_eye eyesGlobal[0].x : %d eyesGlobal[0].y: %d",eyesGlobal[0].x,eyesGlobal[0].y)
    LOGI("_eye eyesGlobal[1].x : %d eyesGlobal[1].y: %d",eyesGlobal[1].x,eyesGlobal[1].y)

    // 封装返回float[4]
    jfloatArray res = env->NewFloatArray(4);
    float arr[4] = {lx, ly, rx, ry};
    env->SetFloatArrayRegion(res, 0, 4, arr);
    AndroidBitmap_unlockPixels(env, bitmap);
    return res;
}