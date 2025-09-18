package net.eqozqq.nostalgialauncherdesktop.Instances;

import java.io.File;
import java.util.Properties;

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
