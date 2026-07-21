#!/bin/bash
set -e # 任意命令失败直接退出，防止错误往下执行

echo "===== Start compile x264 arm64-v8a ====="
# NDK目录
NDK_ROOT=/d/android_studio_SDK/ndk/21.1.6352462
# 目标架构：aarch64 对应 arm64-v8a
# aarch64 64位三元组
ARCH=aarch64-linux-android
# 对应armeabi-v7a
# ARCH=armv7a-linux-androideabi
# 编译后安装位置 pwd表示当前目录
PREFIX=$(pwd)/android/arm64-v8a

# 校验NDK存在
if [ ! -d "$NDK_ROOT" ];then
    echo "ERROR: NDK_ROOT not found: $NDK_ROOT"
    exit 1
fi

# 目标平台版本，兼容到Android28
API=28
# 编译工具链目录
TOOLCHAIN=$NDK_ROOT/toolchains/llvm/prebuilt/windows-x86_64
# 后续cflags/ldflags精准指向架构子目录
SYSROOT=$TOOLCHAIN/sysroot 

# 没有export，就相当于脚本的临时变量
# 加了export，就变成了当前会话的环境变量
export CC="$TOOLCHAIN/bin/aarch64-linux-android$API-clang"
export CXX="$TOOLCHAIN/bin/aarch64-linux-android$API-clang++"
export AR="$TOOLCHAIN/bin/llvm-ar"
export RANLIB="$TOOLCHAIN/bin/llvm-ranlib"

# 校验编译器可用性
echo "===== Check Clang Compiler ====="
"$CC" --version || exit 1

# 创建输出目录
mkdir -p "$PREFIX"
# 清理旧编译产物
make clean 2>/dev/null || true

# 编译参数(注意删除armeabi-v7a部分参数)
EXTRA_CFLAGS="\
--gcc-toolchain=$TOOLCHAIN \
-g -DANDROID \
-fdata-sections -ffunction-sections -funwind-tables -fstack-protector-strong \
-no-canonical-prefixes -D_FORTIFY_SOURCE=2 \
-Wformat -Werror=format-security -O2 -DNDEBUG  -fPIC \
"

# 注意arm64-v8a和armeabi-v7a链接的库也不同
EXTRA_LDFLAGS="\
-L$SYSROOT/usr/lib/aarch64-linux-android/$API \
-Wl,--gc-sections -lm -ldl -llog \
-no-canonical-prefixes \
"

# prefix:指定编译结果的保存目录, 'pwd'：当前目录
./configure --prefix="$PREFIX" \
 --disable-cli \
 --enable-static \
 --enable-pic \
 --host=$ARCH \
 --sysroot=$SYSROOT \
 --extra-cflags="$EXTRA_CFLAGS" \
 --extra-ldflags="$EXTRA_LDFLAGS"

# 同时启动 4 个编译进程，也可以是8
make -j4
# 安装
make install