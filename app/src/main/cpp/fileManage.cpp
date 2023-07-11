
#include <jni.h>
#include <cstdlib>
#include <exception>
#include <cstring>
#include "android/log.h"
#include "unistd.h"
#include <cerrno>
#include <sys/stat.h>

#define BCDEC_IMPLEMENTATION 1
#include "bcdec.h"

#define TAG "CPP"
#define LOG_W(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__) // 定义LOGW类型

jbyte* charToJByteArray(char *buff, int len);

char* jByteArrayToChar(JNIEnv *pEnv, jbyteArray buff);

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_cc_fileManage_utils_TexFileUtil_unpackBC7(JNIEnv *env, jclass clazz, jint width,
                                                   jint height, jbyteArray data) {
    char *src, * dst;
    //
    try {
        // 源byte数组转char数组
        char* compData = jByteArrayToChar(env, data);
        src = compData;
        /// 解压的数据
        int dataLen = width * height * 4;
        char* unCompData = (char*) malloc(dataLen);
        // dst = unCompData;
        ///
        for (int i = 0; i < height; i += 4) {
            for (int j = 0; j < width; j += 4) {
                dst = unCompData + (i * width + j) * 4;
                bcdec_bc7(src, dst, width * 4);
                src += BCDEC_BC7_BLOCK_SIZE;
            }
        }
        //
        jbyteArray array = env->NewByteArray(dataLen);
        jbyte *by = charToJByteArray(unCompData, dataLen);
        env->SetByteArrayRegion(array, 0, dataLen, by);
        ////
        free(compData);
        free(unCompData);
        //
        return array;
    } catch (std::exception& e) {
        LOG_W("异常");
    }
    ///
    return nullptr;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cc_fileManage_utils_TexFileUtil_fileExists(JNIEnv *env, jclass clazz, jstring file) {
    jboolean boo = false;
    int exist = -1;
    const char* path = env->GetStringUTFChars(file, &boo);
    if(path != nullptr) {
        exist = access(path, 0);
        env->ReleaseStringUTFChars(file, path);
    }
    return exist;
}

jbyte* charToJByteArray(char *buff, int len) {
    //
    auto* bytes = new jbyte[len] ;
    //
    memset(bytes, 0, len);
    memcpy(bytes, buff, len);
    return bytes;
}

char* jByteArrayToChar(JNIEnv *env, jbyteArray buff) {
    //
    jboolean boo = false;
    jbyte *bytes = env->GetByteArrayElements(buff, &boo);
    //
    int char_len = env->GetArrayLength(buff);
    char* chars = new char[char_len + 1];
    //
    memset(chars, 0, char_len + 1);
    memcpy(chars, bytes, char_len);
    chars[char_len] = 0;
    env->ReleaseByteArrayElements(buff, bytes, 0);
    return chars;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cc_fileManage_utils_TexFileUtil_read(JNIEnv *env, jclass clazz, jint fd) {
    if(fd != -1) {
        int readL, length = 0;
        char buff[1];

        // 这是一个存储文件(夹)信息的结构体，其中有文件大小和创建时间、访问时间、修改时间等
        struct stat statBuf{0};
        fstat(fd, &statBuf);
        LOG_W("文件长度: %lld", (long long )statBuf.st_size);
        //
        //
        size_t buf = sizeof(char) * statBuf.st_size + 1;
        LOG_W("buf: %zu", buf);
        
        char* text = (char*) malloc(buf);
        /////
        while ((readL = read(fd, buff, 1)) > 0) {
            *(text + length) = *buff;
            length += readL;
        }
        *(text + length) = '\0';
        //
        LOG_W("%s", text);
        ///
        char buff3[4];
        lseek(fd, 0, SEEK_SET);
        if(read(fd, buff3, 3) > 0) {
            buff3[3] = '\0';
            LOG_W("文件第一个字符: %s", buff3);
        }
        free(text);
    }
}