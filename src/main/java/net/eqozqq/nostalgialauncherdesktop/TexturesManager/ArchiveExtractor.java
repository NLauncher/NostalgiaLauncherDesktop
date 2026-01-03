package net.eqozqq.nostalgialauncherdesktop.TexturesManager;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;

public class ArchiveExtractor {

    public static void install(File archiveFile, File destDir) throws IOException {
        String fileName = archiveFile.getName().toLowerCase();
        if (fileName.endsWith(".zip") || fileName.endsWith(".jar") || fileName.endsWith(".mcpack")) {
            installZip(archiveFile, destDir);
        } else if (fileName.endsWith(".rar")) {
            installRar(archiveFile, destDir);
        } else if (fileName.endsWith(".tar")) {
            installTar(archiveFile, destDir);
        } else if (fileName.endsWith(".7z")) {
            install7z(archiveFile, destDir);
        } else {
            throw new IOException("Unsupported archive format.");
        }
    }

    private static String normalizePath(String path) {
        return path.replace("\\", "/");
    }

    private static String getRootPrefix(String entryName) {
        String normalized = normalizePath(entryName);
        int assetsIndex = normalized.indexOf("assets/");
        if (assetsIndex != -1) {
            return normalized.substring(0, assetsIndex);
        }
        int resIndex = normalized.indexOf("res/");
        if (resIndex != -1) {
            return normalized.substring(0, resIndex);
        }
        return null;
    }

    private static void installZip(File zipFile, File destDir) throws IOException {
        String prefix = null;
        try (ZipFile zf = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                String p = getRootPrefix(entry.getName());
                if (p != null) {
                    if (prefix == null || p.length() < prefix.length()) {
                        prefix = p;
                    }
                }
            }
        }
        if (prefix == null) prefix = "";

        try (ZipFile zf = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                extractEntry(zf.getInputStream(entry), entry.getName(), destDir, prefix);
            }
        }
    }

    private static void installRar(File rarFile, File destDir) throws IOException {
        String prefix = null;
        try (Archive archive = new Archive(rarFile)) {
            for (FileHeader fh : archive) {
                if (fh.isDirectory()) continue;
                String p = getRootPrefix(fh.getFileNameString());
                if (p != null) {
                    if (prefix == null || p.length() < prefix.length()) prefix = p;
                }
            }
        } catch (Exception e) { throw new IOException(e); }
        
        if (prefix == null) prefix = "";

        try (Archive archive = new Archive(rarFile)) {
            for (FileHeader fh : archive) {
                if (fh.isDirectory()) continue;
                String name = fh.getFileNameString();
                if (!normalizePath(name).startsWith(prefix)) continue;
                
                String relPath = normalizePath(name).substring(prefix.length());
                if (relPath.isEmpty()) continue;
                
                File outFile = new File(destDir, relPath);
                outFile.getParentFile().mkdirs();
                try (OutputStream os = new FileOutputStream(outFile)) {
                    archive.extractFile(fh, os);
                }
            }
        } catch (Exception e) { throw new IOException(e); }
    }

    private static void installTar(File tarFile, File destDir) throws IOException {
        String prefix = null;
        try (TarArchiveInputStream tis = new TarArchiveInputStream(new FileInputStream(tarFile))) {
            TarArchiveEntry entry;
            while ((entry = (TarArchiveEntry) tis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String p = getRootPrefix(entry.getName());
                if (p != null) {
                    if (prefix == null || p.length() < prefix.length()) prefix = p;
                }
            }
        }
        if (prefix == null) prefix = "";

        try (TarArchiveInputStream tis = new TarArchiveInputStream(new FileInputStream(tarFile))) {
            TarArchiveEntry entry;
            while ((entry = (TarArchiveEntry) tis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                extractEntry(tis, entry.getName(), destDir, prefix);
            }
        }
    }

    private static void install7z(File file, File destDir) throws IOException {
        String prefix = null;
        try (SevenZFile szf = new SevenZFile(file)) {
            SevenZArchiveEntry entry;
            while ((entry = szf.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String p = getRootPrefix(entry.getName());
                if (p != null) {
                    if (prefix == null || p.length() < prefix.length()) prefix = p;
                }
            }
        }
        if (prefix == null) prefix = "";

        try (SevenZFile szf = new SevenZFile(file)) {
            SevenZArchiveEntry entry;
            while ((entry = szf.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                if (!normalizePath(name).startsWith(prefix)) continue;
                
                String relPath = normalizePath(name).substring(prefix.length());
                if (relPath.isEmpty()) continue;

                File outFile = new File(destDir, relPath);
                outFile.getParentFile().mkdirs();
                
                byte[] buffer = new byte[4096];
                int len;
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    while ((len = szf.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
        }
    }

    private static void extractEntry(InputStream is, String entryName, File destDir, String prefix) throws IOException {
        String normName = normalizePath(entryName);
        if (!normName.startsWith(prefix)) return;
        
        String relPath = normName.substring(prefix.length());
        if (relPath.isEmpty()) return;
        
        File outFile = new File(destDir, relPath);
        outFile.getParentFile().mkdirs();
        try (OutputStream os = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
        }
    }
    
    public static void extractDefaultTextures(File apkFile, File destDir) throws IOException {
         try (ZipInputStream zis = new ZipInputStream(new FileInputStream(apkFile))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                String name = zipEntry.getName();
                if (name.startsWith("assets/") || name.startsWith("res/") || name.startsWith("lib/")) {
                     File newFile = new File(destDir, name);
                     if (zipEntry.isDirectory()) {
                         newFile.mkdirs();
                     } else {
                         newFile.getParentFile().mkdirs();
                         try (FileOutputStream fos = new FileOutputStream(newFile)) {
                             byte[] buffer = new byte[4096];
                             int len;
                             while ((len = zis.read(buffer)) > 0) {
                                 fos.write(buffer, 0, len);
                             }
                         }
                     }
                }
            }
         }
    }
}