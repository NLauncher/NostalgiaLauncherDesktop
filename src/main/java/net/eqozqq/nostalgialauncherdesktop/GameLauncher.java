package net.eqozqq.nostalgialauncherdesktop;

import java.io.File;
import java.io.IOException;

public class GameLauncher {
    public Process launchGame(File gameDir, String customExecutablePath, boolean enableDebugging) throws Exception {
        File executable;
        if (customExecutablePath != null && !customExecutablePath.isEmpty()) {
            executable = new File(customExecutablePath);
        } else {
            executable = new File(gameDir, "ninecraft.exe");
        }
        
        if (!executable.exists()) {
            throw new Exception("Game executable not found: " + executable.getAbsolutePath());
        }
        
        ProcessBuilder processBuilder;
        if (enableDebugging) {
            processBuilder = new ProcessBuilder("cmd.exe", "/c", "start", "\"" + executable.getName() + " Debug\"", executable.getAbsolutePath());
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
            throw new Exception("Failed to launch game: " + e.getMessage(), e);
        }
    }
}
