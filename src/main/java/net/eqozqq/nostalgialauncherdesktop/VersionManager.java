package net.eqozqq.nostalgialauncherdesktop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.eqozqq.nostalgialauncherdesktop.Instances.InstanceManager;

public class VersionManager {
    private static final String VERSIONS_CACHE_DIR = "cache" + File.separator + "versions";
    private static final String GAME_DIR = "game";
    private static final String VERSIONS_DIR = "versions";
    private static final String CUSTOM_VERSIONS_FILE = "custom_versions.json";

    private Set<String> installedVersions;

    public VersionManager() {
        updateInstalledVersions();
    }

    public List<Version> loadVersions(String source) throws IOException {
        List<Version> versions = new ArrayList<>();
        try {
            if (source != null && !source.isEmpty()) {
                if (source.startsWith("http://") || source.startsWith("https://")) {
                    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                        HttpGet request = new HttpGet(source);
                        try (CloseableHttpResponse response = httpClient.execute(request)) {
                            HttpEntity entity = response.getEntity();
                            if (entity != null) {
                                Gson gson = new Gson();
                                Type listType = new TypeToken<List<Version>>(){}.getType();
                                versions.addAll(gson.fromJson(new InputStreamReader(entity.getContent()), listType));
                            }
                        }
                    }
                } else {
                    File file = new File(source);
                    if (file.exists() && file.isFile()) {
                        Gson gson = new Gson();
                        Type listType = new TypeToken<List<Version>>(){}.getType();
                        versions.addAll(gson.fromJson(new InputStreamReader(Files.newInputStream(file.toPath())), listType));
                    } else {
                        throw new IOException("versionManager.error.fileNotFound:" + source);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("versionManager.error.loadVersionsGeneric:" + e.getMessage(), e);
        }

        versions.addAll(loadCustomVersions());
        return versions;
    }

    private List<Version> loadCustomVersions() {
        File customVersionsFile = new File(InstanceManager.getInstance().resolvePath(CUSTOM_VERSIONS_FILE));
        if (customVersionsFile.exists()) {
            try (FileReader reader = new FileReader(customVersionsFile)) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Version>>(){}.getType();
                List<Version> customVersions = gson.fromJson(reader, listType);
                if (customVersions != null) {
                    return customVersions;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyList();
    }

    public void addAndSaveCustomVersion(Version version) throws IOException {
        List<Version> customVersions = new ArrayList<>(loadCustomVersions());
        customVersions.add(version);
        File targetFile = new File(InstanceManager.getInstance().resolvePath(CUSTOM_VERSIONS_FILE));
        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        try (Writer writer = new FileWriter(targetFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(customVersions, writer);
        } catch (IOException e) {
            throw e;
        }
    }

    public void updateInstalledVersions() {
        Path versionsPath = Paths.get(InstanceManager.getInstance().resolvePath(VERSIONS_DIR));
        if (Files.exists(versionsPath)) {
            try (Stream<Path> paths = Files.list(versionsPath)) {
                installedVersions = paths.filter(Files::isDirectory)
                                         .map(path -> path.getFileName().toString())
                                         .collect(Collectors.toSet());
            } catch (IOException e) {
                e.printStackTrace();
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
        File versionsCacheDir = new File(InstanceManager.getInstance().resolvePath(VERSIONS_CACHE_DIR));
        if (!versionsCacheDir.exists()) {
            if (!versionsCacheDir.mkdirs()) {
                throw new IOException("versionManager.error.createCacheDirFailed:" + versionsCacheDir.getAbsolutePath());
            }
        }

        String fileName = version.getName() + ".apk";
        File outputFile = new File(versionsCacheDir, fileName);

        if (outputFile.exists() && outputFile.length() > 0) {
            return outputFile;
        }

        String downloadUrl = version.getUrl();
        if (downloadUrl == null || downloadUrl.isEmpty()) {
            throw new IOException("versionManager.error.missingUrl:" + version.getName());
        }

        if (downloadUrl.startsWith("file:")) {
             File sourceFile = new File(URI.create(downloadUrl));
             if(sourceFile.exists()) {
                 FileUtils.copyFile(sourceFile, outputFile);
                 return outputFile;
             } else {
                 throw new IOException("versionManager.error.customVersionNotFound:" + sourceFile.getAbsolutePath());
             }
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
            throw new IOException("versionManager.error.downloadFailed:" + outputFile.getAbsolutePath());
        }
        
        return outputFile;
    }

    public void extractVersion(File apkFile, File gameDir) throws IOException {
        if (!apkFile.exists()) {
            throw new IOException("versionManager.error.apkNotFound:" + apkFile.getAbsolutePath());
        }

        File versionsDir = new File(InstanceManager.getInstance().resolvePath(VERSIONS_DIR));
        if (!versionsDir.exists()) {
            if (!versionsDir.mkdirs()) {
                throw new IOException("versionManager.error.createVersionsDirFailed:" + versionsDir.getAbsolutePath());
            }
        }

        String versionName = apkFile.getName().replace(".apk", "");
        File targetDir = new File(versionsDir, versionName);
        if (!targetDir.exists()) {
            if (!targetDir.mkdirs()) {
                throw new IOException("versionManager.error.createTargetDirFailed:" + targetDir.getAbsolutePath());
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
                        if (!parentDir.mkdirs()) {
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
        File versionsDir = new File(InstanceManager.getInstance().resolvePath(VERSIONS_DIR));
        File currentVersionDir = new File(versionsDir, version.getName());

        if (!currentVersionDir.exists()) {
            throw new IOException("versionManager.error.versionDirNotFound:" + currentVersionDir.getAbsolutePath());
        }

        File assetsDir = new File(gameDir, "assets");
        if (assetsDir.exists()) {
            FileUtils.deleteDirectory(assetsDir);
        }
        File libDir = new File(gameDir, "lib");
        if (libDir.exists()) {
            FileUtils.deleteDirectory(libDir);
        }
        File resDir = new File(gameDir, "res");
        if (resDir.exists()) {
            FileUtils.deleteDirectory(resDir);
        }

        File currentVersionAssets = new File(currentVersionDir, "assets");
        if (currentVersionAssets.exists()) {
            FileUtils.copyDirectory(currentVersionAssets, assetsDir);
        }
        File currentVersionLib = new File(currentVersionDir, "lib");
        if (currentVersionLib.exists()) {
            FileUtils.copyDirectory(currentVersionLib, libDir);
        }
        File currentVersionRes = new File(currentVersionDir, "res");
        if (currentVersionRes.exists()) {
            FileUtils.copyDirectory(currentVersionRes, resDir);
        }
    }
}