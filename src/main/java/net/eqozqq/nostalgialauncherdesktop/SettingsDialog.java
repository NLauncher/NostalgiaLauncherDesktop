package net.eqozqq.nostalgialauncherdesktop;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
    private boolean saved = false;

    private static final String LAST_VERSION = "https://raw.githubusercontent.com/NLauncher/components/refs/heads/main/lastversion.txt";

    public SettingsDialog(JFrame parent, String currentBackgroundPath, String currentVersionsSource, boolean useDefaultVs, String currentCustomLauncherPath, boolean useDefaultLauncher, String currentPostLaunchAction, boolean currentEnableDebugging, double currentScaleFactor, String currentTheme, String currentVersion, String backgroundMode, Color customBackgroundColor) {
        super(parent, "Settings", true);
        setPreferredSize(new Dimension(600, 550));
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

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.LEFT);
        tabbedPane.addTab("Game", createGamePanel());
        tabbedPane.addTab("Launcher", createLauncherPanel());
        tabbedPane.addTab("About", createAboutPanel());

        JPanel buttonPanel = new JPanel();
        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            this.useDefaultVersionsSource = useDefaultSourceCheckbox.isSelected();
            if (!this.useDefaultVersionsSource) {
                this.customVersionsSource = versionsSourceField.getText();
            } else {
                this.customVersionsSource = null;
            }
            
            this.useDefaultLauncher = useDefaultLauncherCheckbox.isSelected();
            if (!this.useDefaultLauncher) {
                this.customLauncherPath = customLauncherField.getText();
            } else {
                this.customLauncherPath = null;
            }
            this.enableDebugging = enableDebuggingCheckbox.isSelected();

            if (defaultBgRadio.isSelected()) {
                this.backgroundMode = "Default";
                this.customBackgroundPath = null;
                this.customBackgroundColor = null;
            } else if (customImageRadio.isSelected()) {
                this.backgroundMode = "Custom Image";
                this.customBackgroundPath = backgroundPathField.getText();
                this.customBackgroundColor = null;
            } else if (customColorRadio.isSelected()) {
                this.backgroundMode = "Custom Color";
                this.customBackgroundPath = null;
                this.customBackgroundColor = colorPreviewPanel.getBackground();
            }

            this.postLaunchAction = (String) postLaunchActionComboBox.getSelectedItem();
            this.scaleFactor = (double) scaleSlider.getValue() / 100.0;
            this.themeName = (String) themeComboBox.getSelectedItem();

            if (!useDefaultVersionsSource) {
                if (urlRadioButton.isSelected()) {
                    try {
                        new URL(versionsSourceField.getText());
                    } catch (MalformedURLException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid URL for versions source.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else if (fileRadioButton.isSelected()) {
                    File file = new File(versionsSourceField.getText());
                    if (!file.exists() || !file.isFile()) {
                        JOptionPane.showMessageDialog(this, "Invalid file path for versions source.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            if (!useDefaultLauncher) {
                File file = new File(customLauncherField.getText());
                if (!file.exists() || !file.isFile()) {
                        JOptionPane.showMessageDialog(this, "Invalid file path for custom launcher.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                }
            }
            
            saved = true;
            dispose();
        });
        buttonPanel.add(saveButton);
        
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);

        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals("About")) {
                buttonPanel.remove(saveButton);
                buttonPanel.revalidate();
                buttonPanel.repaint();
            } else {
                if (!java.util.Arrays.asList(buttonPanel.getComponents()).contains(saveButton)) {
                    buttonPanel.add(saveButton);
                    buttonPanel.revalidate();
                    buttonPanel.repaint();
                }
            }
        });
    }
    
    private JPanel createGamePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        int gridY = 0;

        JLabel versionsLabel = new JLabel("Versions source:");
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        contentPanel.add(versionsLabel, gbc);

        JPanel sourceSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        urlRadioButton = new JRadioButton("URL");
        fileRadioButton = new JRadioButton("File");
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

        browseVersionsButton = new JButton("Browse...");
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
        useDefaultSourceCheckbox = new JCheckBox("Use default URL");
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
        JLabel versionsInfoLabel = createInfoLabel("Add your own versions list source. ", "Learn more", "https://nlauncher.github.io/docs/custom-versions-list-source.html");
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        contentPanel.add(versionsInfoLabel, gbc);

        gridY++;
        JLabel customLauncherLabel = new JLabel("Custom executable for launching versions:");
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

        browseLauncherButton = new JButton("Browse...");
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
        useDefaultLauncherCheckbox = new JCheckBox("Use default executable");
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
        JLabel launcherInfoLabel = createInfoLabel("Add your executable for launching versions. ", "Learn more", "https://nlauncher.github.io/docs/custom-executable-for-versions.html");
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
        enableDebuggingCheckbox = new JCheckBox("Enable debugging");
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

        JLabel postLaunchActionLabel = new JLabel("Action after launch:");
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.weighty = 0.0;
        contentPanel.add(postLaunchActionLabel, gbc);

        postLaunchActionComboBox = new JComboBox<>(new String[]{"Do Nothing", "Minimize Launcher", "Close Launcher"});
        postLaunchActionComboBox.setSelectedItem(this.postLaunchAction);
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
        JLabel themeLabel = new JLabel("Theme:");
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.weighty = 0.0;
        contentPanel.add(themeLabel, gbc);

        String[] themes = new String[]{"Light", "Dark"};
        themeComboBox = new JComboBox<>(themes);
        themeComboBox.setSelectedItem(this.themeName);
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        contentPanel.add(themeComboBox, gbc);

        gridY++;
        scaleLabel = new JLabel("Interface scale: " + (int)(scaleFactor * 100) + "%");
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
            scaleLabel.setText("Interface scale: " + value + "%");
        });
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        contentPanel.add(scaleSlider, gbc);

        gridY++;
        JLabel backgroundLabel = new JLabel("Background:");
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        contentPanel.add(backgroundLabel, gbc);

        JPanel radioPanel = new JPanel(new GridLayout(0, 1));
        defaultBgRadio = new JRadioButton("Default background image");
        customImageRadio = new JRadioButton("Custom background image");
        customColorRadio = new JRadioButton("Custom color");
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

        browseBackgroundButton = new JButton("Browse...");
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
        chooseColorButton = new JButton("Choose Color...");
        colorPreviewPanel = new JPanel();
        colorPreviewPanel.setPreferredSize(new Dimension(24, 24));
        colorPreviewPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        if (customBackgroundColor != null) {
            colorPreviewPanel.setBackground(customBackgroundColor);
        }

        chooseColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Background Color", colorPreviewPanel.getBackground());
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

        JLabel headline1 = new JLabel("NostalgiaLauncher");
        headline1.setFont(new Font("SansSerif", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 3;
        contentPanel.add(headline1, gbc);

        JLabel headline2 = new JLabel("Minecraft PE Alpha versions launcher for Windows");
        headline2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        gbc.gridy = gridY++;
        contentPanel.add(headline2, gbc);

        gbc.gridy = gridY++;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(15, 5, 5, 5); 
        contentPanel.add(new JSeparator(), gbc);
        gbc.insets = new Insets(5, 5, 5, 5); 

        JLabel headline3 = new JLabel("Used materials");
        headline3.setFont(new Font("SansSerif", Font.BOLD, 16));
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
        JLabel versionLabel = new JLabel("<html><span style='color:gray;'>Current version: " + currentVersion + "</span></html>");
        versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        versionPanel.add(versionLabel);

        JLabel updateStatusLabel = new JLabel("Checking for updates...");
        updateStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
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
                        updateStatusLabel.setText("Up to date");
                        updateStatusLabel.setForeground(new Color(76, 175, 80));
                    } else {
                        JLabel updateLink = new JLabel("<html><a href='" + "https://nlauncher.github.io/releases.html#desktop" + "'>New update available</a></html>");
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
                    updateStatusLabel.setText("Error checking for updates");
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
        label.setFont(new Font("SansSerif", Font.PLAIN, 10));
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
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
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

    public boolean isSaved() {
        return saved;
    }
    
    public String getBackgroundMode() {
        return backgroundMode;
    }

    public Color getCustomBackgroundColor() {
        return customBackgroundColor;
    }
}