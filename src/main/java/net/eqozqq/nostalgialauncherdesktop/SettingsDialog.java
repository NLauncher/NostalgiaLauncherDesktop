package net.eqozqq.nostalgialauncherdesktop;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class SettingsDialog extends JDialog {
    private JTextField backgroundPathField;
    private JCheckBox useDefaultBackgroundCheckbox;
    private JTextField versionsSourceField;
    private JCheckBox useDefaultSourceCheckbox;
    private JTextField customLauncherField;
    private JCheckBox useDefaultLauncherCheckbox;
    private JComboBox<String> postLaunchActionComboBox;
    private JCheckBox enableDebuggingCheckbox;
    private JButton saveButton;
    private JButton browseBackgroundButton;
    private JButton browseVersionsButton;
    private JButton browseLauncherButton;
    private JRadioButton urlRadioButton;
    private JRadioButton fileRadioButton;

    private String customBackgroundPath;
    private boolean useDefaultBackground;
    private String customVersionsSource;
    private boolean useDefaultVersionsSource;
    private String customLauncherPath;
    private boolean useDefaultLauncher;
    private String postLaunchAction;
    private boolean enableDebugging;
    private boolean saved = false;

    public SettingsDialog(JFrame parent, String currentBackgroundPath, boolean useDefaultBg, String currentVersionsSource, boolean useDefaultVs, String currentCustomLauncherPath, boolean useDefaultLauncher, String currentPostLaunchAction, boolean currentEnableDebugging) {
        super(parent, "Settings", true);
        setSize(500, 550);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        this.customBackgroundPath = currentBackgroundPath;
        this.useDefaultBackground = useDefaultBg;
        this.customVersionsSource = currentVersionsSource;
        this.useDefaultVersionsSource = useDefaultVs;
        this.customLauncherPath = currentCustomLauncherPath;
        this.useDefaultLauncher = useDefaultLauncher;
        this.postLaunchAction = currentPostLaunchAction;
        this.enableDebugging = currentEnableDebugging;

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int gridY = 0;

        JLabel backgroundLabel = new JLabel("Background image path:");
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(backgroundLabel, gbc);

        backgroundPathField = new JTextField(20);
        backgroundPathField.setText(useDefaultBackground ? "" : customBackgroundPath);
        backgroundPathField.setEnabled(!useDefaultBackground);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(backgroundPathField, gbc);

        browseBackgroundButton = new JButton("Browse...");
        browseBackgroundButton.setEnabled(!useDefaultBackground);
        browseBackgroundButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                backgroundPathField.setText(file.getAbsolutePath());
            }
        });
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        formPanel.add(browseBackgroundButton, gbc);
        
        gridY++;
        useDefaultBackgroundCheckbox = new JCheckBox("Use default background");
        useDefaultBackgroundCheckbox.setSelected(useDefaultBackground);
        useDefaultBackgroundCheckbox.addActionListener(e -> {
            backgroundPathField.setEnabled(!useDefaultBackgroundCheckbox.isSelected());
            browseBackgroundButton.setEnabled(!useDefaultBackgroundCheckbox.isSelected());
        });
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        formPanel.add(useDefaultBackgroundCheckbox, gbc);

        gridY++;
        JLabel versionsLabel = new JLabel("Versions source:");
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        formPanel.add(versionsLabel, gbc);

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
        formPanel.add(sourceSelectionPanel, gbc);
        
        gridY++;
        versionsSourceField = new JTextField(20);
        versionsSourceField.setText(useDefaultVersionsSource ? "" : customVersionsSource);
        versionsSourceField.setEnabled(!useDefaultVersionsSource);
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        formPanel.add(versionsSourceField, gbc);

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
        gbc.weightx = 0.0;
        formPanel.add(browseVersionsButton, gbc);

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
        formPanel.add(useDefaultSourceCheckbox, gbc);

        gridY++;
        JLabel versionsInfoLabel = createInfoLabel("Add your own versions list source. ", "Learn more", "https://legacyminecraftpe.github.io/NostalgiaLauncher/docs/CustomVersionsListSource");
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        formPanel.add(versionsInfoLabel, gbc);

        gridY++;
        JLabel customLauncherLabel = new JLabel("Custom executable for launching versions:");
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        formPanel.add(customLauncherLabel, gbc);

        customLauncherField = new JTextField(20);
        customLauncherField.setText(useDefaultLauncher ? "" : customLauncherPath);
        customLauncherField.setEnabled(!useDefaultLauncher);
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        formPanel.add(customLauncherField, gbc);

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
        formPanel.add(browseLauncherButton, gbc);
        
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
        formPanel.add(useDefaultLauncherCheckbox, gbc);

        gridY++;
        JLabel launcherInfoLabel = createInfoLabel("Add your executable for launching versions. ", "Learn more", "https://legacyminecraftpe.github.io/NostalgiaLauncher/docs/CustomLauncher");
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        formPanel.add(launcherInfoLabel, gbc);

        gridY++;
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(15, 5, 5, 5);
        formPanel.add(new JSeparator(), gbc);
        gbc.insets = new Insets(5, 5, 5, 5);

        gridY++;
        JLabel postLaunchActionLabel = new JLabel("Action after launch:");
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        formPanel.add(postLaunchActionLabel, gbc);

        postLaunchActionComboBox = new JComboBox<>(new String[]{"Do Nothing", "Minimize Launcher", "Close Launcher"});
        postLaunchActionComboBox.setSelectedItem(this.postLaunchAction);
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        formPanel.add(postLaunchActionComboBox, gbc);

        gridY++;
        enableDebuggingCheckbox = new JCheckBox("Enable Debugging");
        enableDebuggingCheckbox.setSelected(this.enableDebugging);
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.gridwidth = 2;
        formPanel.add(enableDebuggingCheckbox, gbc);

        JPanel buttonPanel = new JPanel();
        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            this.useDefaultBackground = useDefaultBackgroundCheckbox.isSelected();
            if (!this.useDefaultBackground) {
                this.customBackgroundPath = backgroundPathField.getText();
            } else {
                this.customBackgroundPath = null;
            }

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
            
            this.postLaunchAction = (String) postLaunchActionComboBox.getSelectedItem();
            this.enableDebugging = enableDebuggingCheckbox.isSelected();

            saved = true;
            dispose();
        });
        buttonPanel.add(saveButton);
        
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JLabel createInfoLabel(String text, String linkText, String url) {
        JLabel label = new JLabel("<html><span style='color:gray;'>" + text + "</span><a href='" + url + "'><span style='font-weight:bold;'>" + linkText + "</span></a></html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, 10));
        label.setForeground(Color.GRAY);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
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

    public boolean isUseDefaultBackground() {
        return useDefaultBackground;
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

    public boolean isSaved() {
        return saved;
    }
}