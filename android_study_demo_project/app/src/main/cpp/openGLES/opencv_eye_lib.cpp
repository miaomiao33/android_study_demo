#include <jni.h>
#include "opencv2/opencv.hpp"
#include "../headerFile/LogUtils.h"
#include "../opencv/opencv_utils.cpp"

//
// Created by Machenike on 2026/7/9.
//
/***
 * 识别图片人脸并获取眼睛中心点
 */
using namespace cv;
using namespace std;

struct EyeDetector {
    CascadeClassifier faceCascade;//人脸检测器
    CascadeClassifier eyeCascade;//眼睛检测器
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

// 拟合瞳孔中心
Point2f getPupilCenter(const Mat& eyeBgr);

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_android_1study_1demo_1project_opencv_OpenGLUsage_EyeDectorManager_nativeCreateDetector(
        JNIEnv *env, jobject thiz) {
    //新建EyeDetector，并返回地址
    EyeDetector* det = new EyeDetector();

    return (jlong)det;
}

//从路径加载人脸特征模型文件（xml）和人眼检测模型文件
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_OpenGLUsage_EyeDectorManager_nativeLoadCascade(
        JNIEnv *env, jobject thiz, jlong ptr, jstring face_xml, jstring eye_xml) {

    EyeDetector* det = (EyeDetector*)ptr;
    const char* facePath = env->GetStringUTFChars(face_xml, nullptr);
    const char* eyePath = env->GetStringUTFChars(eye_xml, nullptr);

    //从 facePath 路径加载人脸特征模型文件（xml）和人眼检测模型文件
    bool fOk = det->faceCascade.load(facePath);
    bool eOk = det->eyeCascade.load(eyePath);
    //两个都成功才初始化成功
    det->inited = fOk && eOk;

    env->ReleaseStringUTFChars(face_xml, facePath);
    env->ReleaseStringUTFChars(eye_xml, eyePath);
    LOGD("load cascade face=%d eye=%d", fOk, eOk);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_OpenGLUsage_EyeDectorManager_nativeRelease(
        JNIEnv *env, jobject thiz, jlong ptr) {
    EyeDetector* det = (EyeDetector*)ptr;

    if(det) delete det;
}
extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_example_android_1study_1demo_1project_opencv_OpenGLUsage_EyeDectorManager_nativeDetectFrame(
        JNIEnv *env, jobject thiz, jlong ptr, jobject bitmap, jint w, jint h) {

    EyeDetector* det = (EyeDetector*)ptr;
    if (!det->inited || bitmap == nullptr) return nullptr;

//    // 1. 从Java byte[]构造BGR Mat
//    jbyte* buf = env->GetByteArrayElements(jBgr, nullptr);
//    // 用外部内存构造Mat，不复制数据（零拷贝）
//
//    // C++中调用带参数的构造函数生成一个Mat对象frame
//    Mat frame(h, w, CV_8UC3, (uchar*)buf);

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
    //锁住 Bitmap 的像素内存，拿到像素数据的指针
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
    /**自动拉伸图像明暗对比度*/
    //让暗部细节更清晰，提升人脸、人眼在暗光下的检测成功率
    equalizeHist(gray, gray);

    // 2. 检测人脸
    vector<Rect> faces;
    /**哈尔级联人脸检测*/
    //在灰度图上找出所有人脸，把人脸框存进 faces
    //1.1 缩放系数 scaleFactor 每次扫描图片时，把图片缩小 1.1 倍，多尺度检测大小不同的人脸。数值越小检测越精细、速度越慢
    //2 最小相邻候选框数量 minNeighbors。一个人脸区域至少要出现 2 次候选框，才判定为人脸。数值越大误检越少，但容易漏检小脸。
    //Size(80,80) 最小检测人脸尺寸 minSize。只检测宽度≥80、高度≥80 的人脸，比这个更小的区域直接忽略。
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
    //级联分类器核心检测函数，专门用来多尺度检测目标（这里是眼睛）
    det->eyeCascade.detectMultiScale(faceGray, eyesLocal,
                                     1.1, 2, 0, Size(20,20));
    if (eyesLocal.size() < 2) {
        //眼睛个数小于2
        AndroidBitmap_unlockPixels(env, bitmap);
        return nullptr;
    }

    // 转换到原图全局坐标
    vector<Rect> eyesGlobal;
    // 人脸最多2只眼睛，提前预留容量，避免vector扩容
    eyesGlobal.reserve(eyesLocal.size());
    for (auto& r : eyesLocal) {
        //直接在 vector 内存里构造 Rect，相比 push_back(Rect(...)) 少一次临时对象拷贝。
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

    //下面会有问题，因为pup1.x取的是eyesGlobal[0]区域的中间点，加起来有可能会比后面远，比如100+30 和 120+5
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

    // 封装返回float[4]
    jfloatArray res = env->NewFloatArray(4);
    float arr[4] = {lx, ly, rx, ry};
    env->SetFloatArrayRegion(res, 0, 4, arr);
    AndroidBitmap_unlockPixels(env, bitmap);
    return res;
}

// 拟合瞳孔中心
Point2f getPupilCenter(const Mat& eyeBgr){
    Mat gray, blur, bin;
    /**灰度化*/
    cvtColor(eyeBgr, gray, COLOR_BGR2GRAY);
    /**高斯模糊*/
    //图像平滑降噪，摄像头、光线会产生密密麻麻小白点、杂色噪点，这些噪点会干扰瞳孔识别、轮廓查找。
    //高斯模糊把噪点抹平，减少误检测。
    // 脸上毛孔、细纹、光影起伏会造成灰度忽亮忽暗，模糊后灰度过渡更均匀，眼睛和瞳孔的边界会更清晰。
    //blur：输出图，Size(5,5)：用 5×5 的模糊窗口处理每个像素，0：让程序自动计算模糊强度
    GaussianBlur(gray, blur, Size(5,5), 0);
    /**二值化*/
    //超过阈值返回最大值 THRESH_BINARY_INV：反向二值化模式，低于40变成255
    threshold(blur, bin, 40, 255, THRESH_BINARY_INV);

    /** 轮廓检测 */
    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;
    //RETR_EXTERNAL：只提取最外层外轮廓，忽略内部嵌套的小轮廓
    //CHAIN_APPROX_SIMPLE 压缩轮廓点：只保留拐角关键点，删掉中间重复直线点，减少计算量
    findContours(bin, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

    Point2f bestPt(-1,-1);
    double maxArea = 0;

    //循环遍历全部轮廓
    for (auto& cnt : contours) {
        //计算轮廓面积，过滤过小轮廓
        double area = contourArea(cnt);
        if (area < 10) continue;
        //更新最大轮廓，提取椭圆中心
        if (area > maxArea) {
            maxArea = area;
            //给轮廓拟合一个旋转椭圆，返回 RotatedRect（旋转矩形，包含椭圆中心、长宽、旋转角度）
            RotatedRect ell = fitEllipse(cnt);
            bestPt = ell.center;
        }
    }
    return bestPt;
}