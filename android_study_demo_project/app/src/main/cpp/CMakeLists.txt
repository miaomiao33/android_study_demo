# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)
project("androidStudyNDKLib")

add_library(${CMAKE_PROJECT_NAME}
        SHARED
        # List C/C++ source files with relative paths to this CMakeLists.txt.
        native-lib.cpp
        utils_usage_lib.cpp
        register_class_and_variables.cpp
        bitmap_usage.cpp
        opencv/opencv_lib.cpp
        opencv/opencv_utils.cpp
)

#免去openssl的编译
#给所有 C 文件的编译命令追加宏定义 NO_CRYPTO，编译 librtmp 时开启无加密模式
#源码中所有 #ifdef NO_CRYPTO 包裹的加密相关代码（OpenSSL 加密、AES、加密握手逻辑）都会被跳过编译，不链接 OpenSSL 库
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO")
add_library( "librtmpLib"
        SHARED
        librtmp/amf.c
        librtmp/hashswf.c
        librtmp/log.c
        librtmp/parseurl.c
        librtmp/rtmp.c
        liveStreaming/livestreaming_lib.cpp
)

add_library( "eyeLib"
        SHARED
        openGLES/opencv_eye_lib.cpp
)


#引入x264和LibreDWG包
add_library( "x264Lib"
        SHARED
        x264/x264Lib.cpp
        libreDWG/libreDWG_lib.cpp
        FAAC/faac_lib.cpp
)
# 引入ffmpeg
add_library( "ffmpegLib"
        SHARED
        ffmpeg4-4-4/ffmpeg.cpp
        ffmpeg4-4-4/ffmpeg_get_frame.cpp
        ffmpeg4-4-4/ffmpeg_native_player.cpp
        ffmpeg4-4-4/FFmpegCore.cpp
        ffmpeg4-4-4/OpenSL_ES_Core.cpp
        ffmpeg4-4-4/videoDecoder/ffmpeg_video_decoder.cpp
)

# x264
# CXXFLAGS 指定库搜索路径
# 保留原来已有的所有 C++ 编译 / 链接参数 -Lxxx = 指定库搜索目录
#-L 仅作用于编译链接阶段，不会把 .so 打包进 APK
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/x264/${ANDROID_ABI}/lib")
# 指定头文件目录
include_directories(${CMAKE_SOURCE_DIR}/x264/${ANDROID_ABI}/include)

# 导入 redwg 静态库(因为.so和.a都有，默认会用.so，所以使用STATIC IMPORTED)
#（这是导入预编译库，这里的add_library不能搭配源码文件（.cpp），两种模式互斥）
# IMPORTED导入外部预编译库，必须搭配 set_target_properties 指定文件真实位置
add_library(redwg STATIC IMPORTED)
set_target_properties(redwg PROPERTIES
        IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libreDWG/${ANDROID_ABI}/lib/libredwg.a
)
# 指定头文件目录
include_directories(${CMAKE_SOURCE_DIR}/libreDWG/${ANDROID_ABI}/include)

# FAAC
add_library(faac STATIC IMPORTED)
set_target_properties(faac PROPERTIES
        IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/FAAC/${ANDROID_ABI}/lib/libfaac.a)
include_directories(${CMAKE_SOURCE_DIR}/FAAC/${ANDROID_ABI}/include)

# ffmpeg4.4.4
# FFmpeg 路径
set(FFMPEG_DIR ${CMAKE_SOURCE_DIR}/ffmpeg4-4-4/${ANDROID_ABI})
# 头文件
include_directories(${FFMPEG_DIR}/include)
# 逐个导入静态库
add_library(avutil STATIC IMPORTED)
set_target_properties(avutil PROPERTIES
        IMPORTED_LOCATION ${FFMPEG_DIR}/lib/libavutil.a)

add_library(swresample STATIC IMPORTED)
set_target_properties(swresample PROPERTIES
        IMPORTED_LOCATION ${FFMPEG_DIR}/lib/libswresample.a)

add_library(swscale STATIC IMPORTED)
set_target_properties(swscale PROPERTIES
        IMPORTED_LOCATION ${FFMPEG_DIR}/lib/libswscale.a)

add_library(avcodec STATIC IMPORTED)
set_target_properties(avcodec PROPERTIES
        IMPORTED_LOCATION ${FFMPEG_DIR}/lib/libavcodec.a)

add_library(avformat STATIC IMPORTED)
set_target_properties(avformat PROPERTIES
        IMPORTED_LOCATION ${FFMPEG_DIR}/lib/libavformat.a)

add_library(avdevice STATIC IMPORTED)
set_target_properties(avdevice PROPERTIES
        IMPORTED_LOCATION ${FFMPEG_DIR}/lib/libavdevice.a)

add_library(avfilter STATIC IMPORTED)
set_target_properties(avfilter PROPERTIES
        IMPORTED_LOCATION ${FFMPEG_DIR}/lib/libavfilter.a)

add_library(postproc STATIC IMPORTED)
set_target_properties(postproc PROPERTIES
        IMPORTED_LOCATION ${FFMPEG_DIR}/lib/libpostproc.a)

#log输出库
find_library(
        log-lib
        log )
# 在ndk中查找jnigraphics库 取别名jnigraphics-lib
# jnigraphics包含图形操作的库
find_library(
        jnigraphics-lib
        jnigraphics
)

#设置OpenCV 的路径
set(OpenCV_DIR F:/OpenCV/OpenCV-android-sdk-4.7.0/sdk/native/jni)
#寻找 包名（OpenCV）+Config.cmake文件是否存在，即 OpenCVConfig.cmake
#寻找OpenCV库，REQUIRED：没找到就报错
#找到会有两个变量：OpenCV_INCLUDE_DIRS、OpenCV_LIBS
find_package(
        OpenCV
        REQUIRED
)
LINK_DIRECTORIES(${OpenCV_INCLUDE_DIRS})

## 引入头文件路径（关键，找不到ssl.h就是这里没配）
#include_directories(openssl/include)
#
## 导入静态库
#add_library(local_openssl STATIC IMPORTED)
#set_target_properties(local_openssl PROPERTIES
#        IMPORTED_LOCATION openssl/lib/${ANDROID_ABI}/libssl.a)
#
#add_library(local_crypto STATIC IMPORTED)
#set_target_properties(local_crypto PROPERTIES
#        IMPORTED_LOCATION openssl/lib/${ANDROID_ABI}/libcrypto.a)


#链接找到的库
target_link_libraries(
        ${CMAKE_PROJECT_NAME}

        ${log-lib}
        ${jnigraphics-lib}
        ${OpenCV_LIBS}

#        ${local_openssl}
#        ${local_crypto}
)
target_link_libraries(
        "librtmpLib"
        ${log-lib}
)

target_link_libraries(
        "eyeLib"
        ${log-lib}
        ${jnigraphics-lib}
        ${OpenCV_LIBS}
)
#引入x264包
#CMake /autotools 链接时自动省略 lib 前缀与 .a 后缀
target_link_libraries(
        "x264Lib"
        ${log-lib}
        x264
        redwg
        faac
)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -ffunction-sections -fdata-sections")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -ffunction-sections -fdata-sections")
#引入ffmpeg包
target_link_libraries(
        "ffmpegLib"
        ${log-lib}
        # FFmpeg 顺序固定
        -Wl,--whole-archive
        avformat
        avfilter
        avcodec
        avdevice
        postproc
        swscale
        swresample
        avutil
        -Wl,--no-whole-archive

        # 系统依赖库，全部放在ffmpeg静态库之后
        z  # 新增zlib！必须放在ffmpeg所有库之后
        m
        atomic
        android
        OpenSLES

        camera2ndk
        mediandk
)
#添加预编译选项，为LogUtils使用
#添加两个宏：Windows，Linux
IF (${CMAKE_HOST_SYSTEM_NAME} MATCHES "Windows")
    ADD_DEFINITIONS(-DWindows)
ELSE (${CMAKE_HOST_SYSTEM_NAME} MATCHES "Linux")
    ADD_DEFINITIONS(-DLinux)
ENDIF ()

#target_link_libraries(${CMAKE_PROJECT_NAME}
#        # List libraries link to the target library
#        android
#        log)

## Creates and names a library, sets it as either STATIC
## or SHARED, and provides the relative paths to its source code.
## You can define multiple libraries, and CMake builds them for you.
## Gradle automatically packages shared libraries with your APK.
#
#add_library( # Sets the name of the library.
#        native-lib
#
#        # Sets the library as a shared library.
#        SHARED
#
#        # Provides a relative path to your source file(s).
#        src/main/cpp/native-lib.cpp )
#
## Searches for a specified prebuilt library and stores the path as a
## variable. Because CMake includes system libraries in the search path by
## default, you only need to specify the name of the public NDK library
## you want to add. CMake verifies that the library exists before
## completing its build.
#
#find_library( # Sets the name of the path variable.
#        log-lib
#
#        # Specifies the name of the NDK library that
#        # you want CMake to locate.
#        log )
#
## Specifies libraries CMake should link to your target library. You
## can link multiple libraries, such as libraries you define in this
## build script, prebuilt third-party libraries, or system libraries.
#
#target_link_libraries( # Specifies the target library.
#        native-lib
#
#        # Links the target library to the log library
#        # included in the NDK.
#        ${log-lib} )