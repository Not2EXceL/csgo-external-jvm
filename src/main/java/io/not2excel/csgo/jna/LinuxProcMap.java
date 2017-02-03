package io.not2excel.csgo.jna;

import com.sun.jna.Pointer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
public class LinuxProcMap {

    public static final Pattern modPattern;
    static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    static {
        modPattern = Pattern.compile("^(\\S+)-(\\S+)\\s([rwxps-]{4})\\s(\\S+)\\s(\\d+):(\\d+)\\s(\\d+)\\s+(\\S*)$");
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Memory {
        Pointer start = Pointer.NULL;
        long    size  = 0;

        @Override
        public String toString() {
            return String.format("%-14sStart: %X | Size: %X",
                                 "[Memory]", Pointer.nativeValue(start), size);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Permissions {
        boolean readable   = false;
        boolean writable   = false;
        boolean executable = false;
        boolean shared     = false;

        public void parse(String perms) {
            if (perms.length() != 4) {
                return;
            }
            char[] charPerms = perms.toCharArray();
            if (charPerms[0] == 'r') {
                readable = true;
            }
            if (charPerms[1] == 'w') {
                writable = true;
            }
            if (charPerms[2] == 'x') {
                executable = true;
            }
            if (charPerms[3] == 's') {
                shared = true;
            }
        }

        @Override
        public String toString() {
            return String.format("%-14sReadable: %b | Writable: %b | Executable: %b | Shared: %b",
                                 "[Permissions]", readable, writable, executable, shared);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Device {
        int major = 0;
        int minor = 0;

        @Override
        public String toString() {
            return String.format("%-14sMajor: %X | Minor: %X",
                                 "[Device]", major, minor);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class File {
        String pathName = "";
        String fileName = "";

        @Override
        public String toString() {
            return String.format("%-14sPath: %s | File: %s",
                                 "[File]", pathName, fileName);
        }
    }

    LinuxProcMap.Memory      memory      = new LinuxProcMap.Memory();
    LinuxProcMap.Permissions permissions = new LinuxProcMap.Permissions();
    @NonFinal
    int offset = 0;
    LinuxProcMap.Device device = new LinuxProcMap.Device();
    @NonFinal
    int inode = 0;
    LinuxProcMap.File file = new LinuxProcMap.File();

    private LinuxProcMap(Matcher matcher) {
        long start = Long.parseLong(matcher.group(1), 16);
        long end = Long.parseLong(matcher.group(2), 16);
        memory.start = Pointer.createConstant(start);
        memory.size = end - start;
        permissions.parse(matcher.group(3));
        offset = Integer.parseInt(matcher.group(4), 16);
        device.major = Integer.parseInt(matcher.group(5), 16);
        device.minor = Integer.parseInt(matcher.group(6), 16);
        inode = Integer.parseInt(matcher.group(7), 16);
        file.pathName = matcher.group(8);
        file.fileName = file.pathName.contains("/") ?
                        file.pathName.substring(file.pathName.lastIndexOf("/") + 1) :
                        file.pathName;
    }

    private String offsetString() {
        return String.format("%-14sOffset: %X", "[Offset]", offset);
    }

    private String inodeString() {
        return String.format("%-14sInode: %X", "[Inode]", inode);
    }

    @Override
    public String toString() {
        return String.format("[Module]\n"
                             + "\t%s\n"
                             + "\t%s\n"
                             + "\t%s\n"
                             + "\t%s\n"
                             + "\t%s\n"
                             + "\t%s",
                             memory.toString(), permissions.toString(), offsetString(),
                             device.toString(), inodeString(), file.toString());
    }

    public static LinuxProcMap from(String map) {
        Matcher matcher = modPattern.matcher(map);
        if (matcher.matches() && matcher.groupCount() == 8) {
            BigInteger bigInt = new BigInteger(matcher.group(1), 16);
            if (bigInt.compareTo(LONG_MAX) > 0 || Integer.parseInt(matcher.group(4), 16) < 0) {
                return null;
            }
            return new LinuxProcMap(matcher);
        }
        return null;
    }
}
