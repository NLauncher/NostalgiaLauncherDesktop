package net.eqozqq.nostalgialauncherdesktop.TexturesManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;

public class ArchiveExtractor {

    public static void extract(File archiveFile, File destDir) throws IOException {
        String fileName = archiveFile.getName().toLowerCase();
        if (fileName.endsWith(".zip")) {
            extractZip(archiveFile, destDir);
        } else if (fileName.endsWith(".rar")) {
            extractRar(archiveFile, destDir);
        } else if (fileName.endsWith(".tar")) {
            extractTar(archiveFile, destDir);
        } else if (fileName.endsWith(".7z")) {
            extract7z(archiveFile, destDir);
        } else {
            throw new IOException("Unsupported archive format.");
        }
    }

    private static void extractZip(File zipFile, File destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
        }
    }
    
    private static void extractRar(File rarFile, File destDir) throws IOException {
        try (Archive archive = new Archive(rarFile)) {
            FileHeader fh;
            while ((fh = archive.nextFileHeader()) != null) {
                File newFile = newFile(destDir, fh.getFileNameString());
                if (fh.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        archive.extractFile(fh, fos);
                    }
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to extract RAR file.", e);
        }
    }

    private static void extractTar(File tarFile, File destDir) throws IOException {
        try (TarArchiveInputStream tis = new TarArchiveInputStream(new FileInputStream(tarFile))) {
            TarArchiveEntry entry;
            while ((entry = (TarArchiveEntry) tis.getNextEntry()) != null) {
                File newFile = newFile(destDir, entry.getName());
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = tis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }

    private static void extract7z(File sevenZFile, File destDir) throws IOException {
        try (SevenZFile szf = new SevenZFile(sevenZFile)) {
            SevenZArchiveEntry entry;
            while ((entry = szf.getNextEntry()) != null) {
                File newFile = newFile(destDir, entry.getName());
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] content = new byte[(int) entry.getSize()];
                        szf.read(content, 0, content.length);
                        fos.write(content);
                    }
                }
            }
        }
    }

    public static void extractDefaultTextures(File apkFile, File destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(apkFile))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (zipEntry.getName().startsWith("assets/") || zipEntry.getName().startsWith("res/")) {
                    File newFile = newFile(destDir, zipEntry.getName());
                    if (zipEntry.isDirectory()) {
                        newFile.mkdirs();
                    } else {
                        new File(newFile.getParent()).mkdirs();
                        try (FileOutputStream fos = new FileOutputStream(newFile)) {
                            byte[] buffer = new byte[1024];
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

    private static File newFile(File destinationDir, String zipEntryName) throws IOException {
        File destFile = new File(destinationDir, zipEntryName);
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntryName);
        }
        return destFile;
    }
}