package net.eqozqq.nostalgialauncherdesktop;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class NinecraftCompiler {

    private static final String DEPS_AMD64 = "git make cmake gcc g++ gcc-multilib g++-multilib libopenal-dev:i386 libx11-dev:i386 libxrandr-dev:i386 libxinerama-dev:i386 libxcursor-dev:i386 libxi-dev:i386 libgl-dev:i386 zenity unzip python3-jinja2";
    private static final String DEPS_ARM64 = "git make cmake gcc-arm-linux-gnueabihf g++-arm-linux-gnueabihf libopenal-dev:armhf libx11-dev:armhf libxrandr-dev:armhf libxinerama-dev:armhf libxcursor-dev:armhf libxi-dev:armhf libgl-dev:armhf zenity unzip python3-jinja2";

    public static boolean compile(File gameDir, NinecraftCompilationDialog dialog, LocaleManager localeManager, String repoUrl) {
        File sourceDir = new File(gameDir, "Ninecraft_source");
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("win");
        String arch = System.getProperty("os.arch").toLowerCase();
        boolean isArm = arch.contains("arm") || arch.contains("aarch64");
        
        try {
            dialog.setStatus(localeManager.get("compiler.status.checkingDeps"));
            List<String> missing = isWindows ? checkWindowsDependencies() : checkLinuxDependencies(isArm);
            
            if (!missing.isEmpty()) {
                dialog.appendLog(localeManager.get("compiler.error.missingSpecificDeps") + " " + String.join(", ", missing));
                dialog.appendLog(localeManager.get("compiler.log.installPrompt"));
                dialog.setStatus(localeManager.get("compiler.status.missingDepsTitle"));
                
                dialog.showInstallButton(() -> installDependencies(isWindows, isArm, dialog, localeManager));
                return false;
            }

            if (sourceDir.exists()) {
                dialog.setStatus(localeManager.get("compiler.status.updatingRepo"));
                
                dialog.appendLog("Setting remote origin to: " + repoUrl);
                runCommand(sourceDir, dialog, localeManager, "git", "remote", "set-url", "origin", repoUrl);
                
                if (!runCommand(sourceDir, dialog, localeManager, "git", "pull")) return false;
                if (!runCommand(sourceDir, dialog, localeManager, "git", "submodule", "update", "--init", "--recursive")) return false;
            } else {
                dialog.setStatus(localeManager.get("compiler.status.cloningRepo"));
                if (!runCommand(gameDir, dialog, localeManager, "git", "clone", "--recursive", repoUrl, "Ninecraft_source")) return false;
            }

            dialog.setStatus(localeManager.get("compiler.status.compiling"));
            dialog.appendLog(localeManager.get("compiler.log.archDetected") + " " + arch);

            if (isWindows) {
                boolean hasGcc = hasCommand("gcc");
                String batchFile = hasGcc ? "compile.bat" : "compile-msvc.bat";
                dialog.appendLog(localeManager.get("compiler.log.buildTarget") + " " + batchFile);

                if (!runCommand(sourceDir, dialog, localeManager, "cmd", "/c", batchFile)) {
                    dialog.appendLog(localeManager.get("compiler.error.compilationFailed"));
                    return false;
                }
            } else {
                String makeTarget = isArm ? "build-arm" : "build-i686";
                dialog.appendLog(localeManager.get("compiler.log.buildTarget") + " " + makeTarget);

                if (!runCommand(sourceDir, dialog, localeManager, "make", makeTarget)) {
                    dialog.appendLog(localeManager.get("compiler.error.compilationFailed"));
                    dialog.showInstallButton(() -> installDependencies(isWindows, isArm, dialog, localeManager));
                    return false;
                }
            }

            String binaryName = isWindows ? "ninecraft.exe" : "ninecraft";
            File builtBinary = new File(sourceDir, binaryName);
            if (isWindows && !builtBinary.exists()) {
                builtBinary = new File(sourceDir, "bin/" + binaryName);
            }

            File targetBinary = new File(gameDir, binaryName);

            if (builtBinary.exists()) {
                if (targetBinary.exists()) targetBinary.delete();
                
                if (builtBinary.renameTo(targetBinary)) {
                    targetBinary.setExecutable(true);
                    dialog.appendLog(localeManager.get("compiler.log.binaryMoved") + " " + targetBinary.getAbsolutePath());
                    return true;
                } else {
                    dialog.appendLog(localeManager.get("compiler.error.moveBinary"));
                    return false;
                }
            } else {
                dialog.appendLog(localeManager.get("compiler.error.binaryNotFound"));
                return false;
            }

        } catch (Exception e) {
            dialog.appendLog(localeManager.get("compiler.error.exception") + " " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static List<String> checkWindowsDependencies() {
        List<String> missing = new ArrayList<>();
        
        if (!hasCommand("git")) missing.add("git");
        if (!hasCommand("cmake")) missing.add("cmake");
        
        boolean hasPython = hasCommand("python") || hasCommand("python3");
        if (!hasPython) {
            missing.add("python");
        } else {
            if (!checkPythonPackage("jinja2")) missing.add("jinja2 (pip package)");
        }

        boolean hasGcc = hasCommand("gcc");
        boolean hasMsvc = hasCommand("cl"); 
        
        if (!hasGcc && !hasMsvc) {
            missing.add("Compiler (MinGW or Visual Studio)");
        }
        
        return missing;
    }

    private static List<String> checkLinuxDependencies(boolean isArm) {
        List<String> missing = new ArrayList<>();
        
        if (!hasCommand("git")) missing.add("git");
        if (!hasCommand("make")) missing.add("make");
        if (!hasCommand("cmake")) missing.add("cmake");
        
        if (hasCommand("dpkg")) {
            if (isArm) {
                if (!hasPackage("gcc-arm-linux-gnueabihf")) missing.add("gcc-arm-linux-gnueabihf");
            } else {
                if (!hasPackage("gcc-multilib")) missing.add("gcc-multilib");
                if (!hasPackage("g++-multilib")) missing.add("g++-multilib");
            }
        } else {
            if (!hasCommand("gcc")) missing.add("gcc");
        }
        
        return missing;
    }

    private static boolean hasCommand(String command) {
        try {
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
            String checkCmd = isWindows ? "where" : "which";
            Process p = new ProcessBuilder(checkCmd, command).start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkPythonPackage(String pkg) {
        String python = hasCommand("python") ? "python" : "python3";
        try {
            Process p = new ProcessBuilder(python, "-c", "import " + pkg).start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean hasPackage(String packageName) {
        try {
            Process p = new ProcessBuilder("dpkg", "-s", packageName).start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static void installDependencies(boolean isWindows, boolean isArm, NinecraftCompilationDialog dialog, LocaleManager localeManager) {
        if (isWindows) {
            dialog.appendLog("Checking Windows dependencies...");
            if (hasCommand("python") || hasCommand("python3")) {
                if (!checkPythonPackage("jinja2")) {
                    String py = hasCommand("python") ? "python" : "python3";
                    dialog.appendLog("Installing jinja2 via pip...");
                    runCommand(new File("."), dialog, localeManager, py, "-m", "pip", "install", "jinja2");
                }
            } else {
                dialog.appendLog("Python is missing. Please install Python 3.");
            }
            
            if (!hasCommand("git") || !hasCommand("cmake")) {
                dialog.appendLog("Please install Git and CMake manually and add them to PATH.");
            }
            if (!hasCommand("gcc") && !hasCommand("cl")) {
                dialog.appendLog("Please install MinGW32/LLVM-MinGW or Visual Studio Build Tools.");
            }
            dialog.appendLog("After installing, restart the launcher.");
            return;
        }

        String installCmd;
        if (isArm) {
            installCmd = "sudo dpkg --add-architecture armhf && sudo apt update && sudo apt install -y " + DEPS_ARM64;
        } else {
            installCmd = "sudo dpkg --add-architecture i386 && sudo apt update && sudo apt install -y " + DEPS_AMD64;
        }

        dialog.appendLog(localeManager.get("compiler.log.launchingTerminal"));
        dialog.appendLog(localeManager.get("compiler.log.command") + " " + installCmd);

        try {
            String[] terminals = {"x-terminal-emulator", "gnome-terminal", "konsole", "xfce4-terminal", "lxterminal", "mate-terminal"};
            boolean launched = false;

            for (String terminal : terminals) {
                if (hasCommand(terminal)) {
                    if (terminal.equals("gnome-terminal") || terminal.equals("mate-terminal")) {
                        new ProcessBuilder(terminal, "--", "bash", "-c", installCmd + "; echo 'Done. Press Enter to close.'; read").start();
                    } else {
                        new ProcessBuilder(terminal, "-e", "bash -c \"" + installCmd + "; echo 'Done. Press Enter to close.'; read\"").start();
                    }
                    launched = true;
                    break;
                }
            }

            if (!launched) {
                dialog.appendLog(localeManager.get("compiler.error.noTerminal"));
                dialog.appendLog(localeManager.get("compiler.log.manualRun"));
            }

        } catch (Exception e) {
            dialog.appendLog(localeManager.get("compiler.error.launchInstaller") + " " + e.getMessage());
        }
    }

    private static boolean runCommand(File workingDir, NinecraftCompilationDialog dialog, LocaleManager localeManager, String... command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            if (workingDir != null) pb.directory(workingDir);
            pb.redirectErrorStream(true);
            Process p = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    dialog.appendLog(line);
                }
            }

            int exitCode = p.waitFor();
            if (exitCode != 0) {
                dialog.appendLog(localeManager.get("compiler.error.commandFailed") + " " + exitCode);
                return false;
            }
            return true;
        } catch (Exception e) {
            dialog.appendLog(localeManager.get("compiler.error.executeCommand") + " " + e.getMessage());
            return false;
        }
    }
}