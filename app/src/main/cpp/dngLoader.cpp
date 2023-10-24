#define TINY_DNG_LOADER_IMPLEMENTATION
#define STB_IMAGE_IMPLEMENTATION
#include <jni.h>
#include "tiny_dng_loader.h"
#include "logs.h"
extern "C"
JNIEXPORT void JNICALL
Java_amirz_dngprocessor_parser_TinyDNG_readDNGImage(JNIEnv *env, jclass clazz, jobject dng_buffer,
                                                    jobject output) {
    auto mem = env->GetDirectBufferAddress(dng_buffer);
    std::string warn, err;
    std::vector<tinydng::DNGImage> images;
    const unsigned short magic = *(reinterpret_cast<const unsigned short*>(mem));
    LOGD("Magic: %d",magic);
    // List of custom field infos. This is optional and can be empty.
    std::vector<tinydng::FieldInfo> custom_field_lists;
    bool ret = tinydng::LoadDNGFromMemory(static_cast<const char *>(mem), env->GetDirectBufferCapacity(dng_buffer), custom_field_lists, &images, &warn, &err);

    if (!warn.empty()) {
        LOGD("Warn: %s",warn.c_str());
    }

    if (!err.empty()) {
        LOGD("Err: %s",err.c_str());
    }
    int res = 0;
    int resInd = -1;
    LOGD("Reading images count: %d",int(images.size()));
    if (ret) {
        for (int i = 0; i<images.size();i++) {
            auto image = images[i];
            /*std::cout << "width = " << image.width << std::endl;
            std::cout << "height = " << image.height << std::endl;
            std::cout << "bits per pixel = " << image.bits_per_sample << std::endl;
            std::cout << "bits per pixel(original) = " << image.bits_per_sample_original << std::endl;
            std::cout << "samples per pixel = " << image.samples_per_pixel << std::endl;*/
            LOGD("width = %d",image.width);
            LOGD("height = %d",image.height);
            LOGD("bits per pixel = %d",image.bits_per_sample);
            LOGD("bits per pixel(original) = %d",image.bits_per_sample_original);
            LOGD("samples per pixel = %d",image.samples_per_pixel);

            //Select biggest image
            if(image.width > res){
                res = image.width;
                resInd = i;
            }
        }
    }

    if(resInd != -1){
        auto image = images[resInd];
        auto outputBufferPtr = env->GetDirectBufferAddress(output);
        for(int i =0; i<image.data.size();i++){
            reinterpret_cast<unsigned char*>(outputBufferPtr)[i] = image.data[i];
        }
    }

    for (auto image : images) {
        image.data.clear();
        image.strip_byte_counts.clear();
        image.strip_offsets.clear();
        image.custom_fields.clear();
        image.opcodelist1_gainmap.clear();
        image.opcodelist2_gainmap.clear();
        image.opcodelist3_gainmap.clear();
    }
}