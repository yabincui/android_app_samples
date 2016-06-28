#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_example_yabinc_sudogame_MainActivity_getMsgFromJni(JNIEnv *env, jobject instance) {

   // TODO

   return (*env)->NewStringUTF(env, "Hello From JNI!");
}