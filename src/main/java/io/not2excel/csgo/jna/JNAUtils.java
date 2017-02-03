package io.not2excel.csgo.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface JNAUtils extends Library {

    JNAUtils INSTANCE = (JNAUtils) Native.loadLibrary("c", JNAUtils.class);

    int getuid();
}
