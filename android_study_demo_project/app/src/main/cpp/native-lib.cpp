#include <jni.h>
#include <string>

//C++传输String给Java
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_android_1study_1demo_1project_JNI_JNIJavaCallC_stringFromJNI(
        JNIEnv *env, jobject obj) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}