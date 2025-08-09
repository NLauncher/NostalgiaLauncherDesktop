package net.eqozqq.nostalgialauncherdesktop;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


public class NostalgiaLauncherDesktop extends JFrame {
    private JTextField nicknameField;
    private JComboBox<Version> versionComboBox;
    private JButton launchButton;
    private JButton refreshButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private Image backgroundImage;

    private VersionManager versionManager;
    private GameLauncher gameLauncher;
    private Properties settings;

    private String customBackgroundPath;
    private boolean useDefaultBackground;
    private String customVersionsSource;
    private boolean useDefaultVersionsSource;
    private String customLauncherPath;
    private boolean useDefaultLauncher;
    private String postLaunchAction;
    private boolean enableDebugging;
    private String lastPlayedVersionName;

    private static final int COMPONENT_WIDTH = 300;
    private static final String DEFAULT_VERSIONS_URL = "https://raw.githubusercontent.com/NLauncher/NostalgiaLauncherDesktop/main/winlauncher_versions.json";
    private static final String DEFAULT_LAUNCHER_URL = "https://github.com/NLauncher/NostalgiaLauncherDesktop/raw/refs/main/ninecraft.zip";

    public NostalgiaLauncherDesktop() {
        versionManager = new VersionManager();
        gameLauncher = new GameLauncher();
        settings = new Properties();

        loadSettings();
        loadBackgroundImage();
        initializeUI();
        loadVersions();
        loadNickname();
        setIcon();
    }

    private void setIcon() {
        try (InputStream iconStream = NostalgiaLauncherDesktop.class.getResourceAsStream("/app_icon.jpg")) {
            if (iconStream != null) {
                Image iconImage = ImageIO.read(iconStream);
                if (iconImage != null) {
                    setIconImage(iconImage);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load launcher icon: " + e.getMessage());
        }
    }

    private Font getMinecraftFont(int style, float size) {
        try (InputStream fontStream = NostalgiaLauncherDesktop.class.getResourceAsStream("/MPLUS1p-Regular.ttf")) {
            if (fontStream != null) {
                return Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(style, size);
            } else {
                System.err.println("MPLUS1p-Regular.ttf font file not found.");
            }
        } catch (Exception e) {
            System.err.println("Error loading MPLUS1p-Regular font: " + e.getMessage());
        }
        return new Font("SansSerif", style, (int) size);
    }
    
    private Font getRegularFont(int style, float size) {
        try (InputStream fontStream = NostalgiaLauncherDesktop.class.getResourceAsStream("/MPLUS1p-Regular.ttf")) {
            if (fontStream != null) {
                return Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(style, size);
            } else {
                System.err.println("MPLUS1p-Regular.ttf font file not found.");
            }
        } catch (Exception e) {
            System.err.println("Error loading MPLUS1p-Regular font: " + e.getMessage());
        }
        return new Font("SansSerif", style, (int) size);
    }

    private void loadBackgroundImage() {
        try {
            if (useDefaultBackground) {
                try (InputStream backgroundStream = NostalgiaLauncherDesktop.class.getResourceAsStream("/background.png")) {
                    if (backgroundStream != null) {
                        backgroundImage = ImageIO.read(backgroundStream);
                    } else {
                        System.err.println("background.png file not found in resources.");
                        backgroundImage = null;
                    }
                }
            } else if (customBackgroundPath != null && new File(customBackgroundPath).exists()) {
                backgroundImage = ImageIO.read(new File(customBackgroundPath));
            } else {
                try (InputStream backgroundStream = NostalgiaLauncherDesktop.class.getResourceAsStream("/background.png")) {
                    if (backgroundStream != null) {
                        backgroundImage = ImageIO.read(backgroundStream);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load background image: " + e.getMessage());
            backgroundImage = null;
        }
    }

    private void loadSettings() {
        try (FileInputStream fis = new FileInputStream("launcher.properties")) {
            settings.load(fis);
            customBackgroundPath = settings.getProperty("customBackgroundPath");
            useDefaultBackground = Boolean.parseBoolean(settings.getProperty("useDefaultBackground", "true"));
            customVersionsSource = settings.getProperty("customVersionsSource");
            useDefaultVersionsSource = Boolean.parseBoolean(settings.getProperty("useDefaultVersionsSource", "true"));
            customLauncherPath = settings.getProperty("customLauncherPath");
            useDefaultLauncher = Boolean.parseBoolean(settings.getProperty("useDefaultLauncher", "true"));
            postLaunchAction = settings.getProperty("postLaunchAction", "Do Nothing");
            enableDebugging = Boolean.parseBoolean(settings.getProperty("enableDebugging", "false"));
            lastPlayedVersionName = settings.getProperty("lastPlayedVersionName");
        } catch (IOException e) {
            useDefaultBackground = true;
            useDefaultVersionsSource = true;
            useDefaultLauncher = true;
            postLaunchAction = "Do Nothing";
            enableDebugging = false;
        }
    }

    private void saveSettings() {
        try (FileOutputStream fos = new FileOutputStream("launcher.properties")) {
            if (customBackgroundPath != null) {
                settings.setProperty("customBackgroundPath", customBackgroundPath);
            }
            settings.setProperty("useDefaultBackground", String.valueOf(useDefaultBackground));
            if (customVersionsSource != null) {
                settings.setProperty("customVersionsSource", customVersionsSource);
            }
            settings.setProperty("useDefaultVersionsSource", String.valueOf(useDefaultVersionsSource));
            if (customLauncherPath != null) {
                settings.setProperty("customLauncherPath", customLauncherPath);
            }
            settings.setProperty("useDefaultLauncher", String.valueOf(useDefaultLauncher));
            settings.setProperty("postLaunchAction", postLaunchAction);
            settings.setProperty("enableDebugging", String.valueOf(enableDebugging));
            if (lastPlayedVersionName != null) {
                settings.setProperty("lastPlayedVersionName", lastPlayedVersionName);
            }
            settings.store(fos, null);
        } catch (IOException e) {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }

    private void initializeUI() {
        setTitle("NostalgiaLauncher Desktop");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(true);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);

        BackgroundPanel backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new GridBagLayout());

        JPanel topPanel = createTopButtonsPanel();
        GridBagConstraints gbcTop = new GridBagConstraints();
        gbcTop.gridx = 0;
        gbcTop.gridy = 0;
        gbcTop.weightx = 1.0;
        gbcTop.weighty = 1.0;
        gbcTop.anchor = GridBagConstraints.NORTH;
        gbcTop.insets = new Insets(10, 0, 0, 0);
        backgroundPanel.add(topPanel, gbcTop);

        JPanel contentPanel = createContentPanel();
        GridBagConstraints gbcMain = new GridBagConstraints();
        gbcMain.gridx = 0;
        gbcMain.gridy = 0;
        gbcMain.weighty = 1.0;
        gbcMain.anchor = GridBagConstraints.CENTER;
        backgroundPanel.add(contentPanel, gbcMain);

        JPanel infoPanel = createInfoPanel();
        GridBagConstraints gbcInfo = new GridBagConstraints();
        gbcInfo.gridx = 0;
        gbcInfo.gridy = 1;
        gbcInfo.weighty = 0.0;
        gbcInfo.anchor = GridBagConstraints.PAGE_END;
        gbcInfo.insets = new Insets(0, 0, 10, 0);
        backgroundPanel.add(infoPanel, gbcInfo);

        add(backgroundPanel);
    }

    private JPanel createTopButtonsPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JButton discordButton = createIconButton("icons/discord.svg", "Discord", "https://discord.gg/4fv4RrTav4");
        JButton websiteButton = createIconButton("icons/globe.svg", "Website", "https://nlauncher.github.io/");
        JButton settingsButton = createIconButton("icons/gear.svg", "Settings", null);
        settingsButton.addActionListener(e -> showSettingsDialog());

        panel.add(Box.createHorizontalGlue());
        panel.add(discordButton);
        panel.add(Box.createHorizontalStrut(5));
        panel.add(websiteButton);
        panel.add(Box.createHorizontalStrut(5));
        panel.add(settingsButton);
        panel.add(Box.createHorizontalStrut(10));
        
        return panel;
    }

    private JButton createIconButton(String iconPath, String tooltip, String url) {
        JButton button = new JButton();
        try {
            FlatSVGIcon icon = new FlatSVGIcon(iconPath, 20, 20);
            button.setIcon(icon);
        } catch (Exception e) {
            System.err.println("Failed to load icon: " + iconPath);
        }
        button.setToolTipText(tooltip);
        if (url != null) {
            button.addActionListener(e -> {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    System.err.println("Failed to open URL: " + ex.getMessage());
                }
            });
        }
        button.setPreferredSize(new Dimension(35, 35));
        button.setMaximumSize(new Dimension(35, 35));
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);
        return button;
    }

    private void showSettingsDialog() {
        SettingsDialog dialog = new SettingsDialog(this,
            customBackgroundPath, useDefaultBackground, customVersionsSource, useDefaultVersionsSource,
            customLauncherPath, useDefaultLauncher, postLaunchAction, enableDebugging);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            customBackgroundPath = dialog.getCustomBackgroundPath();
            useDefaultBackground = dialog.isUseDefaultBackground();
            customVersionsSource = dialog.getCustomVersionsSource();
            useDefaultVersionsSource = dialog.isUseDefaultVersionsSource();
            customLauncherPath = dialog.getCustomLauncherPath();
            useDefaultLauncher = dialog.isUseDefaultLauncher();
            postLaunchAction = dialog.getPostLaunchAction();
            enableDebugging = dialog.isEnableDebugging();
            saveSettings();
            loadBackgroundImage();
            loadVersions();
            repaint();
        }
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
            }
        };

        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel versionLabel = new JLabel("NostalgiaLauncher Desktop v1.1.1 by eqozqq");
        versionLabel.setForeground(UIManager.getColor("Label.foreground"));
        versionLabel.setFont(getRegularFont(Font.PLAIN, 12));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel disclaimerLabel = new JLabel("This program is not affiliated with Mojang, Microsoft, or any other entity");
        disclaimerLabel.setForeground(UIManager.getColor("Label.foreground"));
        disclaimerLabel.setFont(getRegularFont(Font.PLAIN, 10));
        disclaimerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(versionLabel);
        infoPanel.add(disclaimerLabel);

        return infoPanel;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(40, 60, 40, 60));

        contentPanel.add(createLogoPanel());
        contentPanel.add(Box.createVerticalStrut(30));

        JPanel transparentPanel = createTranslucentGamePanel();
        contentPanel.add(transparentPanel);

        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(createProgressPanel());
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(createStatusPanel());

        return contentPanel;
    }

    private JPanel createTranslucentGamePanel() {
        JPanel gamePanel = new TranslucentGamePanel();
        gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.Y_AXIS));
        gamePanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        nicknameField = new JTextField();
        nicknameField.setPreferredSize(new Dimension(COMPONENT_WIDTH, 35));
        nicknameField.setMaximumSize(new Dimension(COMPONENT_WIDTH, 35));
        nicknameField.setFont(getRegularFont(Font.PLAIN, 14));
        nicknameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        nicknameField.putClientProperty("JTextField.placeholderText", "Nickname");
        nicknameField.setText("Steve");

        gamePanel.add(nicknameField);
        gamePanel.add(Box.createVerticalStrut(2));

        JPanel versionPanel = new JPanel();
        versionPanel.setLayout(new BoxLayout(versionPanel, BoxLayout.X_AXIS));
        versionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionPanel.setOpaque(false);

        versionComboBox = new JComboBox<Version>();
        versionComboBox.setPreferredSize(new Dimension(COMPONENT_WIDTH - 40, 35));
        versionComboBox.setMaximumSize(new Dimension(COMPONENT_WIDTH - 40, 35));
        versionComboBox.setFont(getRegularFont(Font.PLAIN, 14));
        versionComboBox.setRenderer(new VersionListCellRenderer(versionManager));

        refreshButton = new JButton();
        try {
            FlatSVGIcon icon = new FlatSVGIcon("icons/refresh.svg", 16, 16);
            refreshButton.setIcon(icon);
        } catch (Exception e) {
            System.err.println("Failed to load refresh icon: " + e.getMessage());
        }
        refreshButton.setPreferredSize(new Dimension(35, 35));
        refreshButton.setMaximumSize(new Dimension(35, 35));
        refreshButton.setToolTipText("Refresh versions");
        refreshButton.addActionListener(e -> loadVersions());

        versionPanel.add(versionComboBox);
        versionPanel.add(Box.createHorizontalStrut(5));
        versionPanel.add(refreshButton);

        gamePanel.add(versionPanel);
        gamePanel.add(Box.createVerticalStrut(5));

        launchButton = new JButton("Launch");
        launchButton.setPreferredSize(new Dimension(COMPONENT_WIDTH, 45));
        launchButton.setMaximumSize(new Dimension(COMPONENT_WIDTH, 45));
        launchButton.setFont(getRegularFont(Font.BOLD, 16));
        launchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        launchButton.addActionListener(new LaunchButtonListener());

        gamePanel.add(launchButton);

        return gamePanel;
    }

    private JPanel createLogoPanel() {
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setOpaque(false);

        JLabel logoLabel = new JLabel("NLauncher Desktop");
        logoLabel.setFont(getMinecraftFont(Font.PLAIN, 31));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoLabel.setForeground(UIManager.getColor("Label.foreground"));
        logoLabel.setOpaque(false);
        logoPanel.add(logoLabel);

        JLabel subtitleLabel = new JLabel("Minecraft Pocket Edition Alpha Launcher");
        subtitleLabel.setFont(getRegularFont(Font.PLAIN, 18));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(UIManager.getColor("Label.foreground"));
        subtitleLabel.setOpaque(false);

        logoPanel.add(Box.createVerticalStrut(10));
        logoPanel.add(subtitleLabel);

        return logoPanel;
    }

    private JPanel createProgressPanel() {
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setOpaque(false);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(COMPONENT_WIDTH, 20));
        progressBar.setMaximumSize(new Dimension(COMPONENT_WIDTH, 20));
        progressBar.setFont(getRegularFont(Font.PLAIN, 12));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        progressPanel.add(progressBar);

        return progressPanel;
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setOpaque(false);

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(getRegularFont(Font.PLAIN, 12));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(UIManager.getColor("Label.foreground"));
        statusLabel.setOpaque(false);

        statusPanel.add(statusLabel);

        return statusPanel;
    }

    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
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
            File optionsFile = new File("game/storage/games/com.mojang/minecraftpe/options.txt");
            if (optionsFile.exists()) {
                List<String> lines = Files.readAllLines(optionsFile.toPath());
                if (!lines.isEmpty()) {
                    String firstLine = lines.get(0);
                    if (firstLine.startsWith("mp_username:")) {
                        String nickname = firstLine.substring("mp_username:".length());
                        nicknameField.setText(nickname);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    private void saveNickname() {
        try {
            String nickname = nicknameField.getText().trim();
            if (nickname.isEmpty()) {
                nickname = "Steve";
            }
            File optionsDir = new File("game/storage/games/com.mojang/minecraftpe");
            if (!optionsDir.exists()) {
                optionsDir.mkdirs();
            }
            File optionsFile = new File(optionsDir, "options.txt");
            List<String> lines;
            if (optionsFile.exists()) {
                lines = new ArrayList<>(Files.readAllLines(optionsFile.toPath()));
                if (!lines.isEmpty() && lines.get(0).startsWith("mp_username:")) {
                    lines.set(0, "mp_username:" + nickname);
                } else {
                    lines.add(0, "mp_username:" + nickname);
                }
            } else {
                lines = new ArrayList<>();
                lines.add("mp_username:" + nickname);
            }
            Files.write(optionsFile.toPath(), lines);
        } catch (Exception e) {
            statusLabel.setText("Failed to save nickname");
        }
    }

    private void loadVersions() {
        SwingWorker<List<Version>, Void> worker = new SwingWorker<List<Version>, Void>() {
            @Override
            protected List<Version> doInBackground() throws Exception {
                statusLabel.setText("Loading versions...");
                refreshButton.setEnabled(false);
                String source = useDefaultVersionsSource ? DEFAULT_VERSIONS_URL : customVersionsSource;
                return versionManager.loadVersions(source);
            }

            @Override
            protected void done() {
                try {
                    List<Version> versions = get();
                    versionComboBox.removeAllItems();
                    for (Version version : versions) {
                        versionComboBox.addItem(version);
                    }
                    versionManager.updateInstalledVersions();
                    statusLabel.setText(versions.size() + " versions available");
                    
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
                            "Failed to load versions: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Error loading versions");
                } finally {
                    refreshButton.setEnabled(true);
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
                        "Please select a version to launch.",
                        "No version selected", JOptionPane.WARNING_MESSAGE);
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
                nicknameField.setEnabled(false);
                versionComboBox.setEnabled(false);
                progressBar.setVisible(true);
                progressBar.setValue(0);
                progressBar.setString("Initializing...");
                File gameDir = new File("game");
                if (!gameDir.exists()) {
                    gameDir.mkdirs();
                }

                if (useDefaultLauncher) {
                    downloadLauncherComponents(progress -> {
                        int progressValue = (int)(progress * 15);
                        publish(progressValue);
                    });
                }

                statusLabel.setText("Checking version installation...");
                publish(15);
                if (!versionManager.isVersionInstalled(version)) {
                    statusLabel.setText("Downloading " + version.getName() + "...");
                    progressBar.setString("Downloading...");
                    publish(20);
                    File apkFile = versionManager.downloadVersion(version, progress -> {
                        int progressValue = 20 + (int)(progress * 45);
                        publish(progressValue);
                    });
                    statusLabel.setText("Extracting game files...");
                    progressBar.setString("Extracting...");
                    publish(65);
                    versionManager.extractVersion(apkFile, gameDir);
                }
                statusLabel.setText("Preparing game directory...");
                progressBar.setString("Preparing...");
                publish(80);
                versionManager.prepareGameDir(version, gameDir);
                statusLabel.setText("Setting up nickname...");
                progressBar.setString("Setting up...");
                publish(90);
                saveNickname();
                statusLabel.setText("Starting game...");
                progressBar.setString("Launching...");
                publish(95);
                
                String launcherPath = useDefaultLauncher ? null : customLauncherPath;
                Process gameProcess = gameLauncher.launchGame(gameDir, launcherPath, enableDebugging);
                
                SwingUtilities.invokeLater(() -> {
                    switch (postLaunchAction) {
                        case "Minimize Launcher":
                            setExtendedState(JFrame.ICONIFIED);
                            break;
                        case "Close Launcher":
                            System.exit(0);
                            break;
                        case "Do Nothing":
                        default:
                            break;
                    }
                });

                publish(100);
                statusLabel.setText("Successfully launched");
                progressBar.setVisible(false);

                gameProcess.waitFor();
                
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                if (!chunks.isEmpty()) {
                    int progress = chunks.get(chunks.size() - 1);
                    progressBar.setValue(progress);
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Ready");
                    progressBar.setVisible(false);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(NostalgiaLauncherDesktop.this,
                            "Failed to launch game:\n" + e.getMessage(),
                            "Launch Error", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Launch failed");
                    progressBar.setVisible(false);
                } finally {
                    launchButton.setEnabled(true);
                    refreshButton.setEnabled(true);
                    nicknameField.setEnabled(true);
                    versionComboBox.setEnabled(true);
                    versionManager.updateInstalledVersions();
                }
            }
        };
        worker.execute();
    }
    
    private void downloadLauncherComponents(ProgressCallback callback) throws IOException {
        File gameDir = new File("game");
        File executable = new File(gameDir, "ninecraft.exe");
        File dllFile = new File(gameDir, "D3DCompiler_43.dll");
        
        if (executable.exists() && dllFile.exists()) {
            callback.onProgress(1.0);
            return;
        }
        
        statusLabel.setText("Downloading launcher components...");
        progressBar.setString("Downloading components...");
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(DEFAULT_LAUNCHER_URL);
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
                            if (callback != null && totalSize > 0) {
                                callback.onProgress((double) totalBytesRead / totalSize);
                            }
                        }
                    }
                    
                    try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(Files.newInputStream(tempZipFile.toPath()))) {
                        java.util.zip.ZipEntry entry;
                        while ((entry = zis.getNextEntry()) != null) {
                            if (entry.isDirectory()) {
                                continue;
                            }
                            File newFile = new File(gameDir, entry.getName());
                            try (FileOutputStream fos = new FileOutputStream(newFile)) {
                                byte[] buffer = new byte[1024];
                                int len;
                                while ((len = zis.read(buffer)) > 0) {
                                    fos.write(buffer, 0, len);
                                }
                            }
                        }
                    } finally {
                        tempZipFile.delete();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatIntelliJLaf());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new NostalgiaLauncherDesktop().setVisible(true);
        });
    }
}