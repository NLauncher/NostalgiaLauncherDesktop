package net.eqozqq.nostalgialauncherdesktop;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;

public class SettingsDialog extends JDialog {
    private JTextField backgroundPathField;
    private JTextField versionsSourceField;
    private JCheckBox useDefaultSourceCheckbox;
    private JTextField customLauncherField;
    private JCheckBox useDefaultLauncherCheckbox;
    private JComboBox<String> postLaunchActionComboBox;
    private JCheckBox enableDebuggingCheckbox;
    private JSlider scaleSlider;
    private JLabel scaleLabel;
    private JButton saveButton;
    private JButton browseBackgroundButton;
    private JButton browseVersionsButton;
    private JButton browseLauncherButton;
    private JRadioButton urlRadioButton;
    private JRadioButton fileRadioButton;
    private JComboBox<String> themeComboBox;
    private JComboBox<String> languageComboBox;

    private JRadioButton defaultBgRadio;
    private JRadioButton customImageRadio;
    private JRadioButton customColorRadio;
    private JButton chooseColorButton;
    private JPanel colorPreviewPanel;
    private JPanel imageOptionsPanel;
    private JPanel colorOptionsPanel;

    private String customBackgroundPath;
    private String customVersionsSource;
    private boolean useDefaultVersionsSource;
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
    private boolean saved = false;
    private LocaleManager localeManager;

    private final Map<String, String> languageMap = new LinkedHashMap<>();
    private final Map<String, String> postActionMap = new LinkedHashMap<>();
    private final Map<String, String> themeMap = new LinkedHashMap<>();

    private static final String LAST_VERSION = "https://raw.githubusercontent.com/NLauncher/components/refs/heads/main/lastversion.txt";

    public SettingsDialog(JFrame parent, String currentBackgroundPath, String currentVersionsSource, boolean useDefaultVs, String currentCustomLauncherPath, boolean useDefaultLauncher, String currentPostLaunchAction, boolean currentEnableDebugging, double currentScaleFactor, String currentTheme, String currentVersion, String backgroundMode, Color customBackgroundColor, LocaleManager localeManager) {
        super(parent, localeManager.get("dialog.settings.title"), true);
        this.localeManager = localeManager;
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(600, 550);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        this.customBackgroundPath = currentBackgroundPath;
        this.customVersionsSource = currentVersionsSource;
        this.useDefaultVersionsSource = useDefaultVs;
        this.customLauncherPath = currentCustomLauncherPath;
        this.useDefaultLauncher = useDefaultLauncher;
        this.postLaunchAction = currentPostLaunchAction;
        this.enableDebugging = currentEnableDebugging;
        this.scaleFactor = currentScaleFactor;
        this.themeName = currentTheme;
        this.currentVersion = currentVersion;
        this.backgroundMode = backgroundMode;
        this.customBackgroundColor = customBackgroundColor;
        this.language = localeManager.getCurrentLanguage();

        languageMap.put("English", "en");
        languageMap.put("Русский", "ru");
        languageMap.put("Беларуская", "be");
        languageMap.put("Українська", "uk");
        languageMap.put("Português", "pt");
        languageMap.put("简体中文", "zh_CN");

        postActionMap.put("Do Nothing", localeManager.get("combo.postLaunch.doNothing"));
        postActionMap.put("Minimize Launcher", localeManager.get("combo.postLaunch.minimize"));
        postActionMap.put("Close Launcher", localeManager.get("combo.postLaunch.close"));

        themeMap.put("Light", localeManager.get("combo.theme.light"));
        themeMap.put("Dark", localeManager.get("combo.theme.dark"));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.LEFT);
        tabbedPane.addTab(localeManager.get("tab.game"), createGamePanel());
        tabbedPane.addTab(localeManager.get("tab.launcher"), createLauncherPanel());
        tabbedPane.addTab(localeManager.get("tab.about"), createAboutPanel());

        JPanel buttonPanel = new JPanel();
        saveButton = new JButton(localeManager.get("button.save"));
        saveButton.addActionListener(e -> {
            handleSave();
            saved = true;
            dispose();
        });
        buttonPanel.add(saveButton);
        
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(localeManager.get("tab.about"))) {
                buttonPanel.setVisible(false);
            } else {
                buttonPanel.setVisible(true);
            }
        });
    }
    
    private void handleSave() {
        this.useDefaultVersionsSource = useDefaultSourceCheckbox.isSelected();
        this.customVersionsSource = this.useDefaultVersionsSource ? null : versionsSourceField.getText();
        
        this.useDefaultLauncher = useDefaultLauncherCheckbox.isSelected();
        this.customLauncherPath = this.useDefaultLauncher ? null : customLauncherField.getText();
        
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
        this.language = languageMap.get((String)languageComboBox.getSelectedItem());

        if (!useDefaultVersionsSource) {
            if (urlRadioButton.isSelected()) {
                try {
                    new URL(versionsSourceField.getText());
                } catch (MalformedURLException ex) {
                    JOptionPane.showMessageDialog(this, localeManager.get("error.invalidUrl"), localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else if (fileRadioButton.isSelected()) {
                File file = new File(versionsSourceField.getText());
                if (!file.exists() || !file.isFile()) {
                    JOptionPane.showMessageDialog(this, localeManager.get("error.invalidFilePath"), localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        if (!useDefaultLauncher) {
            File file = new File(customLauncherField.getText());
            if (!file.exists() || !file.isFile()) {
                    JOptionPane.showMessageDialog(this, localeManager.get("error.invalidFilePath"), localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
                    return;
            }
        }
    }

    private Font getFont(int style, float size) {
        try (InputStream fontStream = getClass().getResourceAsStream("/MPLUS1p-Regular.ttf")) {
            if (fontStream != null) {
                return Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(style, size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Font("SansSerif", style, (int) size);
    }
    
    private JPanel createGamePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        int gridY = 0;

        JLabel versionsLabel = new JLabel(localeManager.get("label.versionsSource"));
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        contentPanel.add(versionsLabel, gbc);

        JPanel sourceSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        urlRadioButton = new JRadioButton(localeManager.get("radio.url"));
        fileRadioButton = new JRadioButton(localeManager.get("radio.file"));
        ButtonGroup group = new ButtonGroup();
        group.add(urlRadioButton);
        group.add(fileRadioButton);
        sourceSelectionPanel.add(urlRadioButton);
        sourceSelectionPanel.add(fileRadioButton);

        if (useDefaultVersionsSource || (customVersionsSource != null && (customVersionsSource.startsWith("http://") || customVersionsSource.startsWith("https://")))) {
            urlRadioButton.setSelected(true);
        } else {
            fileRadioButton.setSelected(true);
        }

        urlRadioButton.setEnabled(!useDefaultVersionsSource);
        fileRadioButton.setEnabled(!useDefaultVersionsSource);

        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        contentPanel.add(sourceSelectionPanel, gbc);
        
        gridY++;
        versionsSourceField = new JTextField(20);
        versionsSourceField.setText(useDefaultVersionsSource ? "" : customVersionsSource);
        versionsSourceField.setEnabled(!useDefaultVersionsSource);
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        contentPanel.add(versionsSourceField, gbc);

        browseVersionsButton = new JButton(localeManager.get("button.browse"));
        browseVersionsButton.setEnabled(!useDefaultVersionsSource && fileRadioButton.isSelected());
        browseVersionsButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                versionsSourceField.setText(file.getAbsolutePath());
            }
        });
        gbc.gridx = 2;
        gbc.gridy = gridY;
        gbc.weightx = 0.0;
        contentPanel.add(browseVersionsButton, gbc);

        urlRadioButton.addActionListener(e -> {
            versionsSourceField.setEnabled(!useDefaultSourceCheckbox.isSelected());
            browseVersionsButton.setEnabled(false);
        });

        fileRadioButton.addActionListener(e -> {
            versionsSourceField.setEnabled(!useDefaultSourceCheckbox.isSelected());
            browseVersionsButton.setEnabled(true);
        });

        gridY++;
        useDefaultSourceCheckbox = new JCheckBox(localeManager.get("checkbox.useDefaultUrl"));
        useDefaultSourceCheckbox.setSelected(useDefaultVersionsSource);
        useDefaultSourceCheckbox.addActionListener(e -> {
            versionsSourceField.setEnabled(!useDefaultSourceCheckbox.isSelected());
            urlRadioButton.setEnabled(!useDefaultSourceCheckbox.isSelected());
            fileRadioButton.setEnabled(!useDefaultSourceCheckbox.isSelected());
            browseVersionsButton.setEnabled(!useDefaultSourceCheckbox.isSelected() && fileRadioButton.isSelected());
        });
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        contentPanel.add(useDefaultSourceCheckbox, gbc);

        gridY++;
        JLabel versionsInfoLabel = createInfoLabel(localeManager.get("info.customVersions"), localeManager.get("link.learnMore"), "https://nlauncher.github.io/docs/custom-versions-list-source.html");
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        contentPanel.add(versionsInfoLabel, gbc);

        gridY++;
        JLabel customLauncherLabel = new JLabel(localeManager.get("label.customExecutable"));
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.weighty = 0.0;
        contentPanel.add(customLauncherLabel, gbc);

        customLauncherField = new JTextField(20);
        customLauncherField.setText(useDefaultLauncher ? "" : customLauncherPath);
        customLauncherField.setEnabled(!useDefaultLauncher);
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        contentPanel.add(customLauncherField, gbc);

        browseLauncherButton = new JButton(localeManager.get("button.browse"));
        browseLauncherButton.setEnabled(!useDefaultLauncher);
        browseLauncherButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                customLauncherField.setText(file.getAbsolutePath());
            }
        });
        gbc.gridx = 2;
        gbc.gridy = gridY;
        gbc.weightx = 0.0;
        contentPanel.add(browseLauncherButton, gbc);
        
        gridY++;
        useDefaultLauncherCheckbox = new JCheckBox(localeManager.get("checkbox.useDefaultExecutable"));
        useDefaultLauncherCheckbox.setSelected(useDefaultLauncher);
        useDefaultLauncherCheckbox.addActionListener(e -> {
            customLauncherField.setEnabled(!useDefaultLauncherCheckbox.isSelected());
            browseLauncherButton.setEnabled(!useDefaultLauncherCheckbox.isSelected());
        });
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        contentPanel.add(useDefaultLauncherCheckbox, gbc);

        gridY++;
        JLabel launcherInfoLabel = createInfoLabel(localeManager.get("info.customExecutable"), localeManager.get("link.learnMore"), "https://nlauncher.github.io/docs/custom-executable-for-versions.html");
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        contentPanel.add(launcherInfoLabel, gbc);

        gridY++;
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(15, 5, 5, 5);
        gbc.weighty = 0.0;
        contentPanel.add(new JSeparator(), gbc);
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gridY++;
        enableDebuggingCheckbox = new JCheckBox(localeManager.get("checkbox.enableDebugging"));
        enableDebuggingCheckbox.setSelected(this.enableDebugging);
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 3;
        gbc.weighty = 0.0;
        contentPanel.add(enableDebuggingCheckbox, gbc);

        gridY++;
        JPanel filler = new JPanel();
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(filler, gbc);

        mainPanel.add(contentPanel, BorderLayout.NORTH);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        return mainPanel;
    }

    private void updateBackgroundOptions() {
        imageOptionsPanel.setVisible(customImageRadio.isSelected());
        colorOptionsPanel.setVisible(customColorRadio.isSelected());
    }

    private JPanel createLauncherPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        int gridY = 0;

        JLabel postLaunchActionLabel = new JLabel(localeManager.get("label.postLaunchAction"));
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.weighty = 0.0;
        contentPanel.add(postLaunchActionLabel, gbc);

        postLaunchActionComboBox = new JComboBox<>(postActionMap.values().toArray(new String[0]));
        postLaunchActionComboBox.setSelectedItem(postActionMap.get(this.postLaunchAction));
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        contentPanel.add(postLaunchActionComboBox, gbc);
        
        gridY++;
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(15, 5, 5, 5);
        contentPanel.add(new JSeparator(), gbc);
        gbc.insets = new Insets(5, 5, 5, 5);

        gridY++;
        JLabel languageLabel = new JLabel(localeManager.get("label.language"));
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.weighty = 0.0;
        contentPanel.add(languageLabel, gbc);

        languageComboBox = new JComboBox<>(languageMap.keySet().toArray(new String[0]));
        languageMap.forEach((name, code) -> {
            if (code.equals(this.language)) {
                languageComboBox.setSelectedItem(name);
            }
        });
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        contentPanel.add(languageComboBox, gbc);

        gridY++;
        JLabel themeLabel = new JLabel(localeManager.get("label.theme"));
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.weighty = 0.0;
        contentPanel.add(themeLabel, gbc);

        themeComboBox = new JComboBox<>(themeMap.values().toArray(new String[0]));
        themeComboBox.setSelectedItem(themeMap.get(this.themeName));
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        contentPanel.add(themeComboBox, gbc);

        gridY++;
        scaleLabel = new JLabel(localeManager.get("label.interfaceScale", (int)(scaleFactor * 100)));
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.weighty = 0.0;
        contentPanel.add(scaleLabel, gbc);

        scaleSlider = new JSlider(JSlider.HORIZONTAL, 50, 200, (int)(scaleFactor * 100));
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
        gbc.weighty = 0.0;
        contentPanel.add(scaleSlider, gbc);

        gridY++;
        JLabel backgroundLabel = new JLabel(localeManager.get("label.background"));
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        contentPanel.add(backgroundLabel, gbc);

        JPanel radioPanel = new JPanel(new GridLayout(0, 1));
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
        contentPanel.add(radioPanel, gbc);

        defaultBgRadio.addActionListener(e -> updateBackgroundOptions());
        customImageRadio.addActionListener(e -> updateBackgroundOptions());
        customColorRadio.addActionListener(e -> updateBackgroundOptions());

        gridY++;
        imageOptionsPanel = new JPanel(new GridBagLayout());
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
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                backgroundPathField.setText(file.getAbsolutePath());
            }
        });
        imageGbc.gridx = 1;
        imageGbc.weightx = 0;
        imageOptionsPanel.add(browseBackgroundButton, imageGbc);
        
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        contentPanel.add(imageOptionsPanel, gbc);
        
        gridY++;
        colorOptionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        chooseColorButton = new JButton(localeManager.get("button.chooseColor"));
        colorPreviewPanel = new JPanel();
        colorPreviewPanel.setPreferredSize(new Dimension(24, 24));
        colorPreviewPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        if (customBackgroundColor != null) {
            colorPreviewPanel.setBackground(customBackgroundColor);
        }

        chooseColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, localeManager.get("dialog.chooseColor.title"), colorPreviewPanel.getBackground());
            if (newColor != null) {
                colorPreviewPanel.setBackground(newColor);
            }
        });
        colorOptionsPanel.add(chooseColorButton);
        colorOptionsPanel.add(colorPreviewPanel);
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        contentPanel.add(colorOptionsPanel, gbc);

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
        JPanel filler = new JPanel();
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(filler, gbc);

        mainPanel.add(contentPanel, BorderLayout.NORTH);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        return mainPanel;
    }
    
    private JPanel createAboutPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        int gridY = 0;

        JLabel headline1 = new JLabel(localeManager.get("about.headline1"));
        headline1.setFont(getFont(Font.BOLD, 32f));
        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 3;
        contentPanel.add(headline1, gbc);

        JLabel headline2 = new JLabel(localeManager.get("about.headline2"));
        headline2.setFont(getFont(Font.PLAIN, 18f));
        gbc.gridy = gridY++;
        contentPanel.add(headline2, gbc);

        gbc.gridy = gridY++;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(15, 5, 5, 5); 
        contentPanel.add(new JSeparator(), gbc);
        gbc.insets = new Insets(5, 5, 5, 5); 

        JLabel headline3 = new JLabel(localeManager.get("about.headline3"));
        headline3.setFont(getFont(Font.BOLD, 18f));
        gbc.gridy = gridY++;
        contentPanel.add(headline3, gbc);
        
        JLabel link1 = createHyperlink("https://fonts.google.com/icons");
        gbc.gridy = gridY++;
        contentPanel.add(link1, gbc);

        JLabel link2 = createHyperlink("https://www.formdev.com/flatlaf/");
        gbc.gridy = gridY++;
        contentPanel.add(link2, gbc);

        JLabel link3 = createHyperlink("https://github.com/MCPI-Revival/Ninecraft");
        gbc.gridy = gridY++;
        contentPanel.add(link3, gbc);

        JLabel link4 = createHyperlink("https://github.com/zhuowei/SpoutNBT");
        gbc.gridy = gridY++;
        contentPanel.add(link4, gbc);

        JLabel translationCredit = new JLabel("Belarusian translation: Djabał Pažyralnik Kaleniaŭ");
        translationCredit.setFont(getFont(Font.PLAIN, 14f));
        gbc.gridy = gridY++;
        contentPanel.add(translationCredit, gbc);

        gbc.gridy = gridY++;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(15, 5, 5, 5); 
        contentPanel.add(new JSeparator(), gbc);
        gbc.insets = new Insets(5, 5, 5, 5); 

        gbc.gridy = gridY++;
        gbc.weighty = 1.0;
        contentPanel.add(new JPanel(), gbc);
        
        JPanel versionPanel = new JPanel();
        versionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        JLabel versionLabel = new JLabel(localeManager.get("about.currentVersion", currentVersion));
        versionLabel.setFont(getFont(Font.PLAIN, 12f));
        versionPanel.add(versionLabel);

        JLabel updateStatusLabel = new JLabel(localeManager.get("about.update.checking"));
        updateStatusLabel.setFont(getFont(Font.PLAIN, 12f));
        versionPanel.add(updateStatusLabel);

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
                        JLabel updateLink = new JLabel("<html><a href='https://nlauncher.github.io/releases.html#desktop'>" + linkText + "</a></html>");
                        updateLink.setFont(getFont(Font.PLAIN, 12f));
                        updateLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        updateLink.addMouseListener(new MouseAdapter() {
                            public void mouseClicked(MouseEvent e) {
                                try {
                                    Desktop.getDesktop().browse(new URI("https://nlauncher.github.io/releases.html#desktop"));
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

        mainPanel.add(contentPanel, BorderLayout.NORTH);
        mainPanel.add(versionPanel, BorderLayout.SOUTH);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        return mainPanel;
    }
    
    private JLabel createInfoLabel(String text, String linkText, String url) {
        JLabel label = new JLabel("<html><span style='color:gray;'>" + text + "</span><a href='" + url + "'><span style='font-weight:bold;'>" + linkText + "</span></a></html>");
        label.setFont(getFont(Font.PLAIN, 10f));
        label.setForeground(Color.GRAY);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        return label;
    }

    private JLabel createHyperlink(String url) {
        JLabel label = new JLabel("<html><a href='" + url + "'>" + url + "</a></html>");
        label.setFont(getFont(Font.PLAIN, 14f));
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        return label;
    }

    public String getCustomBackgroundPath() { return customBackgroundPath; }
    public String getCustomVersionsSource() { return customVersionsSource; }
    public boolean isUseDefaultVersionsSource() { return useDefaultVersionsSource; }
    public String getCustomLauncherPath() { return customLauncherPath; }
    public boolean isUseDefaultLauncher() { return useDefaultLauncher; }
    public String getPostLaunchAction() { return postLaunchAction; }
    public boolean isEnableDebugging() { return enableDebugging; }
    public double getScaleFactor() { return scaleFactor; }
    public String getThemeName() { return themeName; }
    public boolean isSaved() { return saved; }
    public String getBackgroundMode() { return backgroundMode; }
    public Color getCustomBackgroundColor() { return customBackgroundColor; }
    public String getLanguage() { return language; }
}