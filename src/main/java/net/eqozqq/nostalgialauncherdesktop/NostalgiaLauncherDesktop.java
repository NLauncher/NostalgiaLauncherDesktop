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
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

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

    private String executableSource;
    private String customLauncherPath;
    private boolean useDefaultLauncher;

    private String postLaunchAction;
    private boolean enableDebugging;
    private String lastPlayedVersionName;
    private double scaleFactor;
    private String themeName;
    private String backgroundMode;
    private String customTranslationPath;
    private static final String CURRENT_VERSION = "1.7.1";

    private static final int COMPONENT_WIDTH = 300;
    private static final String DEFAULT_VERSIONS_URL = "https://raw.githubusercontent.com/NLauncher/components/main/versions.json";
    private static final String DEFAULT_LAUNCHER_URL_WINDOWS = "https://github.com/NLauncher/components/raw/main/ninecraft-windows.zip";
    private static final String DEFAULT_LAUNCHER_URL_LINUX = "https://github.com/NLauncher/components/raw/main/ninecraft-linux.zip";

    public NostalgiaLauncherDesktop() {
        versionManager = new VersionManager();
        gameLauncher = new GameLauncher();
        settings = new Properties();
        localeManager = LocaleManager.getInstance();
        loadSettings();
        localeManager.init(settings);
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
        try (InputStream fontStream = NostalgiaLauncherDesktop.class.getResourceAsStream("/MPLUS1p-Regular.ttf")) {
            if (fontStream != null)
                return Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(style, size);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Font("SansSerif", style, (int) size);
    }

    private Font getRegularFont(int style, float size) {
        try (InputStream fontStream = NostalgiaLauncherDesktop.class.getResourceAsStream("/MPLUS1p-Regular.ttf")) {
            if (fontStream != null)
                return Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(style, size);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Font("SansSerif", style, (int) size);
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
        try (FileInputStream fis = new FileInputStream("launcher.properties")) {
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
            executableSource = settings.getProperty("executableSource");
            if (executableSource == null)
                executableSource = SystemInfo.isLinux ? "COMPILED" : "SERVER";
            customLauncherPath = settings.getProperty("customLauncherPath");
            if (settings.containsKey("useDefaultLauncher")) {
                boolean useDefault = Boolean.parseBoolean(settings.getProperty("useDefaultLauncher"));
                if (!useDefault && customLauncherPath != null && !customLauncherPath.isEmpty())
                    executableSource = "CUSTOM";
            }
            postLaunchAction = settings.getProperty("postLaunchAction", "Do Nothing");
            enableDebugging = Boolean.parseBoolean(settings.getProperty("enableDebugging", "false"));
            lastPlayedVersionName = settings.getProperty("lastPlayedVersionName");
            scaleFactor = Double.parseDouble(settings.getProperty("scaleFactor", "1.3"));
            themeName = settings.getProperty("themeName", "Dark");
            customTranslationPath = settings.getProperty("customTranslationPath");
        } catch (IOException | NumberFormatException e) {
            backgroundMode = "Default";
            useDefaultVersionsSource = true;
            executableSource = SystemInfo.isLinux ? "COMPILED" : "SERVER";
            postLaunchAction = "Do Nothing";
            enableDebugging = false;
            scaleFactor = 1.3;
            themeName = "Dark";
        }
    }

    private void saveSettings() {
        try (FileOutputStream fos = new FileOutputStream("launcher.properties")) {
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
            if (lastPlayedVersionName != null)
                settings.setProperty("lastPlayedVersionName", lastPlayedVersionName);
            settings.setProperty("scaleFactor", String.valueOf(scaleFactor));
            settings.setProperty("themeName", themeName);
            settings.setProperty("language", localeManager.getCurrentLanguage());
            if (customTranslationPath != null)
                settings.setProperty("customTranslationPath", customTranslationPath);
            else
                settings.remove("customTranslationPath");
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

        worldsPanel = new WorldsManagerPanel(localeManager, themeName);
        texturesPanel = new TexturesManagerPanel(localeManager, themeName);
        instancesPanel = new InstancesPanel(localeManager, themeName);

        instancesPanel.setOnInstanceChanged(() -> {
            saveSettings();
            initializeUI();
            loadVersions();
            loadNickname();
        });

        settingsPanel = new SettingsPanel(
                customBackgroundPath, customVersionsSource, useDefaultVersionsSource, executableSource,
                customLauncherPath, postLaunchAction, enableDebugging, scaleFactor, themeName, CURRENT_VERSION,
                backgroundMode, customBackgroundColor, customTranslationPath, localeManager,
                this::onSettingsSaved);

        contentPanel.add(homePanel, NavigationPanel.NAV_HOME);
        contentPanel.add(worldsPanel, NavigationPanel.NAV_WORLDS);
        contentPanel.add(texturesPanel, NavigationPanel.NAV_TEXTURES);
        contentPanel.add(instancesPanel, NavigationPanel.NAV_INSTANCES);
        contentPanel.add(settingsPanel, NavigationPanel.NAV_SETTINGS);

        backgroundPanel.add(navigationPanel, BorderLayout.WEST);
        backgroundPanel.add(contentPanel, BorderLayout.CENTER);

        add(backgroundPanel);
        revalidate();
        repaint();
    }

    private void onNavigate(String navId) {
        switch (navId) {
            case NavigationPanel.NAV_HOME:
                cardLayout.show(contentPanel, NavigationPanel.NAV_HOME);
                break;
            case NavigationPanel.NAV_WORLDS:
                worldsPanel.loadWorlds();
                cardLayout.show(contentPanel, NavigationPanel.NAV_WORLDS);
                break;
            case NavigationPanel.NAV_TEXTURES:
                texturesPanel.resetView();
                cardLayout.show(contentPanel, NavigationPanel.NAV_TEXTURES);
                break;
            case NavigationPanel.NAV_INSTANCES:
                instancesPanel.reload();
                cardLayout.show(contentPanel, NavigationPanel.NAV_INSTANCES);
                break;
            case NavigationPanel.NAV_SETTINGS:
                cardLayout.show(contentPanel, NavigationPanel.NAV_SETTINGS);
                break;
        }
    }

    private void onSettingsSaved(SettingsPanel updatedSettings) {
        customBackgroundPath = updatedSettings.getCustomBackgroundPath();
        customVersionsSource = updatedSettings.getCustomVersionsSource();
        useDefaultVersionsSource = updatedSettings.isUseDefaultVersionsSource();
        executableSource = updatedSettings.getExecutableSource();
        customLauncherPath = updatedSettings.getCustomLauncherPath();
        postLaunchAction = updatedSettings.getPostLaunchAction();
        enableDebugging = updatedSettings.isEnableDebugging();
        scaleFactor = updatedSettings.getScaleFactor();
        backgroundMode = updatedSettings.getBackgroundMode();
        customBackgroundColor = updatedSettings.getCustomBackgroundColor();
        customTranslationPath = updatedSettings.getCustomTranslationPath();
        String newLanguage = updatedSettings.getLanguage();
        String newThemeName = updatedSettings.getThemeName();
        final boolean wasMaximized = (getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0;
        loadingOverlay.start();
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (!newLanguage.equals(localeManager.getCurrentLanguage()) || ("custom".equals(newLanguage))) {
                    if ("custom".equals(newLanguage))
                        localeManager.loadCustomLanguage(customTranslationPath);
                    else
                        localeManager.loadLanguage(newLanguage);
                }
                if (!newThemeName.equals(themeName)) {
                    themeName = newThemeName;
                    SwingUtilities.invokeAndWait(() -> applyTheme());
                }
                final String activeTabName = settingsPanel.getActiveTabName();
                SwingUtilities.invokeAndWait(() -> {
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
                JOptionPane.showMessageDialog(this, localeManager.get("version.add.error.save", e.getMessage()),
                        localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
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
            File optionsFile = new File(
                    InstanceManager.getInstance().resolvePath("game/storage/games/com.mojang/minecraftpe/options.txt"));
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
            File optionsDir = new File(
                    InstanceManager.getInstance().resolvePath("game/storage/games/com.mojang/minecraftpe"));
            if (!optionsDir.exists())
                optionsDir.mkdirs();
            File optionsFile = new File(optionsDir, "options.txt");
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
                    JOptionPane.showMessageDialog(NostalgiaLauncherDesktop.this,
                            localeManager.get("error.loadVersions", e.getMessage()),
                            localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
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
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                launchButton.setEnabled(false);
                refreshButton.setEnabled(false);
                addVersionButton.setEnabled(false);
                nicknameField.setEnabled(false);
                versionComboBox.setEnabled(false);
                progressBar.setVisible(true);
                progressBar.setValue(0);
                progressBar.setString(localeManager.get("progress.initializing"));
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
                            dialog.setOnStartCompilation((repoUrl) -> {
                                new Thread(() -> {
                                    boolean result = NinecraftCompiler.compile(gameDir, dialog, localeManager, repoUrl);
                                    dialog.compilationFinished(result);
                                    success[0] = result;
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
                    if (!customExe.exists())
                        throw new IOException(localeManager.get("error.invalidFilePath") + ": " + customLauncherPath);
                }

                statusLabel.setText(localeManager.get("status.checkingInstallation"));
                publish(15);
                if (!versionManager.isVersionInstalled(version)) {
                    statusLabel.setText(localeManager.get("status.downloading", version.getName()));
                    progressBar.setString(localeManager.get("progress.downloading"));
                    publish(20);
                    File apkFile = versionManager.downloadVersion(version, progress -> {
                        int progressValue = 20 + (int) (progress * 45);
                        publish(progressValue);
                    });
                    statusLabel.setText(localeManager.get("status.extracting"));
                    progressBar.setString(localeManager.get("progress.extracting"));
                    publish(65);
                    versionManager.extractVersion(apkFile, gameDir);
                }
                statusLabel.setText(localeManager.get("status.preparingDir"));
                progressBar.setString(localeManager.get("progress.preparing"));
                publish(80);
                versionManager.prepareGameDir(version, gameDir);
                statusLabel.setText(localeManager.get("status.setupNickname"));
                progressBar.setString(localeManager.get("progress.settingUp"));
                publish(90);
                saveNickname();
                statusLabel.setText(localeManager.get("status.startingGame"));
                progressBar.setString(localeManager.get("progress.launching"));
                publish(95);

                String launcherPath = null;
                if ("CUSTOM".equals(executableSource) || "COMPILED".equals(executableSource))
                    launcherPath = customLauncherPath;
                Process gameProcess = gameLauncher.launchGame(gameDir, launcherPath, enableDebugging);
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
                statusLabel.setText(localeManager.get("status.launched"));
                progressBar.setVisible(false);
                gameProcess.waitFor();
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                if (!chunks.isEmpty())
                    progressBar.setValue(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText(localeManager.get("status.ready"));
                    progressBar.setVisible(false);
                } catch (Exception e) {
                    String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    JOptionPane.showMessageDialog(NostalgiaLauncherDesktop.this,
                            localeManager.get("error.launchFailed.message", message),
                            localeManager.get("error.launchFailed.title"), JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText(localeManager.get("status.error.launchFailed"));
                    progressBar.setVisible(false);
                } finally {
                    launchButton.setEnabled(true);
                    refreshButton.setEnabled(true);
                    addVersionButton.setEnabled(true);
                    nicknameField.setEnabled(true);
                    versionComboBox.setEnabled(true);
                    versionManager.updateInstalledVersions();
                }
            }
        };
        worker.execute();
    }

    private void downloadLauncherComponents(ProgressCallback callback) throws IOException {
        String osName = System.getProperty("os.name").toLowerCase();
        boolean isWindows = osName.contains("win");
        File gameDir = new File(InstanceManager.getInstance().resolvePath("game"));
        String executableName = isWindows ? "ninecraft.exe" : "ninecraft";
        File executable = new File(gameDir, executableName);
        if (executable.exists()) {
            if (executable.isDirectory())
                deleteRecursive(executable);
            else
                executable.delete();
        }
        statusLabel.setText(localeManager.get("status.loadingComponents"));
        progressBar.setString(localeManager.get("progress.loadingComponents"));
        String launcherUrl = isWindows ? DEFAULT_LAUNCHER_URL_WINDOWS : DEFAULT_LAUNCHER_URL_LINUX;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(launcherUrl);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    long totalSize = entity.getContentLength();
                    File tempZipFile = File.createTempFile("launcher_components", ".zip");
                    try (InputStream inputStream = entity.getContent();
                            OutputStream outputStream = new FileOutputStream(tempZipFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        long totalBytesRead = 0;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                            if (callback != null && totalSize > 0)
                                callback.onProgress((double) totalBytesRead / totalSize);
                        }
                    }
                    try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(
                            Files.newInputStream(tempZipFile.toPath()))) {
                        java.util.zip.ZipEntry entry;
                        while ((entry = zis.getNextEntry()) != null) {
                            if (entry.isDirectory())
                                continue;
                            File newFile = new File(gameDir, entry.getName());
                            File parent = newFile.getParentFile();
                            if (parent != null && !parent.exists())
                                parent.mkdirs();
                            try (FileOutputStream fos = new FileOutputStream(newFile)) {
                                byte[] buffer = new byte[1024];
                                int len;
                                while ((len = zis.read(buffer)) > 0)
                                    fos.write(buffer, 0, len);
                            }
                            if (!isWindows && entry.getName().equals("ninecraft"))
                                newFile.setExecutable(true);
                        }
                    } finally {
                        tempZipFile.delete();
                    }
                }
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
            NostalgiaLauncherDesktop launcher = new NostalgiaLauncherDesktop();
            launcher.setVisible(true);
        });
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
            int size = 50;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(x, y, size, size, angle, 270);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            angle = (angle + 10) % 360;
            repaint();
        }
    }
}