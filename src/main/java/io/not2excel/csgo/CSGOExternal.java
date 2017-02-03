package io.not2excel.csgo;

import io.not2excel.csgo.jna.JNAUtils;
import io.not2excel.csgo.jna.LinuxProcMap;
import io.not2excel.csgo.jna.LinuxProcess;

import java.io.IOException;

public class CSGOExternal {

    public static void main(String[] args) {

        if(JNAUtils.INSTANCE.getuid() != 0) {
//            throw new RuntimeException("Run this as root.");
        }

        try {
            LinuxProcess csgo = LinuxProcess.getProcessByName("idea.sh");
            System.out.println(csgo);
            csgo.getModules().values().forEach(System.out::println);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
