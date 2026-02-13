package net.eqozqq.nostalgialauncherdesktop.WorldManager;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.io.*;
import java.nio.file.Paths;

public class WorldImporter {

    public static void importWorld(File archiveFile, File targetWorldsDir) throws Exception {
        String fileName = archiveFile.getName().toLowerCase();
        if (fileName.endsWith(".7z")) {
            import7z(archiveFile, targetWorldsDir);
        } else {
            importStandardArchive(archiveFile, targetWorldsDir);
        }
    }

    private static void importStandardArchive(File archiveFile, File targetWorldsDir) throws Exception {
        String worldPath = findLevelDatPathStandard(archiveFile);
        String targetFolderName = worldPath.isEmpty()
                ? archiveFile.getName().substring(0, archiveFile.getName().lastIndexOf('.'))
                : Paths.get(worldPath).getFileName().toString();

        File finalTargetDir = new File(targetWorldsDir, targetFolderName);
        if (!finalTargetDir.exists())
            finalTargetDir.mkdirs();

        try (InputStream is = new BufferedInputStream(new FileInputStream(archiveFile))) {
            InputStream wrappedIs = is;
            if (archiveFile.getName().toLowerCase().endsWith(".gz")
                    || archiveFile.getName().toLowerCase().endsWith(".tgz")) {
                wrappedIs = new GzipCompressorInputStream(is);
            }

            try (ArchiveInputStream ais = new ArchiveStreamFactory()
                    .createArchiveInputStream(new BufferedInputStream(wrappedIs))) {
                ArchiveEntry entry;
                while ((entry = ais.getNextEntry()) != null) {
                    String name = entry.getName().replace("\\", "/");
                    if (name.startsWith(worldPath)) {
                        String relativeName = name.substring(worldPath.length());
                        if (relativeName.startsWith("/"))
                            relativeName = relativeName.substring(1);
                        if (relativeName.isEmpty())
                            continue;

                        File outFile = new File(finalTargetDir, relativeName);
                        if (entry.isDirectory()) {
                            outFile.mkdirs();
                        } else {
                            outFile.getParentFile().mkdirs();
                            try (OutputStream os = new FileOutputStream(outFile)) {
                                IOUtils.copy(ais, os);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void import7z(File archiveFile, File targetWorldsDir) throws Exception {
        String worldPath = findLevelDatPath7z(archiveFile);
        String targetFolderName = worldPath.isEmpty()
                ? archiveFile.getName().substring(0, archiveFile.getName().lastIndexOf('.'))
                : Paths.get(worldPath).getFileName().toString();

        File finalTargetDir = new File(targetWorldsDir, targetFolderName);
        if (!finalTargetDir.exists())
            finalTargetDir.mkdirs();

        try (SevenZFile sevenZFile = new SevenZFile(archiveFile)) {
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                String name = entry.getName().replace("\\", "/");
                if (name.startsWith(worldPath)) {
                    String relativeName = name.substring(worldPath.length());
                    if (relativeName.startsWith("/"))
                        relativeName = relativeName.substring(1);
                    if (relativeName.isEmpty())
                        continue;

                    File outFile = new File(finalTargetDir, relativeName);
                    if (entry.isDirectory()) {
                        outFile.mkdirs();
                    } else {
                        outFile.getParentFile().mkdirs();
                        try (OutputStream os = new FileOutputStream(outFile)) {
                            byte[] buffer = new byte[8192];
                            int len;
                            while ((len = sevenZFile.read(buffer)) > 0) {
                                os.write(buffer, 0, len);
                            }
                        }
                    }
                }
            }
        }
    }

    private static String findLevelDatPathStandard(File archiveFile) throws Exception {
        try (InputStream is = new BufferedInputStream(new FileInputStream(archiveFile))) {
            InputStream wrappedIs = is;
            if (archiveFile.getName().toLowerCase().endsWith(".gz")
                    || archiveFile.getName().toLowerCase().endsWith(".tgz")) {
                wrappedIs = new GzipCompressorInputStream(is);
            }

            try (ArchiveInputStream ais = new ArchiveStreamFactory()
                    .createArchiveInputStream(new BufferedInputStream(wrappedIs))) {
                ArchiveEntry entry;
                while ((entry = ais.getNextEntry()) != null) {
                    String name = entry.getName().replace("\\", "/");
                    if (name.endsWith("level.dat")) {
                        String path = name.substring(0, name.length() - "level.dat".length());
                        if (path.endsWith("/"))
                            path = path.substring(0, path.length() - 1);
                        return path;
                    }
                }
            }
        }
        return "";
    }

    private static String findLevelDatPath7z(File archiveFile) throws Exception {
        try (SevenZFile sevenZFile = new SevenZFile(archiveFile)) {
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                String name = entry.getName().replace("\\", "/");
                if (name.endsWith("level.dat")) {
                    String path = name.substring(0, name.length() - "level.dat".length());
                    if (path.endsWith("/"))
                        path = path.substring(0, path.length() - 1);
                    return path;
                }
            }
        }
        return "";
    }
}