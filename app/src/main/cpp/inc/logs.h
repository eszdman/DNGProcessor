//
// Created by eszdman on 24.10.2023.
//

#ifndef DNGPROCESSOR_LOGS_H
#define DNGPROCESSOR_LOGS_H

#include <android/log.h>

#define  LOG_TAG    "DNGProcessor Native"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#endif //DNGPROCESSOR_LOGS_H
