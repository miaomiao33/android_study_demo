#!/bin/bash
set -e # 任意命令失败直接退出，防止错误往下执行

echo "===== Start compile x264 armeabi-v7a ====="
# NDK目录
NDK_ROOT=/d/android_studio_SDK/ndk/21.1.6352462
# 目标架构：aarch64 对应 arm64-v8a
# ARCH=aarch64-linux-android
ARCH=armv7a-linux-androideabi
# 编译后安装位置 pwd表示当前目录
PREFIX=$(pwd)/android/armeabi-v7a

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
export CC="$TOOLCHAIN/bin/armv7a-linux-androideabi$API-clang"
export CXX="$TOOLCHAIN/bin/armv7a-linux-androideabi$API-clang++"

# 校验编译器可用性
echo "===== Check Clang Compiler ====="
"$CC" --version || exit 1

# 创建输出目录
mkdir -p "$PREFIX"
# 清理旧编译产物
make clean 2>/dev/null || true

# 编译参数
EXTRA_CFLAGS="\
--gcc-toolchain=$TOOLCHAIN \
-g -DANDROID \
-fdata-sections -ffunction-sections -funwind-tables -fstack-protector-strong \
-no-canonical-prefixes -D_FORTIFY_SOURCE=2 \
-march=armv7-a -mthumb -mfpu=neon -mfloat-abi=softfp \
-Wformat -Werror=format-security -Oz -DNDEBUG -fPIC \
"

EXTRA_LDFLAGS="\
-L$SYSROOT/usr/lib/arm-linux-androideabi/$API \
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

# 同时启动 4 个编译进程
make -j4
# 安装
make install