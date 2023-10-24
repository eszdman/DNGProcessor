package amirz.dngprocessor.parser;

import java.nio.ByteBuffer;

public class TinyDNG {
    static void LoadLibrary(){
        System.loadLibrary("tinydngJNI");
    }
    public static native void readDNGImage(ByteBuffer dngBuffer, ByteBuffer output);
}
