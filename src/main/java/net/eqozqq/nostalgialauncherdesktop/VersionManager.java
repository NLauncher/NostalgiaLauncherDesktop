package net.eqozqq.nostalgialauncherdesktop;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class VersionManager {
    private static final String VERSIONS_CACHE_DIR = "cache" + File.separator + "versions";
    private static final String GAME_DIR = "game";
    private static final String VERSIONS_DIR = "versions";

    private Set<String> installedVersions;

    public VersionManager() {
        updateInstalledVersions();
    }

    public List<Version> loadVersions(String source) throws IOException {
        try {
            if (source == null || source.isEmpty()) {
                throw new IOException("Versions source is not specified.");
            }
            if (source.startsWith("http://") || source.startsWith("https://")) {
                try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    HttpGet request = new HttpGet(source);
                    try (CloseableHttpResponse response = httpClient.execute(request)) {
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            Gson gson = new Gson();
                            Type listType = new TypeToken<List<Version>>(){}.getType();
                            return gson.fromJson(new InputStreamReader(entity.getContent()), listType);
                        }
                    }
                }
            } else {
                File file = new File(source);
                if (file.exists() && file.isFile()) {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Version>>(){}.getType();
                    return gson.fromJson(new InputStreamReader(Files.newInputStream(file.toPath())), listType);
                } else {
                    throw new IOException("File not found or is not a file: " + source);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load versions from source: " + source);
            e.printStackTrace();
            throw new IOException("Failed to load versions: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    public void updateInstalledVersions() {
        Path versionsPath = Paths.get(VERSIONS_DIR);
        if (Files.exists(versionsPath)) {
            try (Stream<Path> paths = Files.list(versionsPath)) {
                installedVersions = paths.filter(Files::isDirectory)
                                         .map(path -> path.getFileName().toString())
                                         .collect(Collectors.toSet());
            } catch (IOException e) {
                System.err.println("Error listing installed versions: " + e.getMessage());
                installedVersions = Collections.emptySet();
            }
        } else {
            installedVersions = Collections.emptySet();
        }
    }

    public boolean isVersionInstalled(Version version) {
        if (installedVersions == null) {
            updateInstalledVersions();
        }
        return installedVersions.contains(version.getName());
    }

    public File downloadVersion(Version version, ProgressCallback callback) throws IOException {
        File versionsCacheDir = new File(VERSIONS_CACHE_DIR);
        if (!versionsCacheDir.exists()) {
            boolean created = versionsCacheDir.mkdirs();
            if (!created) {
                throw new IOException("Failed to create cache directory: " + versionsCacheDir.getAbsolutePath());
            }
        }

        String fileName = version.getName() + ".apk";
        File outputFile = new File(versionsCacheDir, fileName);

        if (outputFile.exists() && outputFile.length() > 0) {
            return outputFile;
        }

        String downloadUrl = version.getUrl();
        if (downloadUrl == null || downloadUrl.isEmpty()) {
            throw new IOException("Download URL is missing for version: " + version.getName());
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(downloadUrl);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    long totalSize = entity.getContentLength();
                    try (java.io.InputStream inputStream = entity.getContent();
                         OutputStream outputStream = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        long totalBytesRead = 0;

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                            if (callback != null && totalSize > 0) {
                                callback.onProgress((double) totalBytesRead / totalSize);
                            }
                        }
                    }
                }
            }
        }
        
        if (!outputFile.exists() || outputFile.length() == 0) {
            throw new IOException("Failed to download version file: " + outputFile.getAbsolutePath());
        }
        
        return outputFile;
    }

    public void extractVersion(File apkFile, File gameDir) throws IOException {
        if (!apkFile.exists()) {
            throw new IOException("APK file does not exist: " + apkFile.getAbsolutePath());
        }

        File versionsDir = new File(VERSIONS_DIR);
        if (!versionsDir.exists()) {
            boolean created = versionsDir.mkdirs();
            if (!created) {
                throw new IOException("Failed to create versions directory: " + versionsDir.getAbsolutePath());
            }
        }

        String versionName = apkFile.getName().replace(".apk", "");
        File targetDir = new File(versionsDir, versionName);
        if (!targetDir.exists()) {
            boolean created = targetDir.mkdirs();
            if (!created) {
                throw new IOException("Failed to create target directory: " + targetDir.getAbsolutePath());
            }
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(apkFile.toPath()))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                
                String entryName = entry.getName();
                
                if (entryName.startsWith("assets/") || entryName.startsWith("res/") || entryName.startsWith("lib/")) {
                    File newFile = new File(targetDir, entryName);
                    
                    File parentDir = newFile.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        boolean created = parentDir.mkdirs();
                        if (!created) {
                            System.err.println("Failed to create directory: " + parentDir.getAbsolutePath());
                            continue;
                        }
                    }
                    
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                }
                zipInputStream.closeEntry();
            }
        }
    }

    public void prepareGameDir(Version version, File gameDir) throws IOException {
        File versionsDir = new File(VERSIONS_DIR);
        File currentVersionDir = new File(versionsDir, version.getName());

        if (!currentVersionDir.exists()) {
            throw new IOException("Version directory does not exist: " + currentVersionDir.getAbsolutePath());
        }

        File assetsDir = new File(gameDir, "assets");
        if (assetsDir.exists()) {
            try {
                FileUtils.deleteDirectory(assetsDir);
            } catch (IOException e) {
                System.err.println("Failed to delete assets directory: " + e.getMessage());
            }
        }
        File libDir = new File(gameDir, "lib");
        if (libDir.exists()) {
            try {
                FileUtils.deleteDirectory(libDir);
            } catch (IOException e) {
                System.err.println("Failed to delete lib directory: " + e.getMessage());
            }
        }
        File resDir = new File(gameDir, "res");
        if (resDir.exists()) {
            try {
                FileUtils.deleteDirectory(resDir);
            } catch (IOException e) {
                System.err.println("Failed to delete res directory: " + e.getMessage());
            }
        }

        File currentVersionAssets = new File(currentVersionDir, "assets");
        if (currentVersionAssets.exists()) {
            try {
                FileUtils.copyDirectory(currentVersionAssets, assetsDir);
            } catch (IOException e) {
                System.err.println("Failed to copy assets directory: " + e.getMessage());
            }
        }
        File currentVersionLib = new File(currentVersionDir, "lib");
        if (currentVersionLib.exists()) {
            try {
                FileUtils.copyDirectory(currentVersionLib, libDir);
            } catch (IOException e) {
                System.err.println("Failed to copy lib directory: " + e.getMessage());
            }
        }
        File currentVersionRes = new File(currentVersionDir, "res");
        if (currentVersionRes.exists()) {
            try {
                FileUtils.copyDirectory(currentVersionRes, resDir);
            } catch (IOException e) {
                System.err.println("Failed to copy res directory: " + e.getMessage());
            }
        }
    }
}