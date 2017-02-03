package io.not2excel.csgo.jna;

import lombok.Data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class LinuxProcess {

    public static final Pattern procPattern;

    static {
        procPattern = Pattern.compile("^(\\d+)\\s.+[\\d:]+\\s([\\S]+)$");
    }

    private int    pid;
    private String name;
    private Map<String, LinuxProcMap> modules = new HashMap<>();

    @java.beans.ConstructorProperties({"pid", "name"})
    private LinuxProcess(int pid, String name) {
        this.pid = pid;
        this.name = name;

        try {
            parseProcMaps();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseProcMaps() throws IOException {
        if (pid == -1) {
            return;
        }
        Files.lines(Paths.get(String.format("/proc/%d/maps", pid)))
             .map(LinuxProcMap::from)
             .filter(lp -> lp != null)
             .forEach(lp -> modules.put(lp.getFile().getFileName(), lp));
    }

    public static LinuxProcess from(int pid, String name) {
        return new LinuxProcess(pid, name);
    }

    public static LinuxProcess nonExistant() {
        return new LinuxProcess(-1, "");
    }

    public static LinuxProcess getProcessByName(String procName) throws IOException {
        Process procList = Runtime.getRuntime().exec(new String[]{"ps", "-e"});
        BufferedReader procListReader = new BufferedReader(new InputStreamReader(procList.getInputStream()));

        return procListReader.lines().filter(line -> line.contains(procName)).map(line -> {
            Matcher matcher = procPattern.matcher(line);
            if (matcher.matches() && matcher.groupCount() == 2) {
                int pid = Integer.parseInt(matcher.group(1));
                String name = matcher.group(2);
                return LinuxProcess.from(pid, name);
            }
            return null;
        }).filter(lp -> lp != null).findFirst().orElse(LinuxProcess.nonExistant());
    }

    @Override
    public String toString() {
        return String.format("[Process] Name: %s, PID: %d", name, pid);
    }
}
