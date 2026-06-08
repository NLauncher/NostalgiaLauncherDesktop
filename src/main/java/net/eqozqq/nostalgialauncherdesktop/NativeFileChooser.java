package net.eqozqq.nostalgialauncherdesktop;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.FilenameFilter;
import javax.swing.SwingUtilities;
import java.awt.Component;

public class NativeFileChooser {

    public static File chooseFile(Component parent, String title, final String[] extensions, String filePattern) {
        Window window = null;
        if (parent != null) {
            window = SwingUtilities.getWindowAncestor(parent);
        }

        FileDialog dialog;
        if (window instanceof Frame) {
            dialog = new FileDialog((Frame) window, title, FileDialog.LOAD);
        } else if (window instanceof java.awt.Dialog) {
            dialog = new FileDialog((java.awt.Dialog) window, title, FileDialog.LOAD);
        } else {
            dialog = new FileDialog((Frame) null, title, FileDialog.LOAD);
        }

        if (filePattern != null) {
            dialog.setFile(filePattern);
        }

        if (extensions != null && extensions.length > 0) {
            dialog.setFilenameFilter(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    String lower = name.toLowerCase();
                    for (String ext : extensions) {
                        if (lower.endsWith(ext.toLowerCase()) || lower.endsWith(ext.toLowerCase() + ".lnk")) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        dialog.setVisible(true);

        String dir = dialog.getDirectory();
        String file = dialog.getFile();
        if (dir != null && file != null) {
            return new File(dir, file);
        }
        return null;
    }

    public static File chooseDirectory(Component parent, String title) {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("mac")) {
            try {
                System.setProperty("apple.awt.fileDialogForDirectories", "true");
                Window window = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
                FileDialog dialog;
                if (window instanceof Frame) {
                    dialog = new FileDialog((Frame) window, title, FileDialog.LOAD);
                } else if (window instanceof java.awt.Dialog) {
                    dialog = new FileDialog((java.awt.Dialog) window, title, FileDialog.LOAD);
                } else {
                    dialog = new FileDialog((Frame) null, title, FileDialog.LOAD);
                }
                dialog.setVisible(true);
                String dir = dialog.getDirectory();
                String file = dialog.getFile();
                System.setProperty("apple.awt.fileDialogForDirectories", "false");
                if (dir != null && file != null) {
                    return new File(dir, file);
                }
                if (dir != null) {
                    return new File(dir);
                }
            } catch (Exception e) {
            }
        }

        if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            try {
                Process p = Runtime.getRuntime().exec(new String[] { "which", "zenity" });
                if (p.waitFor() == 0) {
                    Process zenity = Runtime.getRuntime()
                            .exec(new String[] { "zenity", "--file-selection", "--directory", "--title=" + title });
                    try (java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(zenity.getInputStream()))) {
                        String line = reader.readLine();
                        if (line != null && !line.trim().isEmpty()) {
                            return new File(line.trim());
                        }
                    }
                }
            } catch (Exception e) {
            }
            try {
                Process p = Runtime.getRuntime().exec(new String[] { "which", "kdialog" });
                if (p.waitFor() == 0) {
                    Process kdialog = Runtime.getRuntime()
                            .exec(new String[] { "kdialog", "--getexistingdirectory", "--title", title });
                    try (java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(kdialog.getInputStream()))) {
                        String line = reader.readLine();
                        if (line != null && !line.trim().isEmpty()) {
                            return new File(line.trim());
                        }
                    }
                }
            } catch (Exception e) {
            }
        }

        if (os.contains("win")) {
            try {
                String cmd = "Add-Type -AssemblyName System.Windows.Forms; $f = New-Object System.Windows.Forms.FolderBrowserDialog; $f.Description = '"
                        + title.replace("'", "''") + "'; if ($f.ShowDialog() -eq 'OK') { $f.SelectedPath }";
                ProcessBuilder builder = new ProcessBuilder("powershell", "-NoProfile", "-Command", cmd);
                Process process = builder.start();
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()))) {
                    String line = reader.readLine();
                    if (line != null && !line.trim().isEmpty()) {
                        return new File(line.trim());
                    }
                }
            } catch (Exception e) {
            }
        }

        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
        int option = chooser.showOpenDialog(parent);
        if (option == javax.swing.JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }
}