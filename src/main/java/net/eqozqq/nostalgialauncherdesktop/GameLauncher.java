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
        File workingDir = gameDir;
        if (customExecutablePath != null && !customExecutablePath.isEmpty()) {
            executable = new File(customExecutablePath);
        } else {
            String executableName = isWindows ? "ninecraft.exe" : "ninecraft";
            executable = new File(gameDir, executableName);
        }
        
        boolean isMac = osName.contains("mac");
        boolean isMacApp = isMac && executable.isDirectory() && executable.getName().endsWith(".app");

        if (!isMacApp) {
            if (executable.isDirectory() || !executable.exists()) {
                throw new Exception("error.executableNotFound:" + executable.getAbsolutePath());
            }
        }
        
        if (!isWindows && !isMacApp) {
            executable.setExecutable(true, false);
            try {
                new ProcessBuilder("chmod", "+x", executable.getAbsolutePath()).start().waitFor();
            } catch (Exception ignored) {
            }
            String interp = getElfInterpreter(executable);
            if (interp != null && !new File(interp).exists()) {
                throw new Exception(LocaleManager.getInstance().get("error.missingDynamicLinker", interp, getMissing32BitLibsInstruction()));
            }
        }

        if (isWindows) {
            File shortWorkingDir = getShortPath(workingDir);
            File shortExecutable = getShortPath(executable);
            workingDir = shortWorkingDir;
            executable = shortExecutable;
        }

        ProcessBuilder processBuilder;
        if (isWindows) {
            if (enableDebugging) {
                processBuilder = new ProcessBuilder("cmd.exe", "/c", "start", "/wait", "Ninecraft Debug", "cmd.exe", "/c", 
                    "\"" + executable.getAbsolutePath() + "\" & echo %errorlevel% > exit.tmp & pause");
            } else {
                processBuilder = new ProcessBuilder(executable.getAbsolutePath());
            }
        } else if (isMacApp) {
            processBuilder = new ProcessBuilder("open", executable.getAbsolutePath());
        } else {
            if (isMac) {
                if (enableDebugging) {
                    processBuilder = new ProcessBuilder("osascript", "-e", 
                        "tell application \"Terminal\" to do script \"cd \\\"" + workingDir.getAbsolutePath().replace("\"", "\\\"") + "\\\" && \\\"" + executable.getAbsolutePath().replace("\"", "\\\"") + "\\\"\"");
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
                                "\"$1\"; echo $? > exit.tmp; echo 'Process exited.'; read", "_", executable.getAbsolutePath());
                        } else {
                            processBuilder = new ProcessBuilder(selectedTerminal, "-e", 
                                "bash -c '\"$1\"; echo $? > exit.tmp; echo \"Process exited.\"; read'", "_", executable.getAbsolutePath());
                        }
                    } else {
                        processBuilder = new ProcessBuilder(executable.getAbsolutePath());
                    }
                } else {
                    processBuilder = new ProcessBuilder(executable.getAbsolutePath());
                }
            }
        }

        processBuilder.directory(workingDir);
        processBuilder.redirectErrorStream(true);
        if (!isWindows) {
            processBuilder.environment().put("LC_ALL", "C.UTF-8");
            processBuilder.environment().put("LANG", "C.UTF-8");
        }

        try {
            return processBuilder.start();
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("error: 2") && !isWindows && !isMac) {
                String interp = getElfInterpreter(executable);
                if (interp != null && !new File(interp).exists()) {
                    throw new Exception(LocaleManager.getInstance().get("error.missingDynamicLinker", interp, getMissing32BitLibsInstruction()), e);
                }
            }
            throw new Exception("error.launchGameFailed:" + e.getMessage(), e);
        }
    }

    private static File getShortPath(File file) {
        if (file == null || !file.exists()) {
            return file;
        }
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                Process p = new ProcessBuilder("cmd.exe", "/c", "for %I in (\"" + file.getAbsolutePath() + "\") do @echo %~sI").start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                    String line = reader.readLine();
                    if (line != null && !line.trim().isEmpty()) {
                        File shortFile = new File(line.trim());
                        if (shortFile.exists()) {
                            return shortFile;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return file;
    }

    private String getElfInterpreter(File file) {
        if (!file.exists() || file.isDirectory() || file.length() < 52) {
            return null;
        }
        try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(file, "r")) {
            byte[] magic = new byte[4];
            raf.readFully(magic);
            if (magic[0] != 0x7F || magic[1] != 'E' || magic[2] != 'L' || magic[3] != 'F') {
                return null;
            }
            int eiClass = raf.readUnsignedByte();
            int eiData = raf.readUnsignedByte();
            boolean littleEndian = (eiData == 1);

            if (eiClass == 1) {
                raf.seek(28);
                long phOff = readInt(raf, littleEndian) & 0xFFFFFFFFL;
                raf.seek(42);
                int phentSize = readShort(raf, littleEndian);
                int phNum = readShort(raf, littleEndian);

                for (int i = 0; i < phNum; i++) {
                    raf.seek(phOff + (long) i * phentSize);
                    int pType = readInt(raf, littleEndian);
                    if (pType == 3) {
                        long pOffset = readInt(raf, littleEndian) & 0xFFFFFFFFL;
                        raf.seek(phOff + (long) i * phentSize + 16);
                        long pFilesz = readInt(raf, littleEndian) & 0xFFFFFFFFL;
                        if (pFilesz > 0 && pFilesz < 4096) {
                            raf.seek(pOffset);
                            byte[] buf = new byte[(int) pFilesz];
                            raf.readFully(buf);
                            int len = 0;
                            while (len < buf.length && buf[len] != 0) {
                                len++;
                            }
                            return new String(buf, 0, len, java.nio.charset.StandardCharsets.UTF_8);
                        }
                    }
                }
            } else if (eiClass == 2) {
                raf.seek(32);
                long phOff = readLong(raf, littleEndian);
                raf.seek(54);
                int phentSize = readShort(raf, littleEndian);
                int phNum = readShort(raf, littleEndian);

                for (int i = 0; i < phNum; i++) {
                    raf.seek(phOff + (long) i * phentSize);
                    int pType = readInt(raf, littleEndian);
                    if (pType == 3) {
                        raf.seek(phOff + (long) i * phentSize + 8);
                        long pOffset = readLong(raf, littleEndian);
                        raf.seek(phOff + (long) i * phentSize + 32);
                        long pFilesz = readLong(raf, littleEndian);
                        if (pFilesz > 0 && pFilesz < 4096) {
                            raf.seek(pOffset);
                            byte[] buf = new byte[(int) pFilesz];
                            raf.readFully(buf);
                            int len = 0;
                            while (len < buf.length && buf[len] != 0) {
                                len++;
                            }
                            return new String(buf, 0, len, java.nio.charset.StandardCharsets.UTF_8);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static int readShort(java.io.RandomAccessFile raf, boolean littleEndian) throws IOException {
        int b1 = raf.readUnsignedByte();
        int b2 = raf.readUnsignedByte();
        return littleEndian ? (b2 << 8) | b1 : (b1 << 8) | b2;
    }

    private static int readInt(java.io.RandomAccessFile raf, boolean littleEndian) throws IOException {
        int b1 = raf.readUnsignedByte();
        int b2 = raf.readUnsignedByte();
        int b3 = raf.readUnsignedByte();
        int b4 = raf.readUnsignedByte();
        return littleEndian ? (b4 << 24) | (b3 << 16) | (b2 << 8) | b1 : (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

    private static long readLong(java.io.RandomAccessFile raf, boolean littleEndian) throws IOException {
        long l1 = readInt(raf, littleEndian) & 0xFFFFFFFFL;
        long l2 = readInt(raf, littleEndian) & 0xFFFFFFFFL;
        return littleEndian ? (l2 << 32) | l1 : (l1 << 32) | l2;
    }

    public static boolean runInTerminal(String cmd) {
        String[] terminals = { "x-terminal-emulator", "gnome-terminal", "konsole", "xfce4-terminal", "lxterminal", "mate-terminal", "alacritty", "kitty", "xterm" };
        for (String terminal : terminals) {
            try {
                Process p = new ProcessBuilder("which", terminal).start();
                if (p.waitFor() == 0) {
                    if (terminal.equals("gnome-terminal") || terminal.equals("mate-terminal")) {
                        new ProcessBuilder(terminal, "--", "bash", "-c", cmd + "; echo 'Done. Press Enter to close.'; read").start();
                    } else {
                        new ProcessBuilder(terminal, "-e", "bash -c \"" + cmd + "; echo 'Done. Press Enter to close.'; read\"").start();
                    }
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public static String detectDistro() {
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
                if (id.contains("fedora") || id.contains("rhel") || id.contains("centos") || id.contains("almalinux") || id.contains("rocky")) {
                    return "fedora";
                }
                if (id.contains("suse")) {
                    return "suse";
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
                if (idLike.contains("suse")) {
                    return "suse";
                }
                if (idLike.contains("alpine")) {
                    return "alpine";
                }
            }
        } catch (Exception ignored) {
        }
        return "unknown";
    }

    public static String getMissing32BitLibsInstruction() {
        String distro = detectDistro();
        if ("fedora".equals(distro)) {
            return "sudo dnf install -y glibc.i686 libstdc++.i686 openal-soft.i686 libX11.i686 libXrandr.i686 libXinerama.i686 libXcursor.i686 libXi.i686 libglvnd-glx.i686 alsa-lib.i686 pulseaudio-libs.i686";
        } else if ("debian".equals(distro)) {
            return "sudo dpkg --add-architecture i386 && sudo apt update && sudo apt install -y libc6:i386 libstdc++6:i386 libopenal1:i386 libx11-6:i386 libxrandr2:i386 libxinerama1:i386 libxcursor1:i386 libxi6:i386 libgl1:i386 libasound2:i386 libpulse0:i386";
        } else if ("arch".equals(distro)) {
            return "sudo pacman -S --noconfirm lib32-glibc lib32-gcc-libs lib32-openal lib32-libx11 lib32-libxrandr lib32-libxinerama lib32-libxcursor lib32-libxi lib32-libglvnd lib32-alsa-lib lib32-libpulse";
        } else if ("suse".equals(distro)) {
            return "sudo zypper install -y glibc-32bit libstdc++6-32bit libopenal1-32bit libX11-6-32bit libXrandr2-32bit libXinerama1-32bit libXcursor1-32bit libXi6-32bit libglvnd-32bit libasound2-32bit libpulse0-32bit";
        } else {
            return "sudo dnf install -y glibc.i686 libstdc++.i686 alsa-lib.i686 pulseaudio-libs.i686 || sudo apt install -y libc6:i386 libasound2:i386 libpulse0:i386 || sudo pacman -S --noconfirm lib32-glibc lib32-alsa-lib lib32-libpulse";
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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
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