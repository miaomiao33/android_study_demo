# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)
project("androidStudyNDKLib")

add_library(${CMAKE_PROJECT_NAME}
        SHARED
        # List C/C++ source files with relative paths to this CMakeLists.txt.
        native-lib.cpp utils_usage_lib.cpp register_class_and_variables.cpp bitmap_usage.cpp opencv/opencv_lib.cpp)
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

#链接找到的库
target_link_libraries(
        ${CMAKE_PROJECT_NAME}
        ${log-lib}
        ${jnigraphics-lib}
        ${OpenCV_LIBS}
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