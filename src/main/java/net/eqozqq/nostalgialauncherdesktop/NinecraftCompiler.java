package net.eqozqq.nostalgialauncherdesktop;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class NinecraftCompiler {

    private static final String DEPS_AMD64 = "git make cmake gcc g++ gcc-multilib g++-multilib libopenal-dev:i386 libx11-dev:i386 libxrandr-dev:i386 libxinerama-dev:i386 libxcursor-dev:i386 libxi-dev:i386 libgl-dev:i386 zenity unzip python3-jinja2";
    private static final String DEPS_ARM64 = "git make cmake gcc-arm-linux-gnueabihf g++-arm-linux-gnueabihf libopenal-dev:armhf libx11-dev:armhf libxrandr-dev:armhf libxinerama-dev:armhf libxcursor-dev:armhf libxi-dev:armhf libgl-dev:armhf zenity unzip python3-jinja2";

    private volatile boolean cancelled = false;
    private volatile Process currentProcess = null;

    public void cancel() {
        cancelled = true;
        if (currentProcess != null) {
            currentProcess.destroyForcibly();
        }
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean compile(File gameDir, NinecraftCompilationDialog dialog, LocaleManager localeManager,
            String repoUrl) {
        cancelled = false;
        File sourceDir = new File(gameDir, "Ninecraft_source");
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("win");
        String arch = System.getProperty("os.arch").toLowerCase();
        boolean isArm = arch.contains("arm") || arch.contains("aarch64");

        try {
            if (cancelled)
                return false;

            dialog.setStatus(localeManager.get("compiler.status.checkingDeps"));
            List<String> missing = isWindows ? checkWindowsDependencies() : checkLinuxDependencies(isArm);

            while (!missing.isEmpty()) {
                if (cancelled)
                    return false;

                dialog.appendLog(
                        localeManager.get("compiler.error.missingSpecificDeps") + " " + String.join(", ", missing));
                dialog.appendLog(localeManager.get("compiler.log.installPrompt"));
                dialog.setStatus(localeManager.get("compiler.status.missingDepsTitle"));

                final Object lock = new Object();
                final boolean[] installClicked = {false};

                dialog.showInstallButton(() -> {
                    synchronized (lock) {
                        installClicked[0] = true;
                        lock.notifyAll();
                    }
                });

                synchronized (lock) {
                    while (!installClicked[0] && !cancelled && !dialog.isCancelled()) {
                        try {
                            lock.wait(500);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }

                if (cancelled || dialog.isCancelled()) {
                    return false;
                }

                if (installClicked[0]) {
                    dialog.appendLog("Starting dependency installation...");
                    installDependenciesSynchronously(isWindows, isArm, dialog, localeManager);
                    if (cancelled || dialog.isCancelled()) {
                        return false;
                    }

                    dialog.appendLog("Re-checking dependencies...");
                    missing = isWindows ? checkWindowsDependencies() : checkLinuxDependencies(isArm);
                    if (missing.isEmpty()) {
                        dialog.appendLog("All dependencies are now met. Continuing to compile!");
                        break;
                    } else {
                        dialog.appendLog("Some dependencies are still missing: " + String.join(", ", missing));
                    }
                }
            }

            if (cancelled)
                return false;

            if (sourceDir.exists()) {
                dialog.setStatus(localeManager.get("compiler.status.updatingRepo"));

                dialog.appendLog("Setting remote origin to: " + repoUrl);
                if (!runCommand(sourceDir, dialog, localeManager, "git", "remote", "set-url", "origin", repoUrl)) {
                    if (cancelled)
                        return false;
                }

                if (!runCommand(sourceDir, dialog, localeManager, "git", "pull")) {
                    if (cancelled)
                        return false;
                    return false;
                }
                if (!runCommand(sourceDir, dialog, localeManager, "git", "submodule", "update", "--init",
                        "--recursive")) {
                    if (cancelled)
                        return false;
                    return false;
                }
            } else {
                dialog.setStatus(localeManager.get("compiler.status.cloningRepo"));
                if (!runCommand(gameDir, dialog, localeManager, "git", "clone", "--recursive", repoUrl,
                        "Ninecraft_source")) {
                    if (cancelled)
                        return false;
                    return false;
                }
            }

            if (cancelled)
                return false;

            dialog.setStatus(localeManager.get("compiler.status.compiling"));
            dialog.appendLog(localeManager.get("compiler.log.archDetected") + " " + arch);

            if (isWindows) {
                boolean hasGcc = hasCommand("gcc");
                String batchFile = hasGcc ? "compile.bat" : "compile-msvc.bat";
                dialog.appendLog(localeManager.get("compiler.log.buildTarget") + " " + batchFile);

                if (!runCommand(sourceDir, dialog, localeManager, "cmd", "/c", batchFile)) {
                    if (cancelled)
                        return false;
                    dialog.appendLog(localeManager.get("compiler.error.compilationFailed"));
                    return false;
                }
            } else {
                String makeTarget = isArm ? "build-arm" : "build-i686";
                dialog.appendLog(localeManager.get("compiler.log.buildTarget") + " " + makeTarget);

                if (!runCommand(sourceDir, dialog, localeManager, "make", makeTarget)) {
                    if (cancelled)
                        return false;
                    dialog.appendLog(localeManager.get("compiler.error.compilationFailed"));
                    dialog.showInstallButton(() -> {
                        new Thread(() -> {
                            installDependenciesSynchronously(isWindows, isArm, dialog, localeManager);
                        }).start();
                    });
                    return false;
                }
            }

            if (cancelled)
                return false;

            String binaryName = isWindows ? "ninecraft.exe" : "ninecraft";
            File builtBinary = new File(sourceDir, binaryName);
            if (isWindows && !builtBinary.exists()) {
                builtBinary = new File(sourceDir, "bin/" + binaryName);
            }

            File targetBinary = new File(gameDir, binaryName);

            if (builtBinary.exists()) {
                if (targetBinary.exists())
                    targetBinary.delete();

                if (builtBinary.renameTo(targetBinary)) {
                    targetBinary.setExecutable(true);
                    dialog.appendLog(
                            localeManager.get("compiler.log.binaryMoved") + " " + targetBinary.getAbsolutePath());
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
            if (!cancelled) {
                dialog.appendLog(localeManager.get("compiler.error.exception") + " " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }
    }

    private List<String> checkWindowsDependencies() {
        List<String> missing = new ArrayList<>();

        if (!hasCommand("git"))
            missing.add("git");
        if (!hasCommand("cmake"))
            missing.add("cmake");

        boolean hasPython = hasCommand("python") || hasCommand("python3");
        if (!hasPython) {
            missing.add("python");
        } else {
            if (!checkPythonPackage("jinja2"))
                missing.add("jinja2 (pip package)");
        }

        boolean hasGcc = hasCommand("gcc");
        boolean hasMsvc = hasCommand("cl");

        if (!hasGcc && !hasMsvc) {
            missing.add("Compiler (MinGW or Visual Studio)");
        }

        return missing;
    }

    private String detectDistro() {
        File file = new File("/etc/os-release");
        if (!file.exists()) {
            return "unknown";
        }
        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(file))) {
            String line;
            String id = null;
            String idLike = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("ID=")) {
                    id = line.substring(3).replace("\"", "").replace("'", "").toLowerCase();
                } else if (line.startsWith("ID_LIKE=")) {
                    idLike = line.substring(8).replace("\"", "").replace("'", "").toLowerCase();
                }
            }
            if (id != null) {
                if (id.contains("ubuntu") || id.contains("debian") || id.contains("mint") || id.contains("pop")) {
                    return "debian";
                }
                if (id.contains("arch") || id.contains("manjaro")) {
                    return "arch";
                }
                if (id.contains("fedora") || id.contains("rhel") || id.contains("centos")) {
                    return "fedora";
                }
                if (id.contains("alpine")) {
                    return "alpine";
                }
            }
            if (idLike != null) {
                if (idLike.contains("ubuntu") || idLike.contains("debian")) {
                    return "debian";
                }
                if (idLike.contains("arch")) {
                    return "arch";
                }
                if (idLike.contains("fedora") || idLike.contains("rhel")) {
                    return "fedora";
                }
                if (idLike.contains("alpine")) {
                    return "alpine";
                }
            }
        } catch (Exception e) {
        }
        return "unknown";
    }

    private List<String> checkLinuxDependencies(boolean isArm) {
        String distro = detectDistro();
        String arch = System.getProperty("os.arch").toLowerCase();
        boolean isX86_64 = arch.contains("amd64") || arch.contains("x86_64");
        boolean isX86 = (arch.contains("i386") || arch.contains("i686") || arch.contains("x86")) && !isX86_64;
        boolean isArm64 = arch.contains("aarch64") || arch.contains("arm64");
        boolean isArm32 = arch.contains("arm") && !isArm64;

        List<String> missing = new ArrayList<>();
        if ("debian".equals(distro)) {
            if (isX86_64) {
                String[] packages = {"git", "make", "cmake", "gcc-multilib", "g++-multilib",
                    "libopenal-dev:i386", "libx11-dev:i386", "libxrandr-dev:i386", "libxinerama-dev:i386",
                    "libxcursor-dev:i386", "libxi-dev:i386", "libgl-dev:i386", "zenity", "unzip", "python3-jinja2"};
                for (String p : packages) {
                    if (!hasPackage(p)) missing.add(p);
                }
            } else if (isArm64) {
                String[] packages = {"git", "make", "cmake", "gcc-arm-linux-gnueabihf", "g++-arm-linux-gnueabihf",
                    "libopenal-dev:armhf", "libx11-dev:armhf", "libxrandr-dev:armhf", "libxinerama-dev:armhf",
                    "libxcursor-dev:armhf", "libxi-dev:armhf", "libgl-dev:armhf", "zenity", "unzip", "python3-jinja2"};
                for (String p : packages) {
                    if (!hasPackage(p)) missing.add(p);
                }
            } else {
                String[] packages = {"git", "make", "cmake", "gcc", "g++", "libopenal-dev", "libx11-dev",
                    "libxrandr-dev", "libxinerama-dev", "libxcursor-dev", "libxi-dev", "libgl-dev", "zenity", "unzip", "python3-jinja2"};
                for (String p : packages) {
                    if (!hasPackage(p)) missing.add(p);
                }
            }
        } else if ("arch".equals(distro)) {
            if (isX86_64) {
                String[] packages = {"git", "make", "cmake", "gcc", "gcc-multilib", "lib32-openal", "lib32-libx11",
                    "lib32-libxrandr", "lib32-libxinerama", "lib32-libxcursor", "lib32-libxi", "lib32-libglvnd", "zenity", "unzip", "python-jinja"};
                for (String p : packages) {
                    if (!hasPacmanPackage(p)) missing.add(p);
                }
            } else {
                String[] packages = {"git", "make", "cmake", "gcc", "openal", "libx11", "libxrandr", "libxinerama",
                    "libxcursor", "libxi", "libglvnd", "zenity", "unzip", "python-jinja"};
                for (String p : packages) {
                    if (!hasPacmanPackage(p)) missing.add(p);
                }
            }
        } else if ("fedora".equals(distro)) {
            if (isX86_64) {
                String[] packages = {"git", "make", "cmake", "gcc", "g++", "glibc-devel.i686", "libstdc++-devel.i686",
                    "openal-soft-devel.i686", "libX11-devel.i686", "libXrandr-devel.i686", "libXinerama-devel.i686",
                    "libXcursor-devel.i686", "libXi-devel.i686", "libglvnd-devel.i686", "zenity", "unzip", "python3-jinja2"};
                for (String p : packages) {
                    if (!hasFedoraPackage(p)) missing.add(p);
                }
            } else {
                String[] packages = {"git", "make", "cmake", "gcc", "g++", "openal-soft-devel", "libX11-devel",
                    "libXrandr-devel", "libXinerama-devel", "libXcursor-devel", "libXi-devel", "libglvnd-devel", "zenity", "unzip", "python3-jinja2"};
                for (String p : packages) {
                    if (!hasFedoraPackage(p)) missing.add(p);
                }
            }
        } else if ("alpine".equals(distro)) {
            String[] packages = {"git", "make", "cmake", "gcc", "g++", "openal-soft-dev", "libx11-dev", "libxrandr-dev",
                "libxinerama-dev", "libxcursor-dev", "libxi-dev", "mesa-dev", "zenity", "unzip", "py3-jinja2"};
            for (String p : packages) {
                if (!hasAlpinePackage(p)) missing.add(p);
            }
        } else {
            if (!hasCommand("git")) missing.add("git");
            if (!hasCommand("make")) missing.add("make");
            if (!hasCommand("cmake")) missing.add("cmake");
            if (!hasCommand("gcc")) missing.add("gcc");
            if (!hasCommand("g++")) missing.add("g++");
        }
        return missing;
    }

    private boolean hasCommand(String command) {
        try {
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
            String checkCmd = isWindows ? "where" : "which";
            Process p = new ProcessBuilder(checkCmd, command).start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkPythonPackage(String pkg) {
        String python = hasCommand("python") ? "python" : "python3";
        try {
            Process p = new ProcessBuilder(python, "-c", "import " + pkg).start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasPackage(String packageName) {
        try {
            Process p = new ProcessBuilder("dpkg", "-s", packageName).start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasPacmanPackage(String packageName) {
        try {
            Process p = new ProcessBuilder("pacman", "-Qi", packageName).start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasFedoraPackage(String packageName) {
        try {
            Process p = new ProcessBuilder("rpm", "-q", packageName).start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasAlpinePackage(String packageName) {
        try {
            Process p = new ProcessBuilder("apk", "info", "-e", packageName).start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean installDependenciesSynchronously(boolean isWindows, boolean isArm, NinecraftCompilationDialog dialog,
            LocaleManager localeManager) {
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
            return false;
        }

        String distro = detectDistro();
        String arch = System.getProperty("os.arch").toLowerCase();
        boolean isX86_64 = arch.contains("amd64") || arch.contains("x86_64");
        boolean isX86 = (arch.contains("i386") || arch.contains("i686") || arch.contains("x86")) && !isX86_64;
        boolean isArm64 = arch.contains("aarch64") || arch.contains("arm64");
        boolean isArm32 = arch.contains("arm") && !isArm64;

        String installCmd = "";
        if ("debian".equals(distro)) {
            if (isX86_64) {
                installCmd = "sudo dpkg --add-architecture i386 && sudo apt update && sudo apt install -y git make cmake gcc g++ gcc-multilib g++-multilib libopenal-dev:i386 libx11-dev:i386 libxrandr-dev:i386 libxinerama-dev:i386 libxcursor-dev:i386 libxi-dev:i386 libgl-dev:i386 zenity unzip python3-jinja2";
            } else if (isArm64) {
                installCmd = "sudo dpkg --add-architecture armhf && sudo apt update && sudo apt install -y git make cmake gcc-arm-linux-gnueabihf g++-arm-linux-gnueabihf libopenal-dev:armhf libx11-dev:armhf libxrandr-dev:armhf libxinerama-dev:armhf libxcursor-dev:armhf libxi-dev:armhf libgl-dev:armhf zenity unzip python3-jinja2";
            } else {
                installCmd = "sudo apt update && sudo apt install -y git make cmake gcc g++ libopenal-dev libx11-dev libxrandr-dev libxinerama-dev libxcursor-dev libxi-dev libgl-dev zenity unzip python3-jinja2";
            }
        } else if ("arch".equals(distro)) {
            if (isX86_64) {
                installCmd = "sudo pacman -Syu --noconfirm && sudo pacman -S --noconfirm git make cmake gcc gcc-multilib lib32-openal lib32-libx11 lib32-libxrandr lib32-libxinerama lib32-libxcursor lib32-libxi lib32-libglvnd zenity unzip python-jinja";
            } else {
                installCmd = "sudo pacman -Syu --noconfirm && sudo pacman -S --noconfirm git make cmake gcc openal libx11 libxrandr libxinerama libxcursor libxi libglvnd zenity unzip python-jinja";
            }
        } else if ("fedora".equals(distro)) {
            if (isX86_64) {
                installCmd = "sudo dnf update -y && sudo dnf install -y git make cmake gcc g++ glibc-devel.i686 libstdc++-devel.i686 openal-soft-devel.i686 libX11-devel.i686 libXrandr-devel.i686 libXinerama-devel.i686 libXcursor-devel.i686 libXi-devel.i686 libglvnd-devel.i686 zenity unzip python3-jinja2";
            } else {
                installCmd = "sudo dnf update -y && sudo dnf install -y git make cmake gcc g++ openal-soft-devel libX11-devel libXrandr-devel libXinerama-devel libXcursor-devel libXi-devel libglvnd-devel zenity unzip python3-jinja2";
            }
        } else if ("alpine".equals(distro)) {
            installCmd = "sudo apk update && sudo apk add git make cmake gcc g++ openal-soft-dev libx11-dev libxrandr-dev libxinerama-dev libxcursor-dev libxi-dev mesa-dev zenity unzip py3-jinja2";
        } else {
            if (hasCommand("nix")) {
                installCmd = "nix --extra-experimental-features \"nix-command flakes\" shell github:MCPI-Revival/Ninecraft --impure";
            } else {
                dialog.appendLog("Unsupported distribution. Please install dependencies manually.");
                return false;
            }
        }

        dialog.appendLog(localeManager.get("compiler.log.launchingTerminal"));
        dialog.appendLog(localeManager.get("compiler.log.command") + " " + installCmd);

        try {
            String[] terminals = { "x-terminal-emulator", "gnome-terminal", "konsole", "xfce4-terminal", "lxterminal",
                    "mate-terminal" };
            boolean launched = false;
            Process p = null;

            for (String terminal : terminals) {
                if (hasCommand(terminal)) {
                    if (terminal.equals("gnome-terminal") || terminal.equals("mate-terminal")) {
                        p = new ProcessBuilder(terminal, "--", "bash", "-c",
                                installCmd + "; echo 'Done. Press Enter to close.'; read").start();
                    } else {
                        p = new ProcessBuilder(terminal, "-e",
                                "bash -c \"" + installCmd + "; echo 'Done. Press Enter to close.'; read\"").start();
                    }
                    launched = true;
                    break;
                }
            }

            if (!launched) {
                dialog.appendLog(localeManager.get("compiler.error.noTerminal"));
                dialog.appendLog(localeManager.get("compiler.log.manualRun"));
                return false;
            }

            if (p != null) {
                dialog.setStatus("Waiting for dependency installation to complete...");
                p.waitFor();
                return true;
            }

        } catch (Exception e) {
            dialog.appendLog(localeManager.get("compiler.error.launchInstaller") + " " + e.getMessage());
        }
        return false;
    }

    private boolean runCommand(File workingDir, NinecraftCompilationDialog dialog, LocaleManager localeManager,
            String... command) {
        if (cancelled)
            return false;

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            if (workingDir != null)
                pb.directory(workingDir);
            pb.redirectErrorStream(true);
            currentProcess = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (cancelled) {
                        currentProcess.destroyForcibly();
                        return false;
                    }
                    dialog.appendLog(line);
                }
            }

            int exitCode = currentProcess.waitFor();
            currentProcess = null;

            if (cancelled)
                return false;

            if (exitCode != 0) {
                dialog.appendLog(localeManager.get("compiler.error.commandFailed") + " " + exitCode);
                return false;
            }
            return true;
        } catch (Exception e) {
            if (!cancelled) {
                dialog.appendLog(localeManager.get("compiler.error.executeCommand") + " " + e.getMessage());
            }
            return false;
        }
    }
}