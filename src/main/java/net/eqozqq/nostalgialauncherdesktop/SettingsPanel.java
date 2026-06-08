package net.eqozqq.nostalgialauncherdesktop;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SettingsPanel extends JPanel {
    private JTextField backgroundPathField;
    private JComboBox<String> customVersionsSourcesComboBox;
    private List<String> customVersionsSourcesList;
    private JCheckBox useDefaultSourceCheckbox;

    private JRadioButton compiledExeRadio;
    private JRadioButton serverExeRadio;
    private JRadioButton customExeRadio;
    private JComboBox<String> customLauncherComboBox;
    private List<String> customLauncherPathsList;
    private JRadioButton anotherExeRadio;
    private JComboBox<CustomLauncherProfile> anotherLauncherComboBox;
    private List<CustomLauncherProfile> customLauncherProfilesList;
    private String selectedLauncherProfileName;

    private JComboBox<String> postLaunchActionComboBox;
    private JCheckBox enableDebuggingCheckbox;
    private JSlider scaleSlider;
    private JLabel scaleLabel;
    private JButton saveButton;
    private JButton browseBackgroundButton;
    private JComboBox<String> themeComboBox;
    private JComboBox<String> languageComboBox;

    private JRadioButton defaultBgRadio;
    private JRadioButton customImageRadio;
    private JRadioButton customColorRadio;
    private JButton chooseColorButton;
    private JPanel colorPreviewPanel;
    private JPanel imageOptionsPanel;
    private JPanel colorOptionsPanel;

    private JTextField customTranslationPathField;
    private JButton browseTranslationButton;
    private JPanel customTranslationPanel;

    private String customBackgroundPath;
    private String customVersionsSource;
    private boolean useDefaultVersionsSource;

    private String executableSource;
    private String customLauncherPath;
    private boolean useDefaultLauncher;

    private String postLaunchAction;
    private boolean enableDebugging;
    private double scaleFactor;
    private String themeName;
    private String currentVersion;
    private String backgroundMode;
    private Color customBackgroundColor;
    private String language;
    private String customTranslationPath;

    private String githubTranslationUrl;
    private String githubTranslationName;

    private LocaleManager localeManager;
    private SaveListener saveListener;
    private boolean isRefreshingComboBox = false;
    private CardLayout cardLayout;
    private JPanel contentCards;
    private JPanel tabsPanel;
    private TabButton activeTab;
    private TabButton gameTab;
    private TabButton launcherTab;
    private TabButton aboutTab;
    private boolean isDark;

    private final Map<String, String> languageMap = new LinkedHashMap<>();
    private final Map<String, String> postActionMap = new LinkedHashMap<>();
    private final Map<String, String> themeMap = new LinkedHashMap<>();

    private static final String LAST_VERSION = "https://raw.githubusercontent.com/NLauncher/components/refs/heads/main/lastversion.txt";
    private static final String GITHUB_LOCALES_BASE = "https://raw.githubusercontent.com/NLauncher/locales/main/";
    private static final String GITHUB_LOCALES_CONFIG = GITHUB_LOCALES_BASE + "languages.json";

    public interface SaveListener {
        void onSave(SettingsPanel settings);
    }

    public SettingsPanel(String currentBackgroundPath, String currentVersionsSource, boolean useDefaultVs,
            List<String> customVersionsSourcesList,
            String currentExecutableSource, String currentCustomLauncherPath,
            List<String> customLauncherPathsList,
            String currentPostLaunchAction,
            boolean currentEnableDebugging, double currentScaleFactor, String currentTheme, String currentVersion,
            String backgroundMode, Color customBackgroundColor, String currentCustomTranslationPath,
            String currentGithubUrl, String currentGithubName,
            List<CustomLauncherProfile> customLauncherProfilesList,
            String selectedLauncherProfileName,
            LocaleManager localeManager, SaveListener saveListener) {
        this.localeManager = localeManager;
        this.saveListener = saveListener;

        this.customBackgroundPath = currentBackgroundPath;
        this.customVersionsSource = currentVersionsSource;
        this.useDefaultVersionsSource = useDefaultVs;
        this.customVersionsSourcesList = customVersionsSourcesList != null ? customVersionsSourcesList
                : new ArrayList<>();
        this.executableSource = currentExecutableSource;
        this.customLauncherPath = currentCustomLauncherPath;
        this.customLauncherPathsList = customLauncherPathsList != null ? customLauncherPathsList : new ArrayList<>();
        this.customLauncherProfilesList = customLauncherProfilesList != null ? customLauncherProfilesList
                : new ArrayList<>();
        this.selectedLauncherProfileName = selectedLauncherProfileName;
        this.useDefaultLauncher = !"CUSTOM".equals(currentExecutableSource) && !"ANOTHER".equals(currentExecutableSource);
        this.postLaunchAction = currentPostLaunchAction;
        this.enableDebugging = currentEnableDebugging;
        this.scaleFactor = currentScaleFactor;
        this.themeName = currentTheme;
        this.currentVersion = currentVersion;
        this.backgroundMode = backgroundMode;
        this.customBackgroundColor = customBackgroundColor;
        this.language = localeManager.getCurrentLanguage();
        this.customTranslationPath = currentCustomTranslationPath;
        this.githubTranslationUrl = currentGithubUrl;
        this.githubTranslationName = currentGithubName;
        this.isDark = currentTheme.contains("Dark");

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        languageMap.put("English", "en");

        String githubText = "Use translations from repository";
        if (localeManager.has("combo.language.github")) {
            githubText = localeManager.get("combo.language.github");
        }
        if ("github".equals(this.language) && this.githubTranslationName != null) {
            githubText = (localeManager.has("combo.language.github_active")
                    ? localeManager.get("combo.language.github_active")
                    : "Using translation from repository") +
                    " (" + this.githubTranslationName + ")";
        }
        languageMap.put(githubText, "github");

        languageMap.put(localeManager.has("combo.language.custom") ? localeManager.get("combo.language.custom")
                : "Use custom translation", "custom");

        postActionMap.put("Do Nothing", localeManager.get("combo.postLaunch.doNothing"));
        postActionMap.put("Minimize Launcher", localeManager.get("combo.postLaunch.minimize"));
        postActionMap.put("Close Launcher", localeManager.get("combo.postLaunch.close"));

        themeMap.put("Light", localeManager.get("combo.theme.light"));
        themeMap.put("Dark", localeManager.get("combo.theme.dark"));

        tabsPanel = new JPanel();
        tabsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
        tabsPanel.setOpaque(false);
        tabsPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        gameTab = new TabButton(localeManager.get("tab.game"), "GAME");
        launcherTab = new TabButton(localeManager.get("tab.launcher"), "LAUNCHER");
        aboutTab = new TabButton(localeManager.get("tab.about"), "ABOUT");

        tabsPanel.add(gameTab);
        tabsPanel.add(launcherTab);
        tabsPanel.add(aboutTab);

        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.setOpaque(false);

        contentCards.add(createScrollPane(createGamePanel()), "GAME");
        contentCards.add(createScrollPane(createLauncherPanel()), "LAUNCHER");
        contentCards.add(createScrollPane(createAboutPanel()), "ABOUT");

        setActiveTab(gameTab);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        saveButton = new JButton(localeManager.get("button.save"));
        saveButton.setFont(getCustomFont(Font.BOLD, 14f));
        saveButton.setPreferredSize(new Dimension((int) (300 * scaleFactor), (int) (45 * scaleFactor)));
        saveButton.addActionListener(e -> handleSave());

        buttonPanel.add(saveButton);

        add(tabsPanel, BorderLayout.NORTH);
        add(contentCards, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        gameTab.addActionListener(e -> {
            cardLayout.show(contentCards, "GAME");
            setActiveTab(gameTab);
            saveButton.setVisible(true);
        });
        launcherTab.addActionListener(e -> {
            cardLayout.show(contentCards, "LAUNCHER");
            setActiveTab(launcherTab);
            saveButton.setVisible(true);
        });
        aboutTab.addActionListener(e -> {
            cardLayout.show(contentCards, "ABOUT");
            setActiveTab(aboutTab);
            saveButton.setVisible(false);
        });
    }

    private class TabButton extends JButton {
        public TabButton(String text, String command) {
            super(text);
            setActionCommand(command);
            setFont(getCustomFont(Font.PLAIN, 14f));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setForeground(isDark ? new Color(200, 200, 200) : new Color(80, 80, 80));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(120, 40));
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (activeTab == this) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(isDark ? new Color(60, 60, 60, 150) : new Color(200, 200, 200, 150));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
            }
            super.paintComponent(g);
        }
    }

    private void setActiveTab(TabButton tab) {
        if (activeTab != null) {
            activeTab.setFont(getCustomFont(Font.PLAIN, 14f));
            activeTab.setForeground(isDark ? new Color(200, 200, 200) : new Color(80, 80, 80));
        }
        activeTab = tab;
        activeTab.setFont(getCustomFont(Font.BOLD, 15f));
        activeTab.setForeground(isDark ? Color.WHITE : Color.BLACK);
        tabsPanel.repaint();
    }

    public String getActiveTabName() {
        if (activeTab == null)
            return "GAME";
        return activeTab.getActionCommand();
    }

    public void setActiveTabByName(String name) {
        if ("LAUNCHER".equals(name)) {
            cardLayout.show(contentCards, "LAUNCHER");
            setActiveTab(launcherTab);
            saveButton.setVisible(true);
        } else if ("ABOUT".equals(name)) {
            cardLayout.show(contentCards, "ABOUT");
            setActiveTab(aboutTab);
            saveButton.setVisible(false);
        } else {
            cardLayout.show(contentCards, "GAME");
            setActiveTab(gameTab);
            saveButton.setVisible(true);
        }
    }

    private JScrollPane createScrollPane(JPanel content) {
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JPanel createCardPanel() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isDark) {
                    g2d.setColor(new Color(30, 30, 30, 200));
                } else {
                    g2d.setColor(new Color(255, 255, 255, 200));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        return card;
    }

    private void handleSave() {
        this.useDefaultVersionsSource = useDefaultSourceCheckbox.isSelected();
        Object selectedVS = customVersionsSourcesComboBox.getSelectedItem();
        if (selectedVS != null && !localeManager.get("combo.noSources", "No sources added yet").equals(selectedVS)) {
            this.customVersionsSource = (String) selectedVS;
        } else {
            this.customVersionsSource = null;
        }

        if (compiledExeRadio.isSelected()) {
            this.executableSource = "COMPILED";
            this.useDefaultLauncher = true;
        } else if (serverExeRadio.isSelected()) {
            this.executableSource = "SERVER";
            this.useDefaultLauncher = true;
        } else if (customExeRadio.isSelected()) {
            this.executableSource = "CUSTOM";
            this.useDefaultLauncher = false;
        } else {
            this.executableSource = "ANOTHER";
            this.useDefaultLauncher = false;
        }

        Object selectedCL = customLauncherComboBox.getSelectedItem();
        if (selectedCL != null
                && !localeManager.get("combo.noLaunchers", "No executables added yet").equals(selectedCL)) {
            this.customLauncherPath = (String) selectedCL;
        } else {
            this.customLauncherPath = null;
        }

        CustomLauncherProfile selProfile = (CustomLauncherProfile) anotherLauncherComboBox.getSelectedItem();
        if (selProfile != null) {
            this.selectedLauncherProfileName = selProfile.getName();
        } else {
            this.selectedLauncherProfileName = null;
        }
        this.enableDebugging = enableDebuggingCheckbox.isSelected();

        if (defaultBgRadio.isSelected()) {
            this.backgroundMode = "Default";
        } else if (customImageRadio.isSelected()) {
            this.backgroundMode = "Custom Image";
            this.customBackgroundPath = backgroundPathField.getText();
        } else if (customColorRadio.isSelected()) {
            this.backgroundMode = "Custom Color";
            this.customBackgroundColor = colorPreviewPanel.getBackground();
        }

        String selectedPostActionDisplay = (String) postLaunchActionComboBox.getSelectedItem();
        this.postLaunchAction = postActionMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(selectedPostActionDisplay))
                .map(Map.Entry::getKey)
                .findFirst().orElse("Do Nothing");

        String selectedThemeDisplay = (String) themeComboBox.getSelectedItem();
        this.themeName = themeMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(selectedThemeDisplay))
                .map(Map.Entry::getKey)
                .findFirst().orElse("Dark");

        this.scaleFactor = (double) scaleSlider.getValue() / 100.0;

        String selectedLang = (String) languageComboBox.getSelectedItem();
        this.language = languageMap.get(selectedLang);

        if ("custom".equals(this.language)) {
            this.customTranslationPath = customTranslationPathField.getText();
        }

        if (!useDefaultVersionsSource) {
            if (this.customVersionsSource == null) {
                ErrorDialog.showSync(this, localeManager.get("dialog.error.title"),
                        localeManager.get("error.noSourcesSelected",
                                "Please add and select a custom version source first."));
                return;
            }
            if (this.customVersionsSource.startsWith("http://") || this.customVersionsSource.startsWith("https://")) {
                try {
                    new URL(this.customVersionsSource);
                } catch (MalformedURLException ex) {
                    ErrorDialog.showSync(this, localeManager.get("dialog.error.title"),
                            localeManager.get("error.invalidUrl") + "\n\n" + ex.getMessage());
                    return;
                }
            } else {
                File file = new File(this.customVersionsSource);
                if (!file.exists() || !file.isFile()) {
                    ErrorDialog.showSync(this, localeManager.get("dialog.error.title"),
                            localeManager.get("error.invalidFilePath") + "\n\n" + this.customVersionsSource);
                    return;
                }
            }
        }

        if ("CUSTOM".equals(this.executableSource)) {
            if (this.customLauncherPath == null) {
                ErrorDialog.showSync(this, localeManager.get("dialog.error.title"),
                        localeManager.get("error.noExecutableSelected",
                                "Please add and select a custom executable first."));
                return;
            }
            File file = new File(this.customLauncherPath);
            if (!file.exists() || !file.isFile()) {
                ErrorDialog.showSync(this, localeManager.get("dialog.error.title"),
                        localeManager.get("error.invalidFilePath") + "\n\n" + this.customLauncherPath);
                return;
            }
        } else if ("ANOTHER".equals(this.executableSource)) {
            if (this.selectedLauncherProfileName == null) {
                ErrorDialog.showSync(this, localeManager.get("dialog.error.title"),
                        localeManager.get("error.noExecutableSelected",
                                "Please select a custom profile first."));
                return;
            }
        }

        if ("custom".equals(this.language)) {
            File file = new File(customTranslationPathField.getText());
            if (!file.exists() || !file.isFile()) {
                ErrorDialog.showSync(this, localeManager.get("dialog.error.title"),
                        localeManager.get("error.invalidFilePath") + "\n\n" + customTranslationPathField.getText());
                return;
            }
        }

        if (saveListener != null) {
            saveListener.onSave(this);
        }
    }

    private Font getCustomFont(int style, float size) {
        return FontManager.getRegularFont(style, size);
    }

    private JPanel createGamePanel() {
        JPanel card = createCardPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        int gridY = 0;

        JLabel versionsLabel = new JLabel(localeManager.get("label.versionsSource"));
        versionsLabel.setFont(getCustomFont(Font.BOLD, 14f));
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        card.add(versionsLabel, gbc);

        customVersionsSourcesComboBox = new JComboBox<>();
        refreshVersionsComboBox(this.customVersionsSource);
        customVersionsSourcesComboBox.addActionListener(e -> {
            if (isRefreshingComboBox)
                return;
            Object selected = customVersionsSourcesComboBox.getSelectedItem();
            if (localeManager.get("combo.addSourceOption", "+ Add Source...").equals(selected)) {
                String prev = this.customVersionsSource;
                if (prev != null && customVersionsSourcesList.contains(prev)) {
                    customVersionsSourcesComboBox.setSelectedItem(prev);
                } else if (!customVersionsSourcesList.isEmpty()) {
                    customVersionsSourcesComboBox.setSelectedIndex(0);
                } else {
                    customVersionsSourcesComboBox.setSelectedIndex(0);
                }
                SwingUtilities.invokeLater(this::showAddSourceDialog);
            } else if (selected != null
                    && !localeManager.get("combo.noSources", "No sources added yet").equals(selected)) {
                this.customVersionsSource = (String) selected;
            }
        });
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(customVersionsSourcesComboBox, gbc);

        gridY++;
        useDefaultSourceCheckbox = new JCheckBox(localeManager.get("checkbox.useDefaultUrl"));
        useDefaultSourceCheckbox.setSelected(useDefaultVersionsSource);
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(useDefaultSourceCheckbox, gbc);

        gridY++;
        JLabel versionsInfoLabel = createInfoLabel(localeManager.get("info.customVersions"),
                localeManager.get("link.learnMore"),
                "https://nlauncher.github.io/docs/custom-versions-list-source.html");
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        card.add(versionsInfoLabel, gbc);

        gridY++;
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(15, 0, 15, 0);
        card.add(new JSeparator(), gbc);
        gbc.insets = new Insets(8, 8, 8, 8);

        gridY++;
        JLabel exeLabel = new JLabel(localeManager.get("label.executableSource"));
        exeLabel.setFont(getCustomFont(Font.BOLD, 14f));
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 3;
        card.add(exeLabel, gbc);

        gridY++;
        ButtonGroup exeGroup = new ButtonGroup();
        compiledExeRadio = new JRadioButton(localeManager.get("radio.executable.compiled"));
        serverExeRadio = new JRadioButton(localeManager.get("radio.executable.server"));
        customExeRadio = new JRadioButton(localeManager.get("radio.executable.custom"));
        anotherExeRadio = new JRadioButton(localeManager.get("radio.executable.another"));
        exeGroup.add(compiledExeRadio);
        exeGroup.add(serverExeRadio);
        exeGroup.add(customExeRadio);
        exeGroup.add(anotherExeRadio);

        if ("COMPILED".equals(executableSource)) {
            compiledExeRadio.setSelected(true);
        } else if ("CUSTOM".equals(executableSource)) {
            customExeRadio.setSelected(true);
        } else if ("ANOTHER".equals(executableSource)) {
            anotherExeRadio.setSelected(true);
        } else {
            serverExeRadio.setSelected(true);
        }

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 3;
        card.add(compiledExeRadio, gbc);

        gridY++;
        gbc.gridy = gridY;
        card.add(serverExeRadio, gbc);

        gridY++;
        gbc.gridy = gridY;
        card.add(customExeRadio, gbc);

        gridY++;
        customLauncherComboBox = new JComboBox<>();
        refreshLauncherComboBox(this.customLauncherPath);
        customLauncherComboBox.addActionListener(e -> {
            if (isRefreshingComboBox)
                return;
            Object selected = customLauncherComboBox.getSelectedItem();
            if (localeManager.get("combo.addLauncherOption", "+ Add Executable...").equals(selected)) {
                String prev = this.customLauncherPath;
                if (prev != null && customLauncherPathsList.contains(prev)) {
                    customLauncherComboBox.setSelectedItem(prev);
                } else if (!customLauncherPathsList.isEmpty()) {
                    customLauncherComboBox.setSelectedIndex(0);
                } else {
                    customLauncherComboBox.setSelectedIndex(0);
                }
                SwingUtilities.invokeLater(this::showAddLauncherDialog);
            } else if (selected != null
                    && !localeManager.get("combo.noLaunchers", "No executables added yet").equals(selected)) {
                this.customLauncherPath = (String) selected;
            }
        });
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(customLauncherComboBox, gbc);

        gridY++;
        gbc.gridy = gridY;
        card.add(anotherExeRadio, gbc);

        gridY++;
        JPanel profilePanel = new JPanel(new GridBagLayout());
        profilePanel.setOpaque(false);
        GridBagConstraints pGbc = new GridBagConstraints();
        pGbc.insets = new Insets(0, 0, 0, 5);
        pGbc.fill = GridBagConstraints.HORIZONTAL;
        pGbc.gridy = 0;

        pGbc.gridx = 0;
        pGbc.weightx = 1.0;
        anotherLauncherComboBox = new JComboBox<>();
        profilePanel.add(anotherLauncherComboBox, pGbc);

        pGbc.gridx = 1;
        pGbc.weightx = 0.0;
        JButton addProfileBtn = new JButton(localeManager.get("button.addProfile", "Add..."));
        profilePanel.add(addProfileBtn, pGbc);

        pGbc.gridx = 2;
        JButton editProfileBtn = new JButton(localeManager.get("button.editProfile", "Edit..."));
        profilePanel.add(editProfileBtn, pGbc);

        pGbc.gridx = 3;
        pGbc.insets = new Insets(0, 0, 0, 0);
        JButton deleteProfileBtn = new JButton(localeManager.get("button.deleteProfile", "Delete"));
        profilePanel.add(deleteProfileBtn, pGbc);

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(profilePanel, gbc);

        refreshProfileComboBox(this.selectedLauncherProfileName);

        addProfileBtn.addActionListener(e -> {
            CustomLauncherProfileDialog dlg = new CustomLauncherProfileDialog(
                (Window) SwingUtilities.getWindowAncestor(this),
                localeManager,
                null
            );
            dlg.setVisible(true);
            if (dlg.isConfirmed()) {
                CustomLauncherProfile newProfile = dlg.getProfile();
                boolean exists = false;
                for (CustomLauncherProfile p : customLauncherProfilesList) {
                    if (p.getName().equalsIgnoreCase(newProfile.getName())) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    JOptionPane.showMessageDialog(this, localeManager.get("error.launcherProfileExists"), localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
                } else {
                    customLauncherProfilesList.add(newProfile);
                    refreshProfileComboBox(newProfile.getName());
                    this.selectedLauncherProfileName = newProfile.getName();
                }
            }
        });

        editProfileBtn.addActionListener(e -> {
            CustomLauncherProfile selected = (CustomLauncherProfile) anotherLauncherComboBox.getSelectedItem();
            if (selected == null) {
                return;
            }
            CustomLauncherProfileDialog dlg = new CustomLauncherProfileDialog(
                (Window) SwingUtilities.getWindowAncestor(this),
                localeManager,
                selected
            );
            dlg.setVisible(true);
            if (dlg.isConfirmed()) {
                CustomLauncherProfile updatedProfile = dlg.getProfile();
                selected.setName(updatedProfile.getName());
                selected.setExecutablePath(updatedProfile.getExecutablePath());
                selected.setRequiredPaths(updatedProfile.getRequiredPaths());
                selected.setCustomWorldsPath(updatedProfile.getCustomWorldsPath());
                selected.setCustomTexturesPath(updatedProfile.getCustomTexturesPath());
                selected.setCustomOptionsPath(updatedProfile.getCustomOptionsPath());
                refreshProfileComboBox(updatedProfile.getName());
                this.selectedLauncherProfileName = updatedProfile.getName();
            }
        });

        deleteProfileBtn.addActionListener(e -> {
            CustomLauncherProfile selected = (CustomLauncherProfile) anotherLauncherComboBox.getSelectedItem();
            if (selected == null) {
                return;
            }
            int opt = JOptionPane.showConfirmDialog(this, localeManager.get("dialog.deleteWorld.message").replace("world '%s'", "profile '" + selected.getName() + "'"), localeManager.get("dialog.deleteWorld.title"), JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                customLauncherProfilesList.remove(selected);
                String nextSel = customLauncherProfilesList.isEmpty() ? null : customLauncherProfilesList.get(0).getName();
                refreshProfileComboBox(nextSel);
                this.selectedLauncherProfileName = nextSel;
            }
        });

        gridY++;
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(15, 0, 15, 0);
        card.add(new JSeparator(), gbc);
        gbc.insets = new Insets(8, 8, 8, 8);

        gridY++;
        enableDebuggingCheckbox = new JCheckBox(localeManager.get("checkbox.enableDebugging"));
        enableDebuggingCheckbox.setSelected(this.enableDebugging);
        enableDebuggingCheckbox.setFont(getCustomFont(Font.BOLD, 14f));
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 3;
        card.add(enableDebuggingCheckbox, gbc);

        gridY++;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        card.add(filler, gbc);

        return card;
    }

    private void updateBackgroundOptions() {
        imageOptionsPanel.setVisible(customImageRadio.isSelected());
        colorOptionsPanel.setVisible(customColorRadio.isSelected());
    }

    private JPanel createLauncherPanel() {
        JPanel card = createCardPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        int gridY = 0;

        JLabel postLaunchActionLabel = new JLabel(localeManager.get("label.postLaunchAction"));
        postLaunchActionLabel.setFont(getCustomFont(Font.BOLD, 14f));
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        card.add(postLaunchActionLabel, gbc);

        postLaunchActionComboBox = new JComboBox<>(postActionMap.values().toArray(new String[0]));
        postLaunchActionComboBox.setSelectedItem(postActionMap.get(this.postLaunchAction));
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        card.add(postLaunchActionComboBox, gbc);

        gridY++;
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(15, 0, 15, 0);
        card.add(new JSeparator(), gbc);
        gbc.insets = new Insets(8, 8, 8, 8);

        gridY++;
        JLabel languageLabel = new JLabel(localeManager.get("label.language"));
        languageLabel.setFont(getCustomFont(Font.BOLD, 14f));
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        card.add(languageLabel, gbc);

        languageComboBox = new JComboBox<>(languageMap.keySet().toArray(new String[0]));
        languageMap.forEach((name, code) -> {
            if (code.equals(this.language)) {
                languageComboBox.setSelectedItem(name);
            }
        });

        languageComboBox.addActionListener(e -> {
            String selected = (String) languageComboBox.getSelectedItem();
            String code = languageMap.get(selected);

            if ("github".equals(code)) {
                showGitHubTranslationsDialog();
            }

            boolean isCustom = "custom".equals(code);
            customTranslationPanel.setVisible(isCustom);
        });

        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        card.add(languageComboBox, gbc);

        gridY++;
        customTranslationPanel = new JPanel(new GridBagLayout());
        customTranslationPanel.setOpaque(false);
        GridBagConstraints customGbc = new GridBagConstraints();
        customGbc.insets = new Insets(0, 0, 0, 0);

        customTranslationPathField = new JTextField(15);
        customTranslationPathField.setText(customTranslationPath != null ? customTranslationPath : "");
        customGbc.gridx = 0;
        customGbc.gridy = 0;
        customGbc.weightx = 1.0;
        customGbc.fill = GridBagConstraints.HORIZONTAL;
        customGbc.insets = new Insets(0, 0, 0, 5);
        customTranslationPanel.add(customTranslationPathField, customGbc);

        browseTranslationButton = new JButton(localeManager.get("button.browse"));
        browseTranslationButton.addActionListener(e -> {
            File file = NativeFileChooser.chooseFile(this,
                    localeManager.get("label.customTranslation", "Custom Translation"),
                    new String[]{".json"}, "*.json");
            if (file != null) {
                customTranslationPathField.setText(file.getAbsolutePath());
            }
        });
        customGbc.gridx = 1;
        customGbc.weightx = 0;
        customTranslationPanel.add(browseTranslationButton, customGbc);

        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        card.add(customTranslationPanel, gbc);

        boolean isCustomLanguage = "custom".equals(languageMap.get((String) languageComboBox.getSelectedItem()));
        customTranslationPanel.setVisible(isCustomLanguage);

        gridY++;
        JLabel themeLabel = new JLabel(localeManager.get("label.theme"));
        themeLabel.setFont(getCustomFont(Font.BOLD, 14f));
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        card.add(themeLabel, gbc);

        themeComboBox = new JComboBox<>(themeMap.values().toArray(new String[0]));
        themeComboBox.setSelectedItem(themeMap.get(this.themeName));
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        card.add(themeComboBox, gbc);

        gridY++;
        scaleLabel = new JLabel(localeManager.get("label.interfaceScale", (int) (scaleFactor * 100)));
        scaleLabel.setFont(getCustomFont(Font.BOLD, 14f));
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        card.add(scaleLabel, gbc);

        scaleSlider = new JSlider(JSlider.HORIZONTAL, 50, 200, (int) (scaleFactor * 100));
        scaleSlider.setOpaque(false);
        scaleSlider.setMajorTickSpacing(50);
        scaleSlider.setMinorTickSpacing(10);
        scaleSlider.setPaintTicks(true);
        scaleSlider.setPaintLabels(true);
        scaleSlider.setSnapToTicks(true);
        scaleSlider.addChangeListener(e -> {
            int value = scaleSlider.getValue();
            scaleLabel.setText(localeManager.get("label.interfaceScale", value));
        });
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        card.add(scaleSlider, gbc);

        gridY++;
        JLabel backgroundLabel = new JLabel(localeManager.get("label.background"));
        backgroundLabel.setFont(getCustomFont(Font.BOLD, 14f));
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        card.add(backgroundLabel, gbc);

        JPanel radioPanel = new JPanel(new GridLayout(0, 1));
        radioPanel.setOpaque(false);
        defaultBgRadio = new JRadioButton(localeManager.get("radio.bg.default"));
        customImageRadio = new JRadioButton(localeManager.get("radio.bg.customImage"));
        customColorRadio = new JRadioButton(localeManager.get("radio.bg.customColor"));
        ButtonGroup bgGroup = new ButtonGroup();
        bgGroup.add(defaultBgRadio);
        bgGroup.add(customImageRadio);
        bgGroup.add(customColorRadio);
        radioPanel.add(defaultBgRadio);
        radioPanel.add(customImageRadio);
        radioPanel.add(customColorRadio);

        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        card.add(radioPanel, gbc);

        defaultBgRadio.addActionListener(e -> updateBackgroundOptions());
        customImageRadio.addActionListener(e -> updateBackgroundOptions());
        customColorRadio.addActionListener(e -> updateBackgroundOptions());

        gridY++;
        imageOptionsPanel = new JPanel(new GridBagLayout());
        imageOptionsPanel.setOpaque(false);
        GridBagConstraints imageGbc = new GridBagConstraints();
        backgroundPathField = new JTextField(15);
        backgroundPathField.setText(customBackgroundPath != null ? customBackgroundPath : "");
        imageGbc.gridx = 0;
        imageGbc.gridy = 0;
        imageGbc.weightx = 1.0;
        imageGbc.fill = GridBagConstraints.HORIZONTAL;
        imageGbc.insets = new Insets(0, 0, 0, 5);
        imageOptionsPanel.add(backgroundPathField, imageGbc);

        browseBackgroundButton = new JButton(localeManager.get("button.browse"));
        browseBackgroundButton.addActionListener(e -> {
            File file = NativeFileChooser.chooseFile(this,
                    localeManager.get("label.backgroundImage", "Background Image"),
                    new String[]{".png", ".jpg", ".jpeg", ".gif"}, "*.png;*.jpg;*.jpeg;*.gif");
            if (file != null) {
                backgroundPathField.setText(file.getAbsolutePath());
            }
        });
        imageGbc.gridx = 1;
        imageGbc.weightx = 0;
        imageOptionsPanel.add(browseBackgroundButton, imageGbc);

        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        card.add(imageOptionsPanel, gbc);

        gridY++;
        colorOptionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        colorOptionsPanel.setOpaque(false);
        chooseColorButton = new JButton(localeManager.get("button.chooseColor"));
        colorPreviewPanel = new JPanel();
        colorPreviewPanel.setPreferredSize(new Dimension(24, 24));
        colorPreviewPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        if (customBackgroundColor != null) {
            colorPreviewPanel.setBackground(customBackgroundColor);
        }

        chooseColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, localeManager.get("dialog.chooseColor.title"),
                    colorPreviewPanel.getBackground());
            if (newColor != null) {
                colorPreviewPanel.setBackground(newColor);
            }
        });
        colorOptionsPanel.add(chooseColorButton);
        colorOptionsPanel.add(colorPreviewPanel);
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        card.add(colorOptionsPanel, gbc);

        switch (backgroundMode) {
            case "Custom Image":
                customImageRadio.setSelected(true);
                break;
            case "Custom Color":
                customColorRadio.setSelected(true);
                break;
            default:
                defaultBgRadio.setSelected(true);
                break;
        }

        updateBackgroundOptions();

        gridY++;
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(15, 0, 15, 0);
        card.add(new JSeparator(), gbc);
        gbc.insets = new Insets(8, 8, 8, 8);

        gridY++;
        JLabel cacheLabel = new JLabel(localeManager.get("label.cacheAndStorage"));
        cacheLabel.setFont(getCustomFont(Font.BOLD, 14f));
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        card.add(cacheLabel, gbc);

        JButton clearCacheButton = new JButton(localeManager.get("button.clearCache"));
        clearCacheButton.setFont(getCustomFont(Font.PLAIN, 14f));
        clearCacheButton.setPreferredSize(new Dimension((int) (200 * scaleFactor), (int) (45 * scaleFactor)));
        clearCacheButton.addActionListener(e -> showClearCacheDialog());
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        card.add(clearCacheButton, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        gridY++;
        JLabel folderLabel = new JLabel(localeManager.get("label.launcherFolder", "Launcher Folder"));
        folderLabel.setFont(getCustomFont(Font.BOLD, 14f));
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        card.add(folderLabel, gbc);

        JButton openFolderButton = new JButton(localeManager.get("button.openFolder", "Open Folder"));
        openFolderButton.setFont(getCustomFont(Font.PLAIN, 14f));
        openFolderButton.setPreferredSize(new Dimension((int) (200 * scaleFactor), (int) (45 * scaleFactor)));
        openFolderButton.addActionListener(e -> openLauncherFolder());
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        card.add(openFolderButton, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        gridY++;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        card.add(filler, gbc);

        return card;
    }

    private static class RemoteLocale {
        String name;
        String file;

        @Override
        public String toString() {
            return name;
        }
    }

    private void showGitHubTranslationsDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Repository Translations", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 400);
        dialog.setLocationRelativeTo(this);

        DefaultListModel<RemoteLocale> listModel = new DefaultListModel<>();
        JList<RemoteLocale> list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dialog.add(new JScrollPane(list), BorderLayout.CENTER);

        JLabel statusLabel = new JLabel("Loading...");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.add(statusLabel, BorderLayout.NORTH);

        JButton selectButton = new JButton(localeManager.get("button.select"));
        selectButton.setEnabled(false);
        selectButton.addActionListener(e -> {
            RemoteLocale selected = list.getSelectedValue();
            if (selected != null) {
                this.githubTranslationUrl = GITHUB_LOCALES_BASE + selected.file;
                this.githubTranslationName = selected.name;

                String label = (localeManager.has("combo.language.github_active")
                        ? localeManager.get("combo.language.github_active")
                        : "Using translation from repository") +
                        " (" + this.githubTranslationName + ")";

                DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) languageComboBox.getModel();
                String selectedItem = (String) languageComboBox.getSelectedItem();

                languageMap.remove(selectedItem);
                languageMap.put(label, "github");

                List<String> items = new ArrayList<>();
                for (int i = 0; i < model.getSize(); i++) {
                    String item = model.getElementAt(i);
                    if (!item.equals(selectedItem)) {
                        items.add(item);
                    }
                }

                int insertPos = items.size() - 1;
                items.add(insertPos, label);

                model.removeAllElements();
                for (String item : items) {
                    model.addElement(item);
                }
                model.setSelectedItem(label);

                dialog.dispose();
            }
        });
        dialog.add(selectButton, BorderLayout.SOUTH);

        list.addListSelectionListener(e -> selectButton.setEnabled(list.getSelectedValue() != null));

        SwingWorker<List<RemoteLocale>, Void> worker = new SwingWorker<List<RemoteLocale>, Void>() {
            @Override
            protected List<RemoteLocale> doInBackground() throws Exception {
                URL url = new URL(GITHUB_LOCALES_CONFIG);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<RemoteLocale>>() {
                    }.getType();
                    return gson.fromJson(reader, listType);
                }
            }

            @Override
            protected void done() {
                try {
                    List<RemoteLocale> files = get();
                    for (RemoteLocale f : files) {
                        listModel.addElement(f);
                    }
                    statusLabel.setText("Select a translation");
                } catch (Exception ex) {
                    statusLabel.setText("Error loading translations");
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();

        dialog.setVisible(true);

        if (this.githubTranslationUrl == null) {
            languageComboBox.setSelectedItem("English");
        }
    }

    private JPanel createAboutPanel() {
        JPanel card = createCardPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        int gridY = 0;

        JLabel headline1 = new JLabel(
                localeManager.get("about.headline1") + " " + localeManager.get("about.headline2"));
        headline1.setFont(getCustomFont(Font.BOLD, 28f));
        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 3;
        card.add(headline1, gbc);

        gbc.gridy = gridY++;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(15, 0, 15, 0);
        card.add(new JSeparator(), gbc);
        gbc.insets = new Insets(8, 8, 8, 8);

        JLabel headline3 = new JLabel(localeManager.get("about.headline3"));
        headline3.setFont(getCustomFont(Font.BOLD, 18f));
        gbc.gridy = gridY++;
        card.add(headline3, gbc);

        JLabel link1 = createHyperlink("https://github.com/MCPI-Revival/Ninecraft");
        gbc.gridy = gridY++;
        card.add(link1, gbc);

        JLabel link2 = createHyperlink("https://github.com/zhuowei/SpoutNBT");
        gbc.gridy = gridY++;
        card.add(link2, gbc);

        JLabel link3 = createHyperlink("https://phosphoricons.com");
        gbc.gridy = gridY++;
        card.add(link3, gbc);

        JLabel link4 = createHyperlink("https://www.formdev.com/flatlaf/");
        gbc.gridy = gridY++;
        card.add(link4, gbc);

        gbc.gridy = gridY++;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(15, 0, 15, 0);
        card.add(new JSeparator(), gbc);
        gbc.insets = new Insets(8, 8, 8, 8);

        JPanel versionPanel = new JPanel();
        versionPanel.setOpaque(false);
        versionPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 0));
        JLabel versionLabel = new JLabel(localeManager.get("about.currentVersion", currentVersion));
        versionLabel.setFont(getCustomFont(Font.PLAIN, 12f));
        versionPanel.add(versionLabel);

        JLabel updateStatusLabel = new JLabel(localeManager.get("about.update.checking"));
        updateStatusLabel.setFont(getCustomFont(Font.PLAIN, 12f));
        versionPanel.add(updateStatusLabel);

        gbc.gridy = gridY++;
        gbc.gridwidth = 3;
        card.add(versionPanel, gbc);

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                URL url = new URL(LAST_VERSION);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    return reader.readLine().trim();
                } finally {
                    connection.disconnect();
                }
            }

            @Override
            protected void done() {
                try {
                    String latestVersion = get();
                    if (currentVersion.equals(latestVersion)) {
                        updateStatusLabel.setText(localeManager.get("about.update.upToDate"));
                        updateStatusLabel.setForeground(new Color(76, 175, 80));
                    } else {
                        String linkText = localeManager.get("about.update.available");
                        JLabel updateLink = new JLabel(
                                "<html><a href='https://nlauncher.github.io/releases.html#desktop'>" + linkText
                                        + "</a></html>");
                        updateLink.setFont(getCustomFont(Font.PLAIN, 12f));
                        updateLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        updateLink.addMouseListener(new MouseAdapter() {
                            public void mouseClicked(MouseEvent e) {
                                try {
                                    Desktop.getDesktop()
                                            .browse(new URI("https://nlauncher.github.io/releases.html#desktop"));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                        versionPanel.remove(updateStatusLabel);
                        versionPanel.add(updateLink);
                        versionPanel.revalidate();
                        versionPanel.repaint();
                    }
                } catch (Exception e) {
                    updateStatusLabel.setText(localeManager.get("about.update.error"));
                    updateStatusLabel.setForeground(Color.RED);
                }
            }
        };
        worker.execute();

        gbc.gridy = gridY++;
        gbc.weighty = 1.0;
        card.add(new JPanel() {
            {
                setOpaque(false);
            }
        }, gbc);

        return card;
    }

    private JLabel createInfoLabel(String text, String linkText, String url) {
        JLabel label = new JLabel("<html><span style='color:gray;'>" + text + "</span><a href='" + url
                + "'><span style='font-weight:bold;'>" + linkText + "</span></a></html>");
        label.setFont(getCustomFont(Font.PLAIN, 10f));
        label.setForeground(Color.GRAY);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                NostalgiaLauncherDesktop.openURL(url);
            }
        });
        return label;
    }

    private JLabel createHyperlink(String url) {
        JLabel label = new JLabel("<html><a href='" + url + "'>" + url + "</a></html>");
        label.setFont(getCustomFont(Font.PLAIN, 14f));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NostalgiaLauncherDesktop.openURL(url);
            }
        });

        return label;
    }

    private void openLinkSilently(String url) {
        NostalgiaLauncherDesktop.openURL(url);
    }

    public String getCustomBackgroundPath() {
        return customBackgroundPath;
    }

    public String getCustomVersionsSource() {
        return customVersionsSource;
    }

    public boolean isUseDefaultVersionsSource() {
        return useDefaultVersionsSource;
    }

    public String getCustomLauncherPath() {
        return customLauncherPath;
    }

    public boolean isUseDefaultLauncher() {
        return useDefaultLauncher;
    }

    public String getPostLaunchAction() {
        return postLaunchAction;
    }

    public boolean isEnableDebugging() {
        return enableDebugging;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public String getThemeName() {
        return themeName;
    }

    public String getBackgroundMode() {
        return backgroundMode;
    }

    public Color getCustomBackgroundColor() {
        return customBackgroundColor;
    }

    public String getLanguage() {
        return language;
    }

    public String getCustomTranslationPath() {
        return customTranslationPath;
    }

    public String getExecutableSource() {
        return executableSource;
    }

    public String getGithubTranslationUrl() {
        return githubTranslationUrl;
    }

    public String getGithubTranslationName() {
        return githubTranslationName;
    }

    private void showClearCacheDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                localeManager.get("dialog.clearCache.title"), true);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel infoLabel = new JLabel(localeManager.get("dialog.clearCache.info"));
        infoLabel.setFont(getCustomFont(Font.BOLD, 14f));
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(infoLabel);
        content.add(Box.createVerticalStrut(10));

        JCheckBox clearApks = new JCheckBox(localeManager.get("checkbox.clearApks"));
        clearApks.setFont(getCustomFont(Font.PLAIN, 13f));
        clearApks.setSelected(true);
        clearApks.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(clearApks);
        content.add(Box.createVerticalStrut(5));

        JCheckBox clearVersionsList = new JCheckBox(localeManager.get("checkbox.clearVersionsList"));
        clearVersionsList.setFont(getCustomFont(Font.PLAIN, 13f));
        clearVersionsList.setSelected(true);
        clearVersionsList.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(clearVersionsList);
        content.add(Box.createVerticalStrut(5));

        JCheckBox clearCustomVersions = new JCheckBox(localeManager.get("checkbox.clearCustomVersions"));
        clearCustomVersions.setFont(getCustomFont(Font.PLAIN, 13f));
        clearCustomVersions.setSelected(false);
        clearCustomVersions.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(clearCustomVersions);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBorder(new EmptyBorder(10, 15, 15, 15));

        JButton cancelButton = new JButton(localeManager.get("button.cancel"));
        cancelButton.addActionListener(e -> dialog.dispose());

        JButton confirmButton = new JButton(localeManager.get("button.clearCache"));
        confirmButton.setFont(getCustomFont(Font.BOLD, 13f));
        confirmButton.addActionListener(e -> {
            boolean apks = clearApks.isSelected();
            boolean list = clearVersionsList.isSelected();
            boolean custom = clearCustomVersions.isSelected();

            if (apks) {
                File apksDir = new File(net.eqozqq.nostalgialauncherdesktop.Instances.InstanceManager.getInstance()
                        .resolvePath("cache/versions"));
                if (apksDir.exists() && apksDir.isDirectory()) {
                    File[] files = apksDir.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.isFile()) {
                                f.delete();
                            }
                        }
                    }
                }
            }
            if (list) {
                File listFile = new File(net.eqozqq.nostalgialauncherdesktop.Instances.InstanceManager.getInstance()
                        .resolvePath("cache/versions_list.json"));
                if (listFile.exists()) {
                    listFile.delete();
                }
            }
            if (custom) {
                File customFile = new File(net.eqozqq.nostalgialauncherdesktop.Instances.InstanceManager.getInstance()
                        .resolvePath("custom_versions.json"));
                if (customFile.exists()) {
                    customFile.delete();
                }
            }

            JOptionPane.showMessageDialog(dialog, localeManager.get("dialog.clearCache.success"));
            dialog.dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void refreshVersionsComboBox(String selectedValue) {
        isRefreshingComboBox = true;
        customVersionsSourcesComboBox.removeAllItems();
        if (customVersionsSourcesList.isEmpty()) {
            customVersionsSourcesComboBox.addItem(localeManager.get("combo.noSources", "No sources added yet"));
        } else {
            for (String src : customVersionsSourcesList) {
                customVersionsSourcesComboBox.addItem(src);
            }
        }
        customVersionsSourcesComboBox.addItem(localeManager.get("combo.addSourceOption", "+ Add Source..."));

        if (selectedValue != null && customVersionsSourcesList.contains(selectedValue)) {
            customVersionsSourcesComboBox.setSelectedItem(selectedValue);
        } else if (!customVersionsSourcesList.isEmpty()) {
            customVersionsSourcesComboBox.setSelectedIndex(0);
        } else {
            customVersionsSourcesComboBox.setSelectedIndex(0);
        }
        isRefreshingComboBox = false;
    }

    private void showAddSourceDialog() {
        JPanel addPanel = new JPanel(new GridBagLayout());
        GridBagConstraints apGbc = new GridBagConstraints();
        apGbc.insets = new Insets(5, 5, 5, 5);
        apGbc.fill = GridBagConstraints.HORIZONTAL;

        JRadioButton urlOpt = new JRadioButton(localeManager.get("radio.url", "URL"), true);
        JRadioButton fileOpt = new JRadioButton(localeManager.get("radio.file", "File"));
        ButtonGroup optGroup = new ButtonGroup();
        optGroup.add(urlOpt);
        optGroup.add(fileOpt);

        JPanel radioPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        radioPnl.add(urlOpt);
        radioPnl.add(fileOpt);

        apGbc.gridx = 0;
        apGbc.gridy = 0;
        apGbc.gridwidth = 2;
        addPanel.add(radioPnl, apGbc);

        JTextField pathFld = new JTextField(25);
        pathFld.setPreferredSize(new Dimension(0, (int) (45 * scaleFactor)));
        apGbc.gridx = 0;
        apGbc.gridy = 1;
        apGbc.gridwidth = 1;
        apGbc.weightx = 1.0;
        addPanel.add(pathFld, apGbc);

        JButton browseBtn = new JButton(localeManager.get("button.browse", "Browse..."));
        browseBtn.setPreferredSize(new Dimension((int) (100 * scaleFactor), (int) (45 * scaleFactor)));
        browseBtn.setEnabled(false);
        apGbc.gridx = 1;
        apGbc.gridy = 1;
        apGbc.gridwidth = 1;
        apGbc.weightx = 0.0;
        addPanel.add(browseBtn, apGbc);

        urlOpt.addActionListener(ev -> browseBtn.setEnabled(false));
        fileOpt.addActionListener(ev -> browseBtn.setEnabled(true));

        browseBtn.addActionListener(ev -> {
            File file = NativeFileChooser.chooseFile(addPanel,
                    localeManager.get("dialog.addSource.title", "Add Source"),
                    new String[]{".apk"}, "*.apk");
            if (file != null) {
                pathFld.setText(file.getAbsolutePath());
            }
        });

        int result = JOptionPane.showConfirmDialog(this, addPanel,
                localeManager.get("dialog.addSource.title", "Add Source"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String val = pathFld.getText().trim();
            if (!val.isEmpty()) {
                if (!customVersionsSourcesList.contains(val)) {
                    customVersionsSourcesList.add(val);
                }
                refreshVersionsComboBox(val);
            }
        }
    }

    private void refreshLauncherComboBox(String selectedValue) {
        isRefreshingComboBox = true;
        customLauncherComboBox.removeAllItems();
        if (customLauncherPathsList.isEmpty()) {
            customLauncherComboBox.addItem(localeManager.get("combo.noLaunchers", "No executables added yet"));
        } else {
            for (String path : customLauncherPathsList) {
                customLauncherComboBox.addItem(path);
            }
        }
        customLauncherComboBox.addItem(localeManager.get("combo.addLauncherOption", "+ Add Executable..."));

        if (selectedValue != null && customLauncherPathsList.contains(selectedValue)) {
            customLauncherComboBox.setSelectedItem(selectedValue);
        } else if (!customLauncherPathsList.isEmpty()) {
            customLauncherComboBox.setSelectedIndex(0);
        } else {
            customLauncherComboBox.setSelectedIndex(0);
        }
        isRefreshingComboBox = false;
    }

    private void showAddLauncherDialog() {
        File file = NativeFileChooser.chooseFile(this,
                localeManager.get("dialog.addLauncher.title", "Add Launcher"),
                new String[]{".exe"}, "*.exe;*");
        if (file != null) {
            String path = file.getAbsolutePath();
            if (!customLauncherPathsList.contains(path)) {
                customLauncherPathsList.add(path);
            }
            refreshLauncherComboBox(path);
        }
    }

    private void openLauncherFolder() {
        try {
            File folder = new File(net.eqozqq.nostalgialauncherdesktop.Instances.InstanceManager.getInstance().resolvePath(""));
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(folder);
            } else {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    new ProcessBuilder("explorer.exe", folder.getAbsolutePath()).start();
                } else if (os.contains("mac")) {
                    new ProcessBuilder("open", folder.getAbsolutePath()).start();
                } else {
                    new ProcessBuilder("xdg-open", folder.getAbsolutePath()).start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getCustomVersionsSourcesList() {
        return customVersionsSourcesList;
    }

    public List<String> getCustomLauncherPathsList() {
        return customLauncherPathsList;
    }

    public List<CustomLauncherProfile> getCustomLauncherProfilesList() {
        return customLauncherProfilesList;
    }

    public String getSelectedLauncherProfileName() {
        return selectedLauncherProfileName;
    }

    private void refreshProfileComboBox(String selectedName) {
        anotherLauncherComboBox.removeAllItems();
        if (customLauncherProfilesList != null) {
            for (CustomLauncherProfile p : customLauncherProfilesList) {
                anotherLauncherComboBox.addItem(p);
            }
        }
        if (selectedName != null && anotherLauncherComboBox.getItemCount() > 0) {
            for (int i = 0; i < anotherLauncherComboBox.getItemCount(); i++) {
                if (anotherLauncherComboBox.getItemAt(i).getName().equals(selectedName)) {
                    anotherLauncherComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
    }
}