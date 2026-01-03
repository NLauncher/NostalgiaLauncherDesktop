package net.eqozqq.nostalgialauncherdesktop.Instances;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InstanceManager {
    private static final String DEFAULT_INSTANCE = "Default Instance";
    private static final String INSTANCES_DIR = "instances";

    private static InstanceManager instance;
    private String activeInstance = DEFAULT_INSTANCE;
    private Properties settings;

    private InstanceManager() {}

    public static synchronized InstanceManager getInstance() {
        if (instance == null) {
            instance = new InstanceManager();
        }
        return instance;
    }

    public void init(Properties settings) {
        this.settings = settings;
        String selected = settings.getProperty("selectedInstance");
        if (selected != null && !selected.trim().isEmpty()) {
            activeInstance = selected;
        } else {
            activeInstance = DEFAULT_INSTANCE;
        }
        File root = new File(INSTANCES_DIR);
        if (!root.exists()) {
            root.mkdirs();
        }
    }

    public String getActiveInstance() {
        return activeInstance;
    }

    public void setActiveInstance(String name) {
        if (name == null || name.trim().isEmpty()) {
            name = DEFAULT_INSTANCE;
        }
        this.activeInstance = name;
        if (settings != null) {
            settings.setProperty("selectedInstance", activeInstance);
        }
    }

    public List<String> getInstances() {
        List<String> instances = new ArrayList<>();
        instances.add(DEFAULT_INSTANCE);
        File root = getInstancesRoot();
        File[] files = root.listFiles();
        if (files != null) {
            Arrays.stream(files)
                .filter(File::isDirectory)
                .map(File::getName)
                .filter(name -> !name.equals(DEFAULT_INSTANCE))
                .forEach(instances::add);
        }
        return instances;
    }

    public void createInstance(String name) {
        if (name == null || name.trim().isEmpty() || DEFAULT_INSTANCE.equals(name.trim())) {
            throw new IllegalArgumentException("Invalid instance name.");
        }
        File root = getInstancesRoot();
        File newInstanceDir = new File(root, name.trim());
        if (newInstanceDir.exists()) {
            throw new IllegalArgumentException("Instance already exists: " + name);
        }
        if (!newInstanceDir.mkdirs()) {
             throw new RuntimeException("Failed to create instance directory: " + name);
        }
    }

    public void renameInstance(String oldName, String newName) {
        if (oldName == null || oldName.trim().isEmpty() || DEFAULT_INSTANCE.equals(oldName.trim())) {
            throw new IllegalArgumentException("Cannot rename the default instance.");
        }
        String cleanOld = oldName.trim();
        String cleanNew = newName.trim();
        
        if (cleanNew.isEmpty()) {
             throw new IllegalArgumentException("New name cannot be empty.");
        }
        if (cleanNew.equals(DEFAULT_INSTANCE)) {
             throw new IllegalArgumentException("Cannot rename to the default instance name.");
        }

        File root = getInstancesRoot();
        File oldDir = new File(root, cleanOld);
        File newDir = new File(root, cleanNew);

        if (!oldDir.exists()) {
            throw new IllegalArgumentException("Instance not found: " + cleanOld);
        }
        if (newDir.exists()) {
            throw new IllegalArgumentException("Instance already exists: " + cleanNew);
        }

        if (oldDir.renameTo(newDir)) {
            if (activeInstance.equals(cleanOld)) {
                setActiveInstance(cleanNew);
            }
        } else {
            throw new RuntimeException("Failed to rename instance directory.");
        }
    }

    public void deleteInstance(String name) {
        if (name == null || name.trim().isEmpty() || DEFAULT_INSTANCE.equals(name.trim())) {
            throw new IllegalArgumentException("Cannot delete the default instance.");
        }
        File instanceDir = new File(INSTANCES_DIR, name.trim());
        if (instanceDir.exists() && instanceDir.isDirectory()) {
            try {
                deleteRecursive(instanceDir.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete instance: " + name, e);
            }
            if (activeInstance.equals(name.trim())) {
                setActiveInstance(DEFAULT_INSTANCE);
            }
        }
    }
    
    private static void deleteRecursive(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (Stream<Path> entries = Files.list(path)) {
                for (Path entry : entries.collect(Collectors.toList())) {
                    deleteRecursive(entry);
                }
            }
        }
        Files.delete(path);
    }

    public boolean isDefault() {
        return DEFAULT_INSTANCE.equals(activeInstance);
    }

    public String resolvePath(String relative) {
        if (relative == null || relative.isEmpty()) {
            return baseDir();
        }
        if (isDefault()) {
            return relative;
        }
        return INSTANCES_DIR + File.separator + activeInstance + File.separator + relative.replace("/", File.separator);
    }

    public String baseDir() {
        if (isDefault()) {
            return ".";
        }
        return INSTANCES_DIR + File.separator + activeInstance;
    }

    public File ensureDir(String relative) {
        File dir = new File(resolvePath(relative));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static String getDefaultInstanceName() {
        return DEFAULT_INSTANCE;
    }

    public static File getInstancesRoot() {
        File root = new File(INSTANCES_DIR);
        if (!root.exists()) {
            root.mkdirs();
        }
        return root;
    }
}