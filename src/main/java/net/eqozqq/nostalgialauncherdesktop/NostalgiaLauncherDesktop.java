package net.eqozqq.nostalgialauncherdesktop;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.util.SystemInfo;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import net.eqozqq.nostalgialauncherdesktop.WorldManager.WorldsManagerPanel;
import net.eqozqq.nostalgialauncherdesktop.TexturesManager.TexturesManagerPanel;
import net.eqozqq.nostalgialauncherdesktop.Instances.InstancesPanel;
import net.eqozqq.nostalgialauncherdesktop.Instances.InstanceManager;
import net.eqozqq.nostalgialauncherdesktop.Proxy.ProxyPanel;
import net.eqozqq.nostalgialauncherdesktop.Proxy.ProxyManager;
import net.eqozqq.nostalgialauncherdesktop.marketplace.MarketplacePanel;

public class NostalgiaLauncherDesktop extends JFrame {
    private JTextField nicknameField;
    private JComboBox<Version> versionComboBox;
    private JButton launchButton;
    private JButton refreshButton;
    private JButton addVersionButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    private NavigationPanel navigationPanel;
    private HomePanel homePanel;
    private WorldsManagerPanel worldsPanel;
    private TexturesManagerPanel texturesPanel;
    private InstancesPanel instancesPanel;
    private MarketplacePanel marketplacePanel;
    private ProxyPanel proxyPanel;
    private SettingsPanel settingsPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private BufferedImage backgroundImage;
    private Color customBackgroundColor;

    private VersionManager versionManager;
    private GameLauncher gameLauncher;
    private Properties settings;
    private LocaleManager localeManager;
    private LoadingOverlay loadingOverlay;

    private String customBackgroundPath;
    private String customVersionsSource;
    private boolean useDefaultVersionsSource;
    private List<String> customVersionsSourcesList = new ArrayList<>();

    private String executableSource;
    private String customLauncherPath;
    private boolean useDefaultLauncher;
    private List<String> customLauncherPathsList = new ArrayList<>();
    private List<CustomLauncherProfile> customLauncherProfilesList = new ArrayList<>();
    private String selectedLauncherProfileName;

    private String postLaunchAction;
    private boolean enableDebugging;
    private boolean enableDiscordIntegration;
    private String lastPlayedVersionName;
    private double scaleFactor;
    private String themeName;
    private String backgroundMode;
    private String customTranslationPath;

    private String githubTranslationUrl;
    private String githubTranslationName;

    private SwingWorker<Void, Integer> launchWorker;

    private static String CURRENT_VERSION = "1.10.1";

    private static NostalgiaLauncherDesktop instance;

    public static NostalgiaLauncherDesktop getInstance() {
        return instance;
    }

    public CustomLauncherProfile getActiveCustomLauncherProfile() {
        if (!"ANOTHER".equals(executableSource)) {
            return null;
        }

        if (versionComboBox != null) {
            Version selected = (Version) versionComboBox.getSelectedItem();
            if (selected != null) {
                String name = selected.getName();
                for (CustomLauncherProfile p : customLauncherProfilesList) {
                    if (p.getName().equals(name)) {
                        return p;
                    }
                }
            }
        }

        if (selectedLauncherProfileName != null) {
            for (CustomLauncherProfile p : customLauncherProfilesList) {
                if (p.getName().equals(selectedLauncherProfileName)) {
                    return p;
                }
            }
        }

        if (customLauncherProfilesList != null && !customLauncherProfilesList.isEmpty()) {
            return customLauncherProfilesList.get(0);
        }

        return null;
    }

    public File getWorldsDirectory() {
        CustomLauncherProfile profile = getActiveCustomLauncherProfile();
        if (profile != null && profile.getCustomWorldsPath() != null
                && !profile.getCustomWorldsPath().trim().isEmpty()) {
            return new File(profile.getCustomWorldsPath().trim());
        }
        return new File(InstanceManager.getInstance().resolvePath("game/storage/games/com.mojang/minecraftWorlds/"));
    }

    public File getTexturesDirectory() {
        CustomLauncherProfile profile = getActiveCustomLauncherProfile();
        if (profile != null && profile.getCustomTexturesPath() != null
                && !profile.getCustomTexturesPath().trim().isEmpty()) {
            return new File(profile.getCustomTexturesPath().trim());
        }
        return new File(InstanceManager.getDataRoot(), "textures");
    }

    public File getOptionsFile() {
        CustomLauncherProfile profile = getActiveCustomLauncherProfile();
        if (profile != null && profile.getCustomOptionsPath() != null
                && !profile.getCustomOptionsPath().trim().isEmpty()) {
            return new File(profile.getCustomOptionsPath().trim());
        }
        return new File(
                InstanceManager.getInstance().resolvePath("game/storage/games/com.mojang/minecraftpe/options.txt"));
    }

    private static final int COMPONENT_WIDTH = 300;
    private static final String DEFAULT_VERSIONS_URL = "https://raw.githubusercontent.com/NLauncher/components/main/versions.json";
    private static final String DEFAULT_LAUNCHER_URL_WINDOWS = "https://github.com/NLauncher/components/raw/main/ninecraft-windows.zip";
    private static final String DEFAULT_LAUNCHER_URL_LINUX = "https://github.com/NLauncher/components/raw/main/ninecraft-linux.zip";

    public static void openURL(String url) {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("nix") || os.contains("nux")) {
                new ProcessBuilder("xdg-open", url).start();
                return;
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", url).start();
                return;
            } else if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[] { "rundll32", "url.dll,FileProtocolHandler", url });
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (java.awt.Desktop.isDesktopSupported()
                    && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openFolder(File folder) {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("nix") || os.contains("nux")) {
                new ProcessBuilder("xdg-open", folder.getAbsolutePath()).start();
                return;
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", folder.getAbsolutePath()).start();
                return;
            } else if (os.contains("win")) {
                new ProcessBuilder("explorer.exe", folder.getAbsolutePath()).start();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.OPEN)) {
                java.awt.Desktop.getDesktop().open(folder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public NostalgiaLauncherDesktop() {
        instance = this;
        javax.imageio.ImageIO.setUseCache(false);
        versionManager = new VersionManager();
        gameLauncher = new GameLauncher();
        settings = new Properties();
        localeManager = LocaleManager.getInstance();
        loadSettings();
        localeManager.init(settings);
        CURRENT_VERSION = localeManager.get("launcher.version", "1.10.1");
        InstanceManager.getInstance().init(settings);
        applyTheme();
        loadBackground();
        loadingOverlay = new LoadingOverlay();
        initializeUI();
        loadVersions();
        loadNickname();
        setIcon();
        setupLinuxIntegration();
        setGlassPane(loadingOverlay);
        showFirstLaunchDisclaimer();
        if (enableDiscordIntegration) {
            DiscordRPCManager.getInstance().init();
        }
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                DiscordRPCManager.getInstance().shutdown();
            }
        });

        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event instanceof java.awt.event.MouseEvent) {
                java.awt.event.MouseEvent me = (java.awt.event.MouseEvent) event;
                if (me.getID() == java.awt.event.MouseEvent.MOUSE_PRESSED) {
                    java.awt.Component target = me.getComponent();
                    if (target == null)
                        return;
                    if (target instanceof JButton)
                        return;
                    if (target instanceof JTextField)
                        return;
                    if (target instanceof JTextArea)
                        return;
                    if (target instanceof JComboBox)
                        return;
                    if (target instanceof JCheckBox)
                        return;
                    if (target instanceof JRadioButton)
                        return;
                    if (target instanceof JScrollBar)
                        return;
                    if (target instanceof JSlider)
                        return;
                    if (target instanceof JList) {
                        JList<?> list = (JList<?>) target;
                        int index = list.locationToIndex(me.getPoint());
                        if (index >= 0) {
                            Rectangle bounds = list.getCellBounds(index, index);
                            if (bounds == null || !bounds.contains(me.getPoint())) {
                                SwingUtilities.invokeLater(list::clearSelection);
                            }
                        } else {
                            SwingUtilities.invokeLater(list::clearSelection);
                        }
                        return;
                    }
                    java.awt.Component check = target;
                    while (check != null) {
                        if (check instanceof JScrollPane) {
                            java.awt.Component view = ((JScrollPane) check).getViewport().getView();
                            if (view instanceof JList) {
                                JList<?> list = (JList<?>) view;
                                java.awt.Point p = SwingUtilities.convertPoint(target, me.getPoint(), list);
                                int index = list.locationToIndex(p);
                                if (index >= 0) {
                                    Rectangle bounds = list.getCellBounds(index, index);
                                    if (bounds == null || !bounds.contains(p)) {
                                        SwingUtilities.invokeLater(list::clearSelection);
                                    }
                                } else {
                                    SwingUtilities.invokeLater(list::clearSelection);
                                }
                                return;
                            }
                        }
                        check = check.getParent();
                    }
                    SwingUtilities.invokeLater(() -> java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager()
                            .clearGlobalFocusOwner());
                }
            }
        }, java.awt.AWTEvent.MOUSE_EVENT_MASK);
    }

    private void setIcon() {
        try (InputStream iconStream = NostalgiaLauncherDesktop.class.getResourceAsStream("/app_icon.jpg")) {
            if (iconStream != null) {
                Image iconImage = ImageIO.read(iconStream);
                if (iconImage != null) {
                    setIconImage(iconImage);
                    try {
                        Class<?> taskbarClass = Class.forName("java.awt.Taskbar");
                        java.lang.reflect.Method isTaskbarSupported = taskbarClass.getMethod("isTaskbarSupported");
                        boolean supported = (boolean) isTaskbarSupported.invoke(null);
                        if (supported) {
                            java.lang.reflect.Method getTaskbar = taskbarClass.getMethod("getTaskbar");
                            Object taskbar = getTaskbar.invoke(null);
                            if (taskbar != null) {
                                java.lang.reflect.Method setIconImage = taskbarClass.getMethod("setIconImage",
                                        Image.class);
                                setIconImage.invoke(taskbar, iconImage);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupLinuxIntegration() {
        if (!SystemInfo.isLinux)
            return;
        new Thread(() -> {
            try {
                String userHome = System.getProperty("user.home");
                File iconsDir = new File(userHome, ".local/share/icons");
                File appsDir = new File(userHome, ".local/share/applications");
                if (!iconsDir.exists())
                    iconsDir.mkdirs();
                if (!appsDir.exists())
                    appsDir.mkdirs();
                File iconFile = new File(iconsDir, "nostalgialauncher.jpg");
                File desktopFile = new File(appsDir, "nostalgialauncher.desktop");
                try (InputStream is = getClass().getResourceAsStream("/app_icon.jpg")) {
                    if (is != null)
                        Files.copy(is, iconFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                String jarPath = new File(
                        NostalgiaLauncherDesktop.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                        .getAbsolutePath();
                String desktopContent = "[Desktop Entry]\nType=Application\nName=NostalgiaLauncher\nComment=Minecraft Pocket Edition Alpha Launcher\nExec=java -jar \""
                        + jarPath + "\"\nIcon=" + iconFile.getAbsolutePath()
                        + "\nTerminal=false\nCategories=Game;\nStartupWMClass=net-eqozqq-nostalgialauncherdesktop-NostalgiaLauncherDesktop\n";
                Files.write(desktopFile.toPath(), desktopContent.getBytes());
                desktopFile.setExecutable(true);
            } catch (Exception e) {
                System.err.println("Failed to setup Linux integration: " + e.getMessage());
            }
        }).start();
    }

    private Font getMinecraftFont(int style, float size) {
        return FontManager.getRegularFont(style, size);
    }

    private Font getRegularFont(int style, float size) {
        return FontManager.getRegularFont(style, size);
    }

    private BufferedImage applyBlur(BufferedImage source) {
        if (source == null)
            return null;
        int radius = 15;
        int size = radius * 2 + 1;
        float[] data = new float[size * size];
        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0.0f;
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                float distance = x * x + y * y;
                int index = (y + radius) * size + (x + radius);
                data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
                total += data[index];
            }
        }
        for (int i = 0; i < data.length; i++)
            data[i] /= total;
        BufferedImage paddedSource = new BufferedImage(source.getWidth() + radius * 2, source.getHeight() + radius * 2,
                source.getType());
        Graphics2D g = paddedSource.createGraphics();
        g.drawImage(source, radius, radius, null);
        g.dispose();
        Kernel kernel = new Kernel(size, size, data);
        ConvolveOp convolveOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        BufferedImage blurredPadded = convolveOp.filter(paddedSource, null);
        return blurredPadded.getSubimage(radius, radius, source.getWidth(), source.getHeight());
    }

    private void loadBackground() {
        this.backgroundImage = null;
        this.customBackgroundColor = null;
        try {
            switch (backgroundMode) {
                case "Custom Image":
                    if (customBackgroundPath != null && new File(customBackgroundPath).exists()) {
                        BufferedImage sourceImage = ImageIO.read(new File(customBackgroundPath));
                        this.backgroundImage = applyBlur(sourceImage);
                    }
                    break;
                case "Custom Color":
                    String rgb = settings.getProperty("customBackgroundColor");
                    if (rgb != null)
                        this.customBackgroundColor = new Color(Integer.parseInt(rgb));
                    break;
                case "Default":
                default:
                    String backgroundPath = themeName.equals("Dark") ? "/background_night.png"
                            : "/background_light.png";
                    try (InputStream backgroundStream = NostalgiaLauncherDesktop.class
                            .getResourceAsStream(backgroundPath)) {
                        if (backgroundStream != null) {
                            BufferedImage sourceImage = ImageIO.read(backgroundStream);
                            this.backgroundImage = applyBlur(sourceImage);
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.backgroundImage = null;
            this.customBackgroundColor = null;
        }
    }

    private void loadSettings() {
        File settingsFile = new File(InstanceManager.getDataRoot(), "launcher.properties");
        try (FileInputStream fis = new FileInputStream(settingsFile)) {
            settings.load(fis);
            backgroundMode = settings.getProperty("backgroundMode", "Default");
            customBackgroundPath = settings.getProperty("customBackgroundPath");
            String rgb = settings.getProperty("customBackgroundColor");
            if (rgb != null) {
                try {
                    customBackgroundColor = new Color(Integer.parseInt(rgb));
                } catch (NumberFormatException e) {
                    customBackgroundColor = null;
                }
            }
            customVersionsSource = settings.getProperty("customVersionsSource");
            useDefaultVersionsSource = Boolean.parseBoolean(settings.getProperty("useDefaultVersionsSource", "true"));
            executableSource = settings.getProperty("executableSource", "SERVER");
            customLauncherPath = settings.getProperty("customLauncherPath");
            if (settings.containsKey("useDefaultLauncher")) {
                boolean useDefault = Boolean.parseBoolean(settings.getProperty("useDefaultLauncher"));
                if (!useDefault && customLauncherPath != null && !customLauncherPath.isEmpty())
                    executableSource = "CUSTOM";
            }
            postLaunchAction = settings.getProperty("postLaunchAction", "Do Nothing");
            enableDebugging = Boolean.parseBoolean(settings.getProperty("enableDebugging", "false"));
            enableDiscordIntegration = Boolean.parseBoolean(settings.getProperty("enableDiscordIntegration", "true"));
            lastPlayedVersionName = settings.getProperty("lastPlayedVersionName");
            scaleFactor = Double.parseDouble(settings.getProperty("scaleFactor", "1.3"));
            themeName = settings.getProperty("themeName", "Dark");
            customTranslationPath = settings.getProperty("customTranslationPath");
            githubTranslationUrl = settings.getProperty("githubTranslationUrl");
            githubTranslationName = settings.getProperty("githubTranslationName");
            String vsListJson = settings.getProperty("customVersionsSourcesList");
            if (vsListJson != null && !vsListJson.isEmpty()) {
                customVersionsSourcesList = new com.google.gson.Gson().fromJson(vsListJson,
                        new com.google.gson.reflect.TypeToken<List<String>>() {
                        }.getType());
            } else {
                customVersionsSourcesList = new ArrayList<>();
            }
            String clListJson = settings.getProperty("customLauncherPathsList");
            if (clListJson != null && !clListJson.isEmpty()) {
                customLauncherPathsList = new com.google.gson.Gson().fromJson(clListJson,
                        new com.google.gson.reflect.TypeToken<List<String>>() {
                        }.getType());
            } else {
                customLauncherPathsList = new ArrayList<>();
            }
            String cpListJson = settings.getProperty("customLauncherProfilesList");
            if (cpListJson != null && !cpListJson.isEmpty()) {
                customLauncherProfilesList = new com.google.gson.Gson().fromJson(cpListJson,
                        new com.google.gson.reflect.TypeToken<List<CustomLauncherProfile>>() {
                        }.getType());
            } else {
                customLauncherProfilesList = new ArrayList<>();
            }
            selectedLauncherProfileName = settings.getProperty("selectedLauncherProfileName");
        } catch (IOException | NumberFormatException e) {
            backgroundMode = "Default";
            useDefaultVersionsSource = true;
            executableSource = "SERVER";
            postLaunchAction = "Do Nothing";
            enableDebugging = false;
            enableDiscordIntegration = true;
            scaleFactor = 1.3;
            themeName = "Dark";
        }
    }

    private void saveSettings() {
        File settingsFile = new File(InstanceManager.getDataRoot(), "launcher.properties");
        try (FileOutputStream fos = new FileOutputStream(settingsFile)) {
            settings.setProperty("backgroundMode", backgroundMode);
            if (customBackgroundPath != null)
                settings.setProperty("customBackgroundPath", customBackgroundPath);
            else
                settings.remove("customBackgroundPath");
            if (customBackgroundColor != null)
                settings.setProperty("customBackgroundColor", String.valueOf(customBackgroundColor.getRGB()));
            else
                settings.remove("customBackgroundColor");
            if (customVersionsSource != null)
                settings.setProperty("customVersionsSource", customVersionsSource);
            settings.setProperty("useDefaultVersionsSource", String.valueOf(useDefaultVersionsSource));
            settings.setProperty("executableSource", executableSource);
            if (customLauncherPath != null)
                settings.setProperty("customLauncherPath", customLauncherPath);
            settings.setProperty("postLaunchAction", postLaunchAction);
            settings.setProperty("enableDebugging", String.valueOf(enableDebugging));
            settings.setProperty("enableDiscordIntegration", String.valueOf(enableDiscordIntegration));
            if (lastPlayedVersionName != null)
                settings.setProperty("lastPlayedVersionName", lastPlayedVersionName);
            settings.setProperty("scaleFactor", String.valueOf(scaleFactor));
            settings.setProperty("themeName", themeName);
            settings.setProperty("language", localeManager.getCurrentLanguage());
            if (customTranslationPath != null)
                settings.setProperty("customTranslationPath", customTranslationPath);
            else
                settings.remove("customTranslationPath");

            if (githubTranslationUrl != null) {
                settings.setProperty("githubTranslationUrl", githubTranslationUrl);
                settings.setProperty("githubTranslationName", githubTranslationName);
            } else {
                settings.remove("githubTranslationUrl");
                settings.remove("githubTranslationName");
            }

            if (customVersionsSourcesList != null) {
                settings.setProperty("customVersionsSourcesList",
                        new com.google.gson.Gson().toJson(customVersionsSourcesList));
            }
            if (customLauncherPathsList != null) {
                settings.setProperty("customLauncherPathsList",
                        new com.google.gson.Gson().toJson(customLauncherPathsList));
            }
            if (customLauncherProfilesList != null) {
                settings.setProperty("customLauncherProfilesList",
                        new com.google.gson.Gson().toJson(customLauncherProfilesList));
            }
            if (selectedLauncherProfileName != null) {
                settings.setProperty("selectedLauncherProfileName", selectedLauncherProfileName);
            } else {
                settings.remove("selectedLauncherProfileName");
            }

            settings.store(fos, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyTheme() {
        FlatLaf newTheme;
        boolean isDark = themeName.equals("Dark");
        if (SystemInfo.isMacOS)
            newTheme = isDark ? new FlatMacDarkLaf() : new FlatMacLightLaf();
        else
            newTheme = isDark ? new FlatDarculaLaf() : new FlatIntelliJLaf();
        try {
            FlatLaf.setUseNativeWindowDecorations(false);
            UIManager.put("TitlePane.useWindowDecorations", Boolean.FALSE);
            int baseFontSize = (int) Math.round(13 * scaleFactor);
            UIManager.put("defaultFont", new Font("SansSerif", Font.PLAIN, baseFontSize));
            UIManager.setLookAndFeel(newTheme);
            SwingUtilities.updateComponentTreeUI(this);
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    private void initializeUI() {
        getContentPane().removeAll();
        setTitle(localeManager.get("launcher.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension((int) (900 * scaleFactor), (int) (600 * scaleFactor)));
        setSize(new Dimension((int) (900 * scaleFactor), (int) (600 * scaleFactor)));
        setLocationRelativeTo(null);

        BackgroundPanel backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        backgroundPanel.setBackground(UIManager.getColor("Panel.background"));

        navigationPanel = new NavigationPanel(localeManager, scaleFactor, themeName);
        navigationPanel.setOnNavigate(this::onNavigate);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        homePanel = new HomePanel(localeManager, scaleFactor, themeName, versionManager);
        homePanel.setLaunchListener(new LaunchButtonListener());
        homePanel.setRefreshListener(e -> loadVersions());
        homePanel.setAddVersionListener(e -> showAddVersionDialog());

        nicknameField = homePanel.getNicknameField();
        versionComboBox = homePanel.getVersionComboBox();
        launchButton = homePanel.getLaunchButton();
        refreshButton = homePanel.getRefreshButton();
        addVersionButton = homePanel.getAddVersionButton();
        progressBar = homePanel.getProgressBar();
        statusLabel = homePanel.getStatusLabel();

        worldsPanel = new WorldsManagerPanel(localeManager, themeName, scaleFactor);
        texturesPanel = new TexturesManagerPanel(localeManager, themeName, scaleFactor);
        instancesPanel = new InstancesPanel(localeManager, themeName, scaleFactor);
        marketplacePanel = new MarketplacePanel(localeManager, themeName, scaleFactor);
        proxyPanel = new ProxyPanel(localeManager, themeName, scaleFactor);

        versionComboBox.addActionListener(e -> {
            Version selected = (Version) versionComboBox.getSelectedItem();
            if (selected != null) {
                if ("ANOTHER".equals(executableSource)) {
                    selectedLauncherProfileName = selected.getName();
                } else {
                    lastPlayedVersionName = selected.getName();
                }
                saveSettings();
                loadNickname();
                if (worldsPanel != null) {
                    worldsPanel.loadWorlds();
                }
                if (texturesPanel != null) {
                    texturesPanel.resetView();
                }
            }
        });

        instancesPanel.setOnInstanceChanged(() -> {
            saveSettings();
            initializeUI();
            loadVersions();
            loadNickname();
            cardLayout.show(contentPanel, NavigationPanel.NAV_INSTANCES);
            navigationPanel.setSelectedNav(NavigationPanel.NAV_INSTANCES);
        });

        settingsPanel = new SettingsPanel(
                customBackgroundPath, customVersionsSource, useDefaultVersionsSource,
                customVersionsSourcesList,
                executableSource, customLauncherPath,
                customLauncherPathsList,
                postLaunchAction, enableDebugging, enableDiscordIntegration, scaleFactor, themeName, CURRENT_VERSION,
                backgroundMode, customBackgroundColor, customTranslationPath, githubTranslationUrl,
                githubTranslationName,
                customLauncherProfilesList,
                selectedLauncherProfileName,
                localeManager, this::onSettingsSaved);

        contentPanel.add(homePanel, NavigationPanel.NAV_HOME);
        contentPanel.add(worldsPanel, NavigationPanel.NAV_WORLDS);
        contentPanel.add(texturesPanel, NavigationPanel.NAV_TEXTURES);
        contentPanel.add(instancesPanel, NavigationPanel.NAV_INSTANCES);
        contentPanel.add(marketplacePanel, NavigationPanel.NAV_MARKETPLACE);
        contentPanel.add(proxyPanel, NavigationPanel.NAV_PROXY);
        contentPanel.add(settingsPanel, NavigationPanel.NAV_SETTINGS);

        backgroundPanel.add(navigationPanel, BorderLayout.WEST);
        backgroundPanel.add(contentPanel, BorderLayout.CENTER);

        add(backgroundPanel);

        ProxyStatusWidget proxyStatusWidget = new ProxyStatusWidget();
        getLayeredPane().add(proxyStatusWidget, JLayeredPane.POPUP_LAYER);

        ProxyManager.getInstance().setStateListener((running, address, port) -> {
            if (running) {
                proxyStatusWidget.updateText(address, port);
                proxyStatusWidget.setVisible(true);
            } else {
                proxyStatusWidget.setVisible(false);
            }
            getLayeredPane().revalidate();
            getLayeredPane().repaint();
        });

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (!proxyStatusWidget.hasBeenDragged()) {
                    Dimension size = proxyStatusWidget.getPreferredSize();
                    int x = getWidth() - size.width - 25;
                    int y = getHeight() - size.height - 45;
                    proxyStatusWidget.setBounds(x, y, size.width, size.height);
                } else {
                    int x = Math.max(0, Math.min(proxyStatusWidget.getX(), getWidth() - proxyStatusWidget.getWidth()));
                    int y = Math.max(0,
                            Math.min(proxyStatusWidget.getY(), getHeight() - proxyStatusWidget.getHeight()));
                    proxyStatusWidget.setLocation(x, y);
                }
            }
        });

        if (!proxyStatusWidget.hasBeenDragged()) {
            Dimension size = proxyStatusWidget.getPreferredSize();
            int x = getWidth() - size.width - 25;
            int y = getHeight() - size.height - 45;
            proxyStatusWidget.setBounds(x, y, size.width, size.height);
        }

        revalidate();
        repaint();
    }

    private void onNavigate(String navId) {
        DiscordRPCManager discordRPCManager = DiscordRPCManager.getInstance();
        switch (navId) {
            case NavigationPanel.NAV_HOME:
                cardLayout.show(contentPanel, NavigationPanel.NAV_HOME);
                discordRPCManager.updatePresence("On Main Page");
                break;
            case NavigationPanel.NAV_WORLDS:
                worldsPanel.loadWorlds();
                cardLayout.show(contentPanel, NavigationPanel.NAV_WORLDS);
                discordRPCManager.updatePresence("In World Manager");
                break;
            case NavigationPanel.NAV_TEXTURES:
                texturesPanel.resetView();
                cardLayout.show(contentPanel, NavigationPanel.NAV_TEXTURES);
                discordRPCManager.updatePresence("In Texture Manager");
                break;
            case NavigationPanel.NAV_INSTANCES:
                instancesPanel.reload();
                cardLayout.show(contentPanel, NavigationPanel.NAV_INSTANCES);
                DiscordRPCManager.getInstance().updatePresence(localeManager.get("rpc.instances"));
                break;
            case NavigationPanel.NAV_MARKETPLACE:
                cardLayout.show(contentPanel, NavigationPanel.NAV_MARKETPLACE);
                discordRPCManager.updatePresence("Browsing Marketplace");
                break;
            case NavigationPanel.NAV_PROXY:
                cardLayout.show(contentPanel, NavigationPanel.NAV_PROXY);
                discordRPCManager.updatePresence("In Proxy");
                break;
            case NavigationPanel.NAV_SETTINGS:
                cardLayout.show(contentPanel, NavigationPanel.NAV_SETTINGS);
                DiscordRPCManager.getInstance().updatePresence("In Settings");
                break;
        }
    }

    private void onSettingsSaved(SettingsPanel updatedSettings) {
        customBackgroundPath = updatedSettings.getCustomBackgroundPath();
        customVersionsSource = updatedSettings.getCustomVersionsSource();
        useDefaultVersionsSource = updatedSettings.isUseDefaultVersionsSource();
        customVersionsSourcesList = updatedSettings.getCustomVersionsSourcesList();
        executableSource = updatedSettings.getExecutableSource();
        customLauncherPath = updatedSettings.getCustomLauncherPath();
        customLauncherPathsList = updatedSettings.getCustomLauncherPathsList();
        customLauncherProfilesList = updatedSettings.getCustomLauncherProfilesList();
        selectedLauncherProfileName = updatedSettings.getSelectedLauncherProfileName();
        postLaunchAction = updatedSettings.getPostLaunchAction();
        enableDebugging = updatedSettings.isEnableDebugging();
        enableDiscordIntegration = updatedSettings.isEnableDiscordIntegration();
        scaleFactor = updatedSettings.getScaleFactor();
        backgroundMode = updatedSettings.getBackgroundMode();
        customBackgroundColor = updatedSettings.getCustomBackgroundColor();
        customTranslationPath = updatedSettings.getCustomTranslationPath();
        githubTranslationUrl = updatedSettings.getGithubTranslationUrl();
        githubTranslationName = updatedSettings.getGithubTranslationName();

        String newLanguage = updatedSettings.getLanguage();
        String newThemeName = updatedSettings.getThemeName();
        final boolean wasMaximized = (getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0;
        loadingOverlay.start();
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (!newLanguage.equals(localeManager.getCurrentLanguage()) ||
                        ("custom".equals(newLanguage)) || ("github".equals(newLanguage))) {

                    if ("custom".equals(newLanguage)) {
                        localeManager.loadCustomLanguage(customTranslationPath);
                    } else if ("github".equals(newLanguage)) {
                        localeManager.loadFromUrl(githubTranslationUrl, githubTranslationName);
                    } else {
                        localeManager.loadLanguage(newLanguage);
                    }
                }
                if (!newThemeName.equals(themeName)) {
                    themeName = newThemeName;
                    SwingUtilities.invokeAndWait(() -> applyTheme());
                }
                final String activeTabName = settingsPanel.getActiveTabName();
                SwingUtilities.invokeAndWait(() -> {
                    DiscordRPCManager.getInstance().shutdown();
                    if (enableDiscordIntegration) {
                        DiscordRPCManager.getInstance().init();
                    }
                    saveSettings();
                    loadBackground();
                    initializeUI();
                    loadVersions();
                    loadNickname();
                    if (wasMaximized)
                        setExtendedState(JFrame.MAXIMIZED_BOTH);
                    settingsPanel.setActiveTabByName(activeTabName);
                    cardLayout.show(contentPanel, NavigationPanel.NAV_SETTINGS);
                    navigationPanel.setSelectedNav(NavigationPanel.NAV_SETTINGS);
                });
                return null;
            }

            @Override
            protected void done() {
                loadingOverlay.stop();
            }
        };
        worker.execute();
    }

    private void showAddVersionDialog() {
        AddCustomVersionDialog dialog = new AddCustomVersionDialog(this, localeManager);
        dialog.setVisible(true);
        Version newVersion = dialog.getNewVersion();
        if (newVersion != null) {
            try {
                versionManager.addAndSaveCustomVersion(newVersion);
                loadVersions();
                for (int i = 0; i < versionComboBox.getItemCount(); i++) {
                    if (versionComboBox.getItemAt(i).getName().equals(newVersion.getName())) {
                        versionComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            } catch (IOException e) {
                ErrorDialog.showSync(this, localeManager.get("dialog.error.title"),
                        localeManager.get("version.add.error.save", e.getMessage()) + "\n\n" + e.toString());
            }
        }
    }

    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            if (backgroundImage != null) {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int imgWidth = backgroundImage.getWidth(this);
                int imgHeight = backgroundImage.getHeight(this);
                int panelWidth = getWidth();
                int panelHeight = getHeight();
                double scaleX = (double) panelWidth / imgWidth;
                double scaleY = (double) panelHeight / imgHeight;
                double scale = Math.max(scaleX, scaleY);
                int scaledWidth = (int) (imgWidth * scale);
                int scaledHeight = (int) (imgHeight * scale);
                int x = (panelWidth - scaledWidth) / 2;
                int y = (panelHeight - scaledHeight) / 2;
                g2d.drawImage(backgroundImage, x, y, scaledWidth, scaledHeight, this);
            } else if (customBackgroundColor != null) {
                g2d.setColor(customBackgroundColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            } else {
                g2d.setColor(UIManager.getColor("Panel.background"));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
            g2d.dispose();
        }
    }

    private class TranslucentGamePanel extends JPanel {
        public TranslucentGamePanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(255, 255, 255, 50));
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            super.paintComponent(g2d);
            g2d.dispose();
        }
    }

    private void loadNickname() {
        try {
            File optionsFile = getOptionsFile();
            if (optionsFile.exists()) {
                List<String> lines = Files.readAllLines(optionsFile.toPath());
                for (String line : lines) {
                    if (line.startsWith("mp_username:")) {
                        nicknameField.setText(line.substring("mp_username:".length()));
                        break;
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    private void saveNickname() {
        try {
            String nickname = nicknameField.getText().trim();
            if (nickname.isEmpty())
                nickname = "Steve";
            File optionsFile = getOptionsFile();
            File parentDir = optionsFile.getParentFile();
            if (parentDir != null && !parentDir.exists())
                parentDir.mkdirs();
            List<String> lines = new ArrayList<>();
            if (optionsFile.exists()) {
                List<String> existing = Files.readAllLines(optionsFile.toPath());
                for (String line : existing)
                    if (!line.startsWith("mp_username:"))
                        lines.add(line);
            }
            lines.add(0, "mp_username:" + nickname);
            Files.write(optionsFile.toPath(), lines);
        } catch (Exception e) {
            statusLabel.setText(localeManager.get("status.error.saveNickname"));
        }
    }

    private void loadVersions() {
        SwingWorker<List<Version>, Void> worker = new SwingWorker<List<Version>, Void>() {
            @Override
            protected List<Version> doInBackground() throws Exception {
                statusLabel.setText(localeManager.get("status.loadingVersions"));
                refreshButton.setEnabled(false);
                addVersionButton.setEnabled(false);
                if ("ANOTHER".equals(executableSource)) {
                    List<Version> profileVersions = new ArrayList<>();
                    if (customLauncherProfilesList != null) {
                        for (CustomLauncherProfile profile : customLauncherProfilesList) {
                            profileVersions.add(new Version(profile.getName(), ""));
                        }
                    }
                    return profileVersions;
                }
                String source = useDefaultVersionsSource ? DEFAULT_VERSIONS_URL : customVersionsSource;
                return versionManager.loadVersions(source);
            }

            @Override
            protected void done() {
                try {
                    List<Version> versions = get();
                    versionComboBox.removeAllItems();
                    for (Version version : versions)
                        versionComboBox.addItem(version);
                    versionManager.updateInstalledVersions();
                    String instanceName = InstanceManager.getInstance().getActiveInstance();
                    statusLabel.setText(localeManager.get("status.versionsAvailable", versions.size()) + " — "
                            + localeManager.get("label.instance") + ": " + instanceName);
                    if (lastPlayedVersionName != null) {
                        for (int i = 0; i < versionComboBox.getItemCount(); i++) {
                            if (versionComboBox.getItemAt(i).getName().equals(lastPlayedVersionName)) {
                                versionComboBox.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    ErrorDialog.showSync(NostalgiaLauncherDesktop.this, localeManager.get("dialog.error.title"),
                            localeManager.get("error.loadVersions", e.getMessage()) + "\n\n" + e.toString());
                    statusLabel.setText(localeManager.get("status.error.loadVersions"));
                } finally {
                    refreshButton.setEnabled(true);
                    addVersionButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private class LaunchButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (launchWorker != null && !launchWorker.isDone()) {
                launchWorker.cancel(true);
                versionManager.cancelDownload();
                statusLabel.setText(localeManager.get("status.cancelling"));
                launchButton.setEnabled(false);
                return;
            }
            Version selectedVersion = (Version) versionComboBox.getSelectedItem();
            if (selectedVersion == null) {
                JOptionPane.showMessageDialog(NostalgiaLauncherDesktop.this,
                        localeManager.get("error.noVersionSelected.message"),
                        localeManager.get("error.noVersionSelected.title"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            lastPlayedVersionName = selectedVersion.getName();
            saveSettings();
            launchVersion(selectedVersion);
        }
    }

    private void launchVersion(Version version) {
        DiscordRPCManager.getInstance().updatePresence("Playing Minecraft PE a" + version.getName());
        launchWorker = new SwingWorker<Void, Integer>() {
            Process gameProcess;

            @Override
            protected Void doInBackground() throws Exception {
                SwingUtilities.invokeAndWait(() -> {
                    launchButton.setText(localeManager.get("button.cancel"));
                    refreshButton.setEnabled(false);
                    addVersionButton.setEnabled(false);
                    nicknameField.setEnabled(false);
                    versionComboBox.setEnabled(false);
                    progressBar.setVisible(true);
                    progressBar.setValue(0);
                    progressBar.setString(localeManager.get("progress.initializing"));
                });
                if (isCancelled())
                    return null;

                File gameDir = new File(InstanceManager.getInstance().resolvePath("game"));
                if (!gameDir.exists())
                    gameDir.mkdirs();

                if ("COMPILED".equals(executableSource)) {
                    String arch = System.getProperty("os.arch").toLowerCase();
                    String buildFolder = (arch.contains("arm") || arch.contains("aarch64")) ? "build-arm"
                            : "build-i686";
                    File sourceDir = new File(gameDir, "Ninecraft_source");
                    File buildDir = new File(sourceDir, buildFolder);
                    File binDir = new File(buildDir, "ninecraft");
                    File executable = new File(binDir, "ninecraft");
                    if (System.getProperty("os.name").toLowerCase().contains("win")) {
                        File exeWin = new File(sourceDir, "ninecraft.exe");
                        if (!exeWin.exists())
                            exeWin = new File(sourceDir, "bin/ninecraft.exe");
                        executable = exeWin;
                    }
                    if (!executable.exists()) {
                        boolean[] success = { false };
                        SwingUtilities.invokeAndWait(() -> {
                            NinecraftCompilationDialog dialog = new NinecraftCompilationDialog(
                                    NostalgiaLauncherDesktop.this, localeManager);
                            NinecraftCompiler compiler = new NinecraftCompiler();
                            dialog.setOnCancelRequested(() -> compiler.cancel());
                            dialog.setOnStartCompilation((repoUrl) -> {
                                new Thread(() -> {
                                    boolean result = compiler.compile(gameDir, dialog, localeManager, repoUrl);
                                    dialog.compilationFinished(result);
                                    success[0] = result && !compiler.isCancelled();
                                }).start();
                            });
                            dialog.setVisible(true);
                        });
                        if (!success[0])
                            throw new IOException(localeManager.get("error.compilationFailedLog"));
                        if (System.getProperty("os.name").toLowerCase().contains("win"))
                            executable = new File(gameDir, "ninecraft.exe");
                        else
                            executable = new File(gameDir, "ninecraft");
                    }
                    customLauncherPath = executable.getAbsolutePath();
                } else if ("SERVER".equals(executableSource)) {
                    downloadLauncherComponents(progress -> {
                        int progressValue = (int) (progress * 15);
                        publish(progressValue);
                    });
                } else if ("CUSTOM".equals(executableSource)) {
                    File customExe = new File(customLauncherPath);
                    if (customExe.isDirectory() || !customExe.exists())
                        throw new IOException(localeManager.get("error.invalidFilePath") + ": " + customLauncherPath);
                } else if ("ANOTHER".equals(executableSource)) {
                    CustomLauncherProfile selectedProfile = null;
                    if (customLauncherProfilesList != null) {
                        for (CustomLauncherProfile p : customLauncherProfilesList) {
                            if (p.getName().equals(version.getName())) {
                                selectedProfile = p;
                                break;
                            }
                        }
                    }
                    if (selectedProfile == null) {
                        throw new IOException(localeManager.get("error.noExecutableSelected"));
                    }
                    File customExe = new File(selectedProfile.getExecutablePath());
                    if (customExe.isDirectory() || !customExe.exists()) {
                        throw new IOException(localeManager.get("error.invalidFilePath") + ": "
                                + selectedProfile.getExecutablePath());
                    }
                    List<String> missingConditions = new ArrayList<>();
                    if (selectedProfile.getRequiredPaths() != null) {
                        for (String path : selectedProfile.getRequiredPaths()) {
                            File reqFile = new File(gameDir, path);
                            if (!reqFile.exists()) {
                                missingConditions.add(path);
                            }
                        }
                    }
                    if (!missingConditions.isEmpty()) {
                        throw new IOException(localeManager.get("error.launchConditionsNotMet").replace("%s",
                                String.join(", ", missingConditions)));
                    }
                    customLauncherPath = selectedProfile.getExecutablePath();
                }

                if (isCancelled())
                    return null;
                SwingUtilities.invokeLater(() -> statusLabel.setText(localeManager.get("status.checkingInstallation")));
                publish(15);
                if (!"ANOTHER".equals(executableSource)) {
                    if (!versionManager.isVersionInstalled(version)) {
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText(localeManager.get("status.downloading", version.getName()));
                            progressBar.setString(localeManager.get("progress.downloading"));
                        });
                        publish(20);
                        File apkFile = versionManager.downloadVersion(version, progress -> {
                            int progressValue = 20 + (int) (progress * 45);
                            publish(progressValue);
                        }, () -> isCancelled());
                        if (isCancelled())
                            return null;
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText(localeManager.get("status.extracting"));
                            progressBar.setString(localeManager.get("progress.extracting"));
                        });
                        publish(65);
                        versionManager.extractVersion(apkFile, gameDir, () -> isCancelled());
                    }
                    if (isCancelled())
                        return null;
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText(localeManager.get("status.preparingDir"));
                        progressBar.setString(localeManager.get("progress.preparing"));
                    });
                    publish(80);
                    versionManager.prepareGameDir(version, gameDir);
                }
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText(localeManager.get("status.setupNickname"));
                    progressBar.setString(localeManager.get("progress.settingUp"));
                });
                publish(90);
                saveNickname();
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText(localeManager.get("status.startingGame"));
                    progressBar.setString(localeManager.get("progress.launching"));
                });
                publish(95);

                String launcherPath = null;
                if ("CUSTOM".equals(executableSource) || "COMPILED".equals(executableSource)
                        || "ANOTHER".equals(executableSource))
                    launcherPath = customLauncherPath;
                File exitFile = new File(gameDir, "exit.tmp");
                if (exitFile.exists()) {
                    exitFile.delete();
                }
                gameProcess = gameLauncher.launchGame(gameDir, launcherPath, enableDebugging);

                final StringBuilder processOutput = new StringBuilder();
                Thread outputReader = new Thread(() -> {
                    String output = GameLauncher.readProcessOutput(gameProcess);
                    processOutput.append(output);
                });
                outputReader.setDaemon(true);
                outputReader.start();

                SwingUtilities.invokeLater(() -> {
                    switch (postLaunchAction) {
                        case "Minimize Launcher":
                            setExtendedState(JFrame.ICONIFIED);
                            break;
                        case "Close Launcher":
                            System.exit(0);
                            break;
                        default:
                            break;
                    }
                });
                publish(100);
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText(localeManager.get("status.launched"));
                    progressBar.setVisible(false);
                });

                long launchTime = System.currentTimeMillis();
                int exitCode = 0;
                try {
                    boolean exited = false;
                    while (!exited) {
                        if (isCancelled()) {
                            gameProcess.destroy();
                            break;
                        }
                        if (exitFile.exists()) {
                            try {
                                String content = new String(java.nio.file.Files.readAllBytes(exitFile.toPath())).trim();
                                if (!content.isEmpty()) {
                                    exitCode = Integer.parseInt(content);
                                    exited = true;
                                    exitFile.delete();
                                }
                            } catch (Exception e) {
                            }
                        }
                        if (!gameProcess.isAlive()) {
                            exitCode = gameProcess.exitValue();
                            exited = true;
                        }
                        if (!exited) {
                            Thread.sleep(100);
                        }
                    }
                } catch (InterruptedException e) {
                    gameProcess.destroy();
                    if (exitFile.exists()) {
                        exitFile.delete();
                    }
                    return null;
                }

                outputReader.join(2000);

                long runDuration = System.currentTimeMillis() - launchTime;

                if (exitCode != 0) {
                    final String output = processOutput.toString().trim();
                    final long duration = runDuration;
                    final int code = exitCode;
                    SwingUtilities.invokeLater(() -> {
                        StringBuilder errorMsg = new StringBuilder();
                        errorMsg.append("Ninecraft exited with code: ").append(code).append("\n");
                        errorMsg.append("Runtime: ").append(duration / 1000.0).append("s\n\n");
                        if (!output.isEmpty()) {
                            errorMsg.append("--- Process Output ---\n");
                            errorMsg.append(output);
                        } else {
                            errorMsg.append("No output was captured from the process.");
                        }
                        ErrorDialog.show(NostalgiaLauncherDesktop.this,
                                localeManager.get("error.launchFailed.title"), errorMsg.toString());
                    });
                }

                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                if (!chunks.isEmpty())
                    progressBar.setValue(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                launchButton.setText(localeManager.get("button.launch"));
                launchButton.setEnabled(true);
                refreshButton.setEnabled(true);
                addVersionButton.setEnabled(true);
                nicknameField.setEnabled(true);
                versionComboBox.setEnabled(true);
                progressBar.setVisible(false);
                launchWorker = null;
                DiscordRPCManager.getInstance().updatePresence("On Main Page");
                try {
                    get();
                } catch (Exception e) {
                    if (e instanceof java.util.concurrent.CancellationException || (e.getCause() != null
                            && (e.getCause() instanceof java.util.concurrent.CancellationException
                                    || "Cancelled".equals(e.getCause().getMessage())
                                    || "Cancelled".equals(e.getMessage())))) {
                        statusLabel.setText(localeManager.get("status.ready"));
                    } else {
                        StringBuilder errorDetails = new StringBuilder();
                        errorDetails.append(localeManager.get("error.launchFailed.message", e.getMessage()));
                        errorDetails.append("\n\n");
                        if (e.getCause() != null) {
                            errorDetails.append("Cause: ").append(e.getCause().toString()).append("\n\n");
                        }
                        for (StackTraceElement element : e.getStackTrace()) {
                            errorDetails.append("  at ").append(element.toString()).append("\n");
                        }
                        ErrorDialog.showSync(NostalgiaLauncherDesktop.this,
                                localeManager.get("error.launchFailed.title"), errorDetails.toString());
                        statusLabel.setText(localeManager.get("status.error.launchFailed"));
                    }
                }
            }
        };
        launchWorker.execute();
    }

    private void downloadLauncherComponents(java.util.function.Consumer<Float> progressCallback) throws IOException {
        String url = SystemInfo.isWindows ? DEFAULT_LAUNCHER_URL_WINDOWS : DEFAULT_LAUNCHER_URL_LINUX;
        File cacheDir = new File(InstanceManager.getInstance().resolvePath("cache"));
        if (!cacheDir.exists())
            cacheDir.mkdirs();
        File zipFile = new File(cacheDir, "launcher_components.zip");
        statusLabel.setText(localeManager.get("status.loadingComponents"));
        progressBar.setString(localeManager.get("progress.loadingComponents"));

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    long totalSize = entity.getContentLength();
                    try (InputStream is = entity.getContent();
                            FileOutputStream fos = new FileOutputStream(zipFile)) {
                        byte[] buffer = new byte[8192];
                        int read;
                        long totalRead = 0;
                        while ((read = is.read(buffer)) != -1) {
                            if (launchWorker != null && launchWorker.isCancelled()) {
                                throw new IOException("Cancelled");
                            }
                            fos.write(buffer, 0, read);
                            totalRead += read;
                            if (totalSize > 0)
                                progressCallback.accept((float) totalRead / totalSize);
                        }
                    }
                }
            }
        }

        File gameDir = new File(InstanceManager.getInstance().resolvePath("game"));
        if (!gameDir.exists())
            gameDir.mkdirs();

        try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(
                java.nio.file.Files.newInputStream(zipFile.toPath()))) {
            java.util.zip.ZipEntry entry;
            String canonicalGameDir = gameDir.getCanonicalPath() + File.separator;
            while ((entry = zis.getNextEntry()) != null) {
                if (launchWorker != null && launchWorker.isCancelled()) {
                    throw new IOException("Cancelled");
                }

                String entryName = entry.getName();
                File outFile = new File(gameDir, entryName);

                if (!outFile.getCanonicalPath().startsWith(canonicalGameDir)) {
                    throw new IOException("Zip entry outside target directory: " + entryName);
                }

                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    File parent = outFile.getParentFile();
                    if (parent != null && !parent.exists())
                        parent.mkdirs();

                    if (outFile.exists() && outFile.isDirectory()) {
                        deleteRecursive(outFile);
                    }

                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }

                    if (entryName.equals("ninecraft") || entryName.endsWith("/ninecraft")) {
                        outFile.setExecutable(true);
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] content = file.listFiles();
            if (content != null)
                for (File child : content)
                    deleteRecursive(child);
        }
        file.delete();
    }

    private void showFirstLaunchDisclaimer() {
        String shown = settings.getProperty("disclaimerShown", "false");
        if ("true".equals(shown)) {
            return;
        }

        JDialog dialog = new JDialog(this, "Disclaimer", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 20, 30));

        JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.warningIcon"));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(iconLabel);
        mainPanel.add(Box.createVerticalStrut(15));

        JLabel titleLabel = new JLabel("Disclaimer");
        titleLabel.setFont(getRegularFont(Font.BOLD, 18f));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(15));

        String htmlText = "<html><body style='width: 420px; font-family: sans-serif; font-size: 11px;'>"
                + "<p>NOSTALGIALAUNCHER IS NOT RESPONSIBLE FOR ANY GAME-RELATED ISSUES. "
                + "WE ARE NOT AFFILIATED WITH THE GAME ITSELF. "
                + "NOSTALGIALAUNCHER ONLY AUTOMATES THE PROCESS OF LAUNCHING THE GAME "
                + "USING THIRD-PARTY SOFTWARE CALLED NINECRAFT (BY DEFAULT), "
                + "WHICH IS RESPONSIBLE FOR RUNNING THE GAME VERSIONS.</p>"
                + "<p style='margin-top: 8px;'>WE DO NOT AFFECT THE FUNCTIONALITY OF THE GAME VERSIONS "
                + "AND DO NOT INFLUENCE WHETHER THE GAME RUNS PROPERLY ON YOUR DEVICE.</p>"
                + "<p style='margin-top: 12px; color: gray;'>If you encounter any issues, "
                + "feel free to join our Discord server for help and support.</p>"
                + "</body></html>";

        JLabel textLabel = new JLabel(htmlText);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(textLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setMaximumSize(new Dimension(460, 45));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton discordButton = new JButton("Discord");
        discordButton.setPreferredSize(new Dimension(0, 45));
        discordButton.setFont(getRegularFont(Font.BOLD, 14f));
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon discordIcon = new com.formdev.flatlaf.extras.FlatSVGIcon(
                    "icons/discord.svg", 20, 20);
            boolean isDarkTheme = themeName.contains("Dark");
            discordIcon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(
                    c -> isDarkTheme ? Color.WHITE : Color.BLACK));
            discordButton.setIcon(discordIcon);
        } catch (Exception ignored) {
        }
        discordButton.addActionListener(e -> {
            String url = "https://discord.gg/4fv4RrTav4";
            try {
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                    if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                        desktop.browse(new java.net.URI(url));
                        return;
                    }
                }
            } catch (Exception ignored) {
            }

            try {
                new ProcessBuilder("xdg-open", url).start();
                return;
            } catch (Exception ignored) {
            }

            try {
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
            } catch (Exception ignored) {
            }
        });

        JButton okButton = new JButton("OK");
        okButton.setPreferredSize(new Dimension(0, 45));
        okButton.setFont(getRegularFont(Font.BOLD, 14f));
        okButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(discordButton);
        buttonPanel.add(okButton);

        mainPanel.add(buttonPanel);

        dialog.setContentPane(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.getRootPane().setDefaultButton(okButton);
        dialog.setVisible(true);

        settings.setProperty("disclaimerShown", "true");
        saveSettings();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                if (SystemInfo.isMacOS)
                    UIManager.setLookAndFeel(new FlatMacLightLaf());
                else
                    UIManager.setLookAndFeel(new FlatDarculaLaf());
            } catch (Exception e) {
                e.printStackTrace();
            }

            StartupLoadingWindow splash = new StartupLoadingWindow();
            splash.setVisible(true);

            SwingWorker<NostalgiaLauncherDesktop, Void> worker = new SwingWorker<NostalgiaLauncherDesktop, Void>() {
                @Override
                protected NostalgiaLauncherDesktop doInBackground() throws Exception {
                    return new NostalgiaLauncherDesktop();
                }

                @Override
                protected void done() {
                    try {
                        NostalgiaLauncherDesktop launcher = get();
                        launcher.setVisible(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        splash.dispose();
                    }
                }
            };
            worker.execute();
        });
    }

    private static class StartupLoadingWindow extends JWindow {
        private final Timer timer;
        private static Image logoImage = null;
        static {
            try (java.io.InputStream is = StartupLoadingWindow.class.getResourceAsStream("/app_icon.jpg")) {
                if (is != null) {
                    logoImage = javax.imageio.ImageIO.read(is);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public StartupLoadingWindow() {
            setSize(360, 110);
            setLocationRelativeTo(null);

            JPanel content = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2.setColor(new Color(30, 30, 30));
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    g2.setColor(new Color(60, 60, 60));
                    g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

                    String text = "NostalgiaLauncher";
                    g2.setFont(new Font("SansSerif", Font.BOLD, 22));
                    FontMetrics fm = g2.getFontMetrics();
                    int textW = fm.stringWidth(text);

                    int totalW = (logoImage != null) ? (36 + 14 + textW) : textW;
                    int barW = totalW;
                    int barH = 6;
                    int barX = (getWidth() - barW) / 2;
                    int barY = 75;

                    if (logoImage != null) {
                        g2.drawImage(logoImage, barX, 22, 36, 36, null);
                        g2.setColor(Color.WHITE);
                        g2.drawString(text, barX + 50, 48);
                    } else {
                        g2.setColor(Color.WHITE);
                        g2.drawString(text, barX, 48);
                    }

                    g2.setColor(new Color(50, 50, 50));
                    g2.fillRect(barX, barY, barW, barH);

                    g2.setColor(new Color(70, 70, 70));
                    g2.drawRect(barX, barY, barW, barH);

                    g2.setColor(new Color(160, 160, 160));
                    int animW = 80;
                    int animX = barX + (int) ((System.currentTimeMillis() / 4) % (barW + animW)) - animW;

                    g2.setClip(barX, barY, barW, barH);
                    g2.fillRect(animX, barY, animW, barH);
                    g2.setClip(null);

                    g2.dispose();
                }
            };
            content.setBackground(new Color(30, 30, 30));
            setContentPane(content);

            timer = new Timer(30, e -> {
                content.repaint();
            });
            timer.start();
        }

        @Override
        public void dispose() {
            timer.stop();
            super.dispose();
        }
    }

    private class LoadingOverlay extends JComponent implements ActionListener {
        private final Timer timer;
        private int angle = 0;
        private boolean visible = false;

        public LoadingOverlay() {
            timer = new Timer(40, this);
            setOpaque(false);
            addMouseListener(new MouseAdapter() {
            });
            addMouseMotionListener(new MouseAdapter() {
            });
        }

        public void start() {
            visible = true;
            setVisible(true);
            timer.start();
        }

        public void stop() {
            visible = false;
            timer.stop();
            setVisible(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (!visible)
                return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 128));
            g2.fillRect(0, 0, getWidth(), getHeight());
            double size = 50.0;
            double x = (getWidth() - size) / 2.0;
            double y = (getHeight() - size) / 2.0;
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            java.awt.geom.Arc2D.Double arc = new java.awt.geom.Arc2D.Double(
                    x, y, size, size, angle, 270, java.awt.geom.Arc2D.OPEN);
            g2.draw(arc);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            angle = (angle + 10) % 360;
            repaint();
        }
    }

    private class ProxyStatusWidget extends JPanel {
        private JLabel statusTextLabel;
        private boolean hasBeenDragged = false;
        private Point dragStartPoint = null;
        private boolean isDragging = false;

        public ProxyStatusWidget() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JLabel iconLabel = new JLabel("●");
            iconLabel.setFont(new Font("SansSerif", Font.PLAIN, (int) (16 * scaleFactor)));
            iconLabel.setForeground(new Color(76, 175, 80));

            statusTextLabel = new JLabel("");
            statusTextLabel.setFont(new Font("SansSerif", Font.BOLD, (int) (12 * scaleFactor)));
            statusTextLabel.setForeground(themeName.contains("Dark") ? Color.WHITE : Color.BLACK);

            add(iconLabel);
            add(statusTextLabel);

            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        dragStartPoint = e.getPoint();
                        isDragging = false;
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        if (!isDragging) {
                            onNavigate(NavigationPanel.NAV_PROXY);
                            navigationPanel.setSelectedNav(NavigationPanel.NAV_PROXY);
                        }
                        dragStartPoint = null;
                        isDragging = false;
                    }
                }
            });

            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragStartPoint != null) {
                        isDragging = true;
                        hasBeenDragged = true;
                        Point current = e.getLocationOnScreen();
                        Container parent = getParent();
                        if (parent != null) {
                            Point parentLoc = parent.getLocationOnScreen();
                            int newX = current.x - parentLoc.x - dragStartPoint.x;
                            int newY = current.y - parentLoc.y - dragStartPoint.y;
                            newX = Math.max(0, Math.min(newX, parent.getWidth() - getWidth()));
                            newY = Math.max(0, Math.min(newY, parent.getHeight() - getHeight()));
                            setBounds(newX, newY, getWidth(), getHeight());
                        }
                    }
                }
            });
        }

        public boolean hasBeenDragged() {
            return hasBeenDragged;
        }

        public void updateText(String address, int port) {
            SwingUtilities.invokeLater(() -> {
                statusTextLabel.setText(String.format("%s:%d", address, port));
                Dimension size = getPreferredSize();
                if (!hasBeenDragged) {
                    int x = NostalgiaLauncherDesktop.this.getWidth() - size.width - 25;
                    int y = NostalgiaLauncherDesktop.this.getHeight() - size.height - 45;
                    setBounds(x, y, size.width, size.height);
                } else {
                    setBounds(getX(), getY(), size.width, size.height);
                }
                revalidate();
                repaint();
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (themeName.contains("Dark")) {
                g2d.setColor(new Color(45, 45, 45, 230));
            } else {
                g2d.setColor(new Color(240, 240, 240, 230));
            }
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2d.setColor(themeName.contains("Dark") ? new Color(255, 255, 255, 20) : new Color(0, 0, 0, 15));
            g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            g2d.dispose();
            super.paintComponent(g);
        }
    }
}