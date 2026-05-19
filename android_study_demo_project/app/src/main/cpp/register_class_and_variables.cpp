//
// Created by Machenike on 2026/5/16.
//
#include "headerFile/register_class_and_variables.h"
#include "headerFile/LogUtils.h"

#define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
//各个类的全局引用
rect_block global_rect_block;
point_block global_point_block;
inner_block global_inner_block;
data_model_block global_data_model_block;

//寻找到对应的jclass，并新建一个全局引用给clazz_out
int find_class(JNIEnv *env, const char *name, jclass *clazz_out) {
    jclass clazz = env->FindClass(name);
    if (clazz == nullptr) {
        LOGE("Can't find %s", name);
        return -1;
    }
    *clazz_out = (jclass) env->NewGlobalRef(clazz); // 这里必须新建一个全局的引用
    return 0;
}

//获取到属性id
int get_field(JNIEnv *env, jclass *clazz, const char *name, const char *sig, jfieldID *field_out) {
    jfieldID filed = env->GetFieldID(*clazz, name, sig);
    if (filed == nullptr) {
        LOGE("Can't find. filed name: %s, sig: %s", name, sig);
        return -1;
    }
    *field_out = filed;
    return 0;
}

void register_rect_class(JNIEnv *env) {
    //先找到Rect class，并存在全局引用的clazz中
    int ret = find_class(env, "android/graphics/Rect", &global_rect_block.clazz);
    if (ret != 0) {
        LOGE("register_rect_class failed");
        return;
    }
    //获取rect class
    jclass clazz = global_rect_block.clazz;
    // 构造方法
    global_rect_block.constructor = env->GetMethodID(clazz, "<init>", "()V");
    // 成员
    get_field(env, &clazz, "left", "I", &global_rect_block.left);
    get_field(env, &clazz, "top", "I", &global_rect_block.top);
    get_field(env, &clazz, "right", "I", &global_rect_block.right);
    get_field(env, &clazz, "bottom", "I", &global_rect_block.bottom);
}

void register_point_class(JNIEnv *env) {
    int ret = find_class(env, "android/graphics/PointF", &global_point_block.clazz);
    if (ret != 0) {
        LOGE("register_point_class failed");
        return;
    }
    jclass clazz = global_point_block.clazz;
    // 构造方法
    global_point_block.constructor = env->GetMethodID(clazz, "<init>", "()V");
    // 成员
    get_field(env, &clazz, "x", "F", &global_point_block.x);
    get_field(env, &clazz, "y", "F", &global_point_block.y);
}

void register_inner_class(JNIEnv *env) {
    int ret = find_class(env, "com/example/android_study_demo_project/JNI/DataModel$Inner", &global_inner_block.clazz);
    if (ret != 0) {
        LOGE("register_inner_class failed");
        return;
    }
    jclass clazz = global_inner_block.clazz;
    // 构造方法
    global_inner_block.constructor = env->GetMethodID(clazz, "<init>", "()V");
    // 成员
    get_field(env, &clazz, "message", "Ljava/lang/String;", &global_inner_block.message);
}

void register_data_model_class(JNIEnv *env) {
    int ret = find_class(env, "com/example/android_study_demo_project/JNI/DataModel", &global_data_model_block.clazz);
    if (ret != 0) {
        LOGE("register_data_model_class  failed");
        return;
    }
    jclass clazz = global_data_model_block.clazz;
    // 构造方法
    global_data_model_block.constructor = env->GetMethodID(clazz, "<init>", "()V");
    // 成员
    get_field(env, &clazz, "rect", "Landroid/graphics/Rect;", &global_data_model_block.rect);
    get_field(env, &clazz, "points", "[Landroid/graphics/PointF;", &global_data_model_block.points);
    get_field(env, &clazz, "inner", "Lcom/example/android_study_demo_project/JNI/DataModel$Inner;", &global_data_model_block.inner);
    get_field(env, &clazz, "id", "I", &global_data_model_block.id);
    get_field(env, &clazz, "score", "F", &global_data_model_block.score);
    get_field(env, &clazz, "data", "[B", &global_data_model_block.data);
    get_field(env, &clazz, "doubleDimenArray", "[[I", &global_data_model_block.double_dimen_array);
}

//注册各个方法和变量，找到各自的jclass和filedid
void register_classes(JNIEnv *env) {
    register_rect_class(env);
    register_point_class(env);
    register_inner_class(env);
    register_data_model_class(env);
}

//把C++的model转为Java的model
jobject data_model_c_to_java(JNIEnv *env, jni_data_model *c_data_model) {
    if (c_data_model == nullptr) {
        LOGW("input data is null!");
        return nullptr;
    }
    LOGD("start data_model_c_to_java");

    // 1. create rect
    jobject rect = env->NewObject(global_rect_block.clazz, global_rect_block.constructor);
    env->SetIntField(rect, global_rect_block.left, c_data_model->rect.left);
    env->SetIntField(rect, global_rect_block.top, c_data_model->rect.top);
    env->SetIntField(rect, global_rect_block.right, c_data_model->rect.right);
    env->SetIntField(rect, global_rect_block.bottom, c_data_model->rect.bottom);

    // 2. point array
    jsize len = NELEM(c_data_model->points);//point 数组长度
    LOGD("point array len: %d", len);
    jobjectArray point_array = env->NewObjectArray(len, global_point_block.clazz, NULL);
    //依次创建point数组的对象
    for (int i = 0; i < len; i++) {
        jobject point = env->NewObject(global_point_block.clazz, global_point_block.constructor);
        env->SetFloatField(point, global_point_block.x, c_data_model->points[i].x);
        env->SetFloatField(point, global_point_block.y, c_data_model->points[i].y);
        env->SetObjectArrayElement(point_array, i, point);
    }

    // 3. inner class
    jobject inner = env->NewObject(global_inner_block.clazz, global_inner_block.constructor);
    jstring message = env->NewStringUTF(c_data_model->message);
    env->SetObjectField(inner, global_inner_block.message, message);

    // 4. DataModel class
    jobject java_data_model = env->NewObject(global_data_model_block.clazz, global_data_model_block.constructor);
    env->SetObjectField(java_data_model, global_data_model_block.rect, rect);
    env->SetObjectField(java_data_model, global_data_model_block.points, point_array);
    env->SetObjectField(java_data_model, global_data_model_block.inner, inner);
    env->SetIntField(java_data_model, global_data_model_block.id, c_data_model->id);
    env->SetFloatField(java_data_model, global_data_model_block.score, c_data_model->score);
    // byte array
    len = NELEM(c_data_model->data);
    LOGD("data array len: %d", len);
    jbyteArray data = env->NewByteArray(len);
    env->SetByteArrayRegion(data, 0, len, c_data_model->data);
    env->SetObjectField(java_data_model, global_data_model_block.data, data);
    // double dimen int array
    len = NELEM(c_data_model->double_dimen_array);
    LOGD("double dimen int array len: %d", len);
    jclass clazz = env->FindClass("[I"); // 一维数组的类
    jobjectArray double_dimen_array = env->NewObjectArray(len, clazz, NULL);
    for (int i = 0; i < len; i++) {
        jsize sub_len = NELEM(c_data_model->double_dimen_array[i]);
        LOGD("sub_len: %d", sub_len);
        jintArray int_array = env->NewIntArray(sub_len);
        env->SetIntArrayRegion(int_array, 0, sub_len, c_data_model->double_dimen_array[i]);
        env->SetObjectArrayElement(double_dimen_array, i, int_array);
    }
    env->SetObjectField(java_data_model, global_data_model_block.double_dimen_array, double_dimen_array);

    return java_data_model;
}

//Java的model转C++的model
void data_model_java_to_c(JNIEnv *env, jobject data_model_in, jni_data_model *data_model_out) {
    if (data_model_in == nullptr) {
        LOGW("input data is null!");
        return;
    }
    LOGD("start data_model_java_to_c");

    // 1. assign rect
    jobject rect = env->GetObjectField(data_model_in, global_data_model_block.rect);
    data_model_out->rect.left = env->GetIntField(rect, global_rect_block.left);
    data_model_out->rect.top = env->GetIntField(rect, global_rect_block.top);
    data_model_out->rect.right = env->GetIntField(rect, global_rect_block.right);
    data_model_out->rect.bottom = env->GetIntField(rect, global_rect_block.bottom);

    // 2. point array
    jobjectArray point_array = (jobjectArray) env->GetObjectField(data_model_in, global_data_model_block.points);
    jsize len = env->GetArrayLength(point_array);
    // len = NELEM(data_model_out->points);
    LOGD("point array len: %d", len); // 注意这个 len 必须等于 NELEM(data_model_out->points)
    for (int i = 0; i < len; i++) {
        jobject point = env->GetObjectArrayElement(point_array, i);
        data_model_out->points[i].x = env->GetFloatField(point, global_point_block.x);
        data_model_out->points[i].y = env->GetFloatField(point, global_point_block.y);
    }

    // 3. inner class
    jobject inner = env->GetObjectField(data_model_in, global_data_model_block.inner);
    jstring message = (jstring) env->GetObjectField(inner, global_inner_block.message);
    data_model_out->message = env->GetStringUTFChars(message, JNI_FALSE);

    // 4. other
    data_model_out->id = env->GetIntField(data_model_in, global_data_model_block.id);
    data_model_out->score = env->GetFloatField(data_model_in, global_data_model_block.score);
    // byte array
    jbyteArray byte_array = (jbyteArray) env->GetObjectField(data_model_in, global_data_model_block.data);
    jbyte *data = env->GetByteArrayElements(byte_array, JNI_FALSE);
    len = env->GetArrayLength(byte_array);
    LOGD("byte array len: %d", len);
    memcpy(data_model_out->data, data, len * sizeof(jbyte));
    env->ReleaseByteArrayElements(byte_array, data, 0);
    // double dimen int array
    jobjectArray array = (jobjectArray) env->GetObjectField(data_model_in, global_data_model_block.double_dimen_array);
    len = env->GetArrayLength(array); // 获取行数
    LOGD("double dimen int array len: %d", len);
    for (int i = 0; i < len; i++) {
        jintArray sub_array = (jintArray) env->GetObjectArrayElement(array, i); // 这步得到的就是一维数组了
        jint *int_array = env->GetIntArrayElements(sub_array, JNI_FALSE);
        jsize sub_len = env->GetArrayLength(sub_array); // 获取列数
        LOGD("sub_len: %d", sub_len);
        memcpy(data_model_out->double_dimen_array[i], int_array, sub_len * sizeof(jint));
        env->ReleaseIntArrayElements(sub_array, int_array, 0);
    }

    LOGD("end data_model_java_to_c");
}