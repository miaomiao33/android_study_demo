#include <jni.h>
#include <string>
#include <android/log.h>
#include "headerFile/LogUtils.h"

//JNI打印，系统自带
#define TAG "native-lib-jni" // 这个是自定义的LOG的标识
//__VA_ARGS__：宏遍量，接收LOGD(...) 中...代表的数据
#define LOGSystemD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型，
#define LOGSystemI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGSystemW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGSystemE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGSystemF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) // 定义LOGF类型

//C++传输String给Java
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_android_1study_1demo_1project_JNI_JNIJavaCallC_stringFromJNI(
        JNIEnv *env, jobject obj) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

//改变 NDKUsageMainActivity 的name属性值
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_JNI_JNIJavaCallC_changeName(
        JNIEnv *env,jobject jniJavaCallCThis) {
    //在JNIJavaCallC中定义的changeName，所以是JNIJavaCallCThis

    //jclass FindClass(const char* name)
    jclass jniJavaCallCClass = env->FindClass(
            "com/example/android_study_demo_project/JNI/JNIJavaCallC");
//    jclass jniJavaCallCClass = env->GetObjectClass(jniJavaCallCClass);//获取jclass方法2

    //jfieldID GetFieldID(jclass clazz, const char* name, const char* sig)
    jfieldID nameFieldID = env->GetFieldID(
            jniJavaCallCClass,"name","Ljava/lang/String;");

    //void SetObjectField(jobject obj, jfieldID fieldID, jobject value)
    jstring newName = env->NewStringUTF("newName");//注意修改需要是jstring
    env->SetObjectField(jniJavaCallCThis,nameFieldID,newName);
}

//改变MainActivity的age属性值(age 为 static)
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_JNI_JNIJavaCallC_changeAge(
        JNIEnv *env, jclass jniJavaCallCClass) {
    //changeAge方法为static属性，故此处为jclass

    //jniJavaCallCClass已经为jclass可以直接使用
    jfieldID ageFieldID = env->GetStaticFieldID(
            jniJavaCallCClass,"age","I");

    //jint实际是int，可以直接按照int使用
    jint newAge  = env->GetStaticIntField(jniJavaCallCClass,ageFieldID) + 1;
    LOGSystemD("newAge:%d",newAge);
    env->SetStaticIntField(jniJavaCallCClass,ageFieldID,newAge);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_JNI_JNIJavaCallC_cCallJavaMethod(
        JNIEnv *env, jobject jniJavaCallCThis) {
    jclass cCallJavaMethodCLass = env->GetObjectClass(jniJavaCallCThis);

    //jmethodID GetMethodID(jclass clazz, const char* name, const char* sig)
    jmethodID cCallJavaMethodMID = env->GetMethodID(cCallJavaMethodCLass,
                                                    "javaMethod",
                                                    "(Ljava/lang/String;I)Ljava/lang/String;");

    //jobject     (*CallObjectMethod)(JNIEnv*, jobject, jmethodID, ...);
    jstring str = env->NewStringUTF("C++ parameters");
    jint value = 18;
    //调用Java方法，无论方法定义为final或者private都可以调用
    //注意方法要定义在native方法定义的同一个类
    jstring resultStr = (jstring)env->CallObjectMethod(jniJavaCallCThis,
                                                       cCallJavaMethodMID,
                                                       str,value);
    //C++进行显示需要把JNI的jstring转为char*才行
    char * result = (char *) env->GetStringUTFChars(resultStr, NULL);
    LOGSystemD("result from java : %s\n",result);
    //自定义的输出工具
    LOGD("result2 from java : %s\n",result);
}