package net.eqozqq.nostalgialauncherdesktop;

import java.util.List;
import java.util.ArrayList;

public class CustomLauncherProfile {
    private String name;
    private String executablePath;
    private List<String> requiredPaths = new ArrayList<>();
    private String customWorldsPath;
    private String customTexturesPath;
    private String customOptionsPath;

    public CustomLauncherProfile() {
    }

    public CustomLauncherProfile(String name, String executablePath, List<String> requiredPaths) {
        this.name = name;
        this.executablePath = executablePath;
        this.requiredPaths = requiredPaths != null ? requiredPaths : new ArrayList<>();
    }

    public CustomLauncherProfile(String name, String executablePath, List<String> requiredPaths, String customWorldsPath, String customTexturesPath) {
        this.name = name;
        this.executablePath = executablePath;
        this.requiredPaths = requiredPaths != null ? requiredPaths : new ArrayList<>();
        this.customWorldsPath = customWorldsPath;
        this.customTexturesPath = customTexturesPath;
    }

    public CustomLauncherProfile(String name, String executablePath, List<String> requiredPaths, String customWorldsPath, String customTexturesPath, String customOptionsPath) {
        this.name = name;
        this.executablePath = executablePath;
        this.requiredPaths = requiredPaths != null ? requiredPaths : new ArrayList<>();
        this.customWorldsPath = customWorldsPath;
        this.customTexturesPath = customTexturesPath;
        this.customOptionsPath = customOptionsPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public List<String> getRequiredPaths() {
        return requiredPaths;
    }

    public void setRequiredPaths(List<String> requiredPaths) {
        this.requiredPaths = requiredPaths;
    }

    public String getCustomWorldsPath() {
        return customWorldsPath;
    }

    public void setCustomWorldsPath(String customWorldsPath) {
        this.customWorldsPath = customWorldsPath;
    }

    public String getCustomTexturesPath() {
        return customTexturesPath;
    }

    public void setCustomTexturesPath(String customTexturesPath) {
        this.customTexturesPath = customTexturesPath;
    }

    public String getCustomOptionsPath() {
        return customOptionsPath;
    }

    public void setCustomOptionsPath(String customOptionsPath) {
        this.customOptionsPath = customOptionsPath;
    }

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
