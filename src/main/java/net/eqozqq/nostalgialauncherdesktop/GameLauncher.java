package net.eqozqq.nostalgialauncherdesktop;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
            if (enableDebugging) {
                String[] terminals = { "x-terminal-emulator", "gnome-terminal", "konsole", "xfce4-terminal", "lxterminal", "mate-terminal" };
                String selectedTerminal = null;
                for (String terminal : terminals) {
                    if (hasCommand(terminal)) {
                        selectedTerminal = terminal;
                        break;
                    }
                }
                if (selectedTerminal != null) {
                    if (selectedTerminal.equals("gnome-terminal") || selectedTerminal.equals("mate-terminal")) {
                        processBuilder = new ProcessBuilder(selectedTerminal, "--", "bash", "-c", 
                            "\"" + executable.getAbsolutePath() + "\"; echo 'Process exited.'; read");
                    } else {
                        processBuilder = new ProcessBuilder(selectedTerminal, "-e", 
                            "bash -c '\"" + executable.getAbsolutePath() + "\"; echo \"Process exited.\"; read'");
                    }
                } else {
                    processBuilder = new ProcessBuilder(executable.getAbsolutePath());
                }
            } else {
                processBuilder = new ProcessBuilder(executable.getAbsolutePath());
            }
        }

        processBuilder.directory(gameDir);
        processBuilder.redirectErrorStream(true);

        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new Exception("error.launchGameFailed:" + e.getMessage(), e);
        }
    }

    private boolean hasCommand(String command) {
        try {
            Process p = new ProcessBuilder("which", command).start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static String readProcessOutput(Process process) {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        } catch (IOException e) {
            output.append("Failed to read process output: ").append(e.getMessage());
        }
        return output.toString();
    }
}