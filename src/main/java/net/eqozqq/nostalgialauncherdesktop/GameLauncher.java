package net.eqozqq.nostalgialauncherdesktop;

import java.io.File;
import java.io.IOException;

public class GameLauncher {
    public Process launchGame(File gameDir, String customExecutablePath, boolean enableDebugging) throws Exception {
        String osName = System.getProperty("os.name").toLowerCase();
        boolean isWindows = osName.contains("win");

        File executable;
        if (customExecutablePath != null && !customExecutablePath.isEmpty()) {
            executable = new File(customExecutablePath);
        } else {
            String executableName = isWindows ? "ninecraft.exe" : "ninecraft";
            executable = new File(gameDir, executableName);
        }
        
        if (!executable.exists()) {
            throw new Exception("error.executableNotFound:" + executable.getAbsolutePath());
        }
        
        if (!isWindows) {
            executable.setExecutable(true);
        }

        ProcessBuilder processBuilder;
        if (isWindows) {
            if (enableDebugging) {
                processBuilder = new ProcessBuilder("cmd.exe", "/c", "start", "\"" + executable.getName() + " Debug\"", executable.getAbsolutePath());
            } else {
                processBuilder = new ProcessBuilder(executable.getAbsolutePath());
            }
        } else {
            processBuilder = new ProcessBuilder(executable.getAbsolutePath());
        }

        processBuilder.directory(gameDir);
        
        if (!enableDebugging) {
            processBuilder.inheritIO();
        }

        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new Exception("error.launchGameFailed:" + e.getMessage(), e);
        }
    }
}