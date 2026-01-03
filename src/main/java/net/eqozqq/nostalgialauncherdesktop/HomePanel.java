package net.eqozqq.nostalgialauncherdesktop;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;

public class HomePanel extends JPanel {

    private JTextField nicknameField;
    private JComboBox<Version> versionComboBox;
    private JButton launchButton;
    private JButton refreshButton;
    private JButton addVersionButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    private final LocaleManager localeManager;
    private final double scaleFactor;
    private final String themeName;
    private final boolean isDark;
    private final VersionManager versionManager;

    private ActionListener launchListener;
    private ActionListener refreshListener;
    private ActionListener addVersionListener;

    private static final int COMPONENT_WIDTH = 350;

    public HomePanel(LocaleManager localeManager, double scaleFactor, String themeName, VersionManager versionManager) {
        this.localeManager = localeManager;
        this.scaleFactor = scaleFactor;
        this.themeName = themeName;
        this.isDark = themeName.contains("Dark");
        this.versionManager = versionManager;

        setOpaque(false);
        setLayout(new GridBagLayout());

        JPanel contentPanel = createContentPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(contentPanel, gbc);

        JPanel infoPanel = createInfoPanel();
        GridBagConstraints gbcInfo = new GridBagConstraints();
        gbcInfo.gridx = 0;
        gbcInfo.gridy = 1;
        gbcInfo.weighty = 0.0;
        gbcInfo.anchor = GridBagConstraints.PAGE_END;
        gbcInfo.insets = new Insets(0, 0, (int) (20 * scaleFactor), 0);
        add(infoPanel, gbcInfo);
    }

    private Font getMinecraftFont(int style, float size) {
        try (InputStream fontStream = HomePanel.class.getResourceAsStream("/MPLUS1p-Regular.ttf")) {
            if (fontStream != null) {
                return Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(style, size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Font("SansSerif", style, (int) size);
    }

    private Font getRegularFont(int style, float size) {
        try (InputStream fontStream = HomePanel.class.getResourceAsStream("/MPLUS1p-Regular.ttf")) {
            if (fontStream != null) {
                return Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(style, size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Font("SansSerif", style, (int) size);
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(
                (int) (40 * scaleFactor), (int) (60 * scaleFactor),
                (int) (40 * scaleFactor), (int) (60 * scaleFactor)));

        contentPanel.add(createLogoPanel());
        contentPanel.add(Box.createVerticalStrut((int) (30 * scaleFactor)));
        contentPanel.add(createTranslucentGamePanel());

        return contentPanel;
    }

    private JPanel createLogoPanel() {
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setOpaque(false);

        JLabel logoLabel = new JLabel(localeManager.get("launcher.logo", "Nostalgia Launcher"));
        logoLabel.setFont(getMinecraftFont(Font.PLAIN, (float) (42 * scaleFactor)));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);

        logoLabel.setOpaque(false);
        logoPanel.add(logoLabel);

        JLabel subtitleLabel = new JLabel(localeManager.get("launcher.subtitle", "Minecraft Pocket Edition"));
        subtitleLabel.setFont(getRegularFont(Font.PLAIN, (float) (18 * scaleFactor)));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(isDark ? new Color(220, 220, 220) : new Color(50, 50, 50));
        subtitleLabel.setOpaque(false);

        logoPanel.add(Box.createVerticalStrut((int) (5 * scaleFactor)));
        logoPanel.add(subtitleLabel);

        return logoPanel;
    }

    private JPanel createTranslucentGamePanel() {
        JPanel gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isDark) {
                    g2d.setColor(new Color(30, 30, 30, 180));
                } else {
                    g2d.setColor(new Color(255, 255, 255, 180));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
            }
        };
        gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.Y_AXIS));
        gamePanel.setOpaque(false);
        gamePanel.setBorder(new EmptyBorder(
                (int) (30 * scaleFactor), (int) (30 * scaleFactor),
                (int) (30 * scaleFactor), (int) (30 * scaleFactor)));

        nicknameField = new JTextField();
        nicknameField.setPreferredSize(new Dimension(
                (int) (COMPONENT_WIDTH * scaleFactor), (int) (45 * scaleFactor)));
        nicknameField.setMaximumSize(new Dimension(
                (int) (COMPONENT_WIDTH * scaleFactor), (int) (45 * scaleFactor)));
        nicknameField.setFont(getRegularFont(Font.PLAIN, (float) (15 * scaleFactor)));
        nicknameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        nicknameField.putClientProperty("JTextField.placeholderText",
                localeManager.get("placeholder.nickname", "Nickname"));
        nicknameField.setText(localeManager.get("default.nickname", "Steve"));

        gamePanel.add(nicknameField);
        gamePanel.add(Box.createVerticalStrut((int) (10 * scaleFactor)));

        JPanel managePanel = new JPanel();
        managePanel.setLayout(new BoxLayout(managePanel, BoxLayout.X_AXIS));
        managePanel.setOpaque(false);
        managePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        managePanel.setMaximumSize(new Dimension((int) (COMPONENT_WIDTH * scaleFactor), (int) (45 * scaleFactor)));

        addVersionButton = new JButton(localeManager.get("tooltip.addVersion", "Add Version"));
        try {
            FlatSVGIcon icon = new FlatSVGIcon("icons/add.svg", (int) (18 * scaleFactor), (int) (18 * scaleFactor));
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> isDark ? Color.WHITE : Color.BLACK));
            addVersionButton.setIcon(icon);
        } catch (Exception e) {
            e.printStackTrace();
        }
        addVersionButton.setPreferredSize(new Dimension((int) (300 * scaleFactor), (int) (45 * scaleFactor)));
        addVersionButton.setMaximumSize(new Dimension((int) (300 * scaleFactor), (int) (45 * scaleFactor)));
        addVersionButton.setFont(getRegularFont(Font.PLAIN, (float) (14 * scaleFactor)));

        refreshButton = new JButton();
        try {
            FlatSVGIcon icon = new FlatSVGIcon("icons/refresh.svg", (int) (18 * scaleFactor), (int) (18 * scaleFactor));
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> isDark ? Color.WHITE : Color.BLACK));
            refreshButton.setIcon(icon);
        } catch (Exception e) {
            e.printStackTrace();
        }
        refreshButton.setPreferredSize(new Dimension((int) (45 * scaleFactor), (int) (45 * scaleFactor)));
        refreshButton.setMaximumSize(new Dimension((int) (45 * scaleFactor), (int) (45 * scaleFactor)));
        refreshButton.setToolTipText(localeManager.get("tooltip.refreshVersions", "Refresh"));

        managePanel.add(addVersionButton);
        managePanel.add(Box.createHorizontalStrut((int) (5 * scaleFactor)));
        managePanel.add(refreshButton);

        gamePanel.add(managePanel);
        gamePanel.add(Box.createVerticalStrut((int) (10 * scaleFactor)));

        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
        actionPanel.setOpaque(false);
        actionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionPanel.setMaximumSize(new Dimension((int) (COMPONENT_WIDTH * scaleFactor), (int) (45 * scaleFactor)));

        versionComboBox = new JComboBox<Version>();
        versionComboBox.setPreferredSize(new Dimension(
                (int) (120 * scaleFactor), (int) (45 * scaleFactor)));
        versionComboBox.setMaximumSize(new Dimension(
                (int) (120 * scaleFactor), (int) (45 * scaleFactor)));
        versionComboBox.setFont(getRegularFont(Font.PLAIN, (float) (14 * scaleFactor)));
        versionComboBox.setRenderer(new VersionListCellRenderer(versionManager));

        launchButton = new JButton(localeManager.get("button.launch", "Launch"));
        launchButton.setPreferredSize(new Dimension(
                (int) (225 * scaleFactor), (int) (45 * scaleFactor)));
        launchButton.setMaximumSize(new Dimension(
                (int) (225 * scaleFactor), (int) (45 * scaleFactor)));
        launchButton.setFont(getRegularFont(Font.BOLD, (float) (16 * scaleFactor)));

        actionPanel.add(versionComboBox);
        actionPanel.add(Box.createHorizontalStrut((int) (5 * scaleFactor)));
        actionPanel.add(launchButton);

        gamePanel.add(actionPanel);

        gamePanel.add(Box.createVerticalStrut((int) (15 * scaleFactor)));
        gamePanel.add(createProgressPanel());
        gamePanel.add(Box.createVerticalStrut((int) (10 * scaleFactor)));
        gamePanel.add(createStatusPanel());

        return gamePanel;
    }

    private JPanel createProgressPanel() {
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setOpaque(false);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(
                (int) (COMPONENT_WIDTH * scaleFactor), (int) (20 * scaleFactor)));
        progressBar.setMaximumSize(new Dimension(
                (int) (COMPONENT_WIDTH * scaleFactor), (int) (20 * scaleFactor)));
        progressBar.setFont(getRegularFont(Font.PLAIN, (float) (12 * scaleFactor)));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        progressPanel.add(progressBar);

        return progressPanel;
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setOpaque(false);

        statusLabel = new JLabel(localeManager.get("status.ready", "Ready"));
        statusLabel.setFont(getRegularFont(Font.PLAIN, (float) (12 * scaleFactor)));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(isDark ? new Color(200, 200, 200) : new Color(80, 80, 80));
        statusLabel.setOpaque(false);

        statusPanel.add(statusLabel);

        return statusPanel;
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isDark) {
                    g2d.setColor(new Color(30, 30, 30, 180));
                } else {
                    g2d.setColor(new Color(255, 255, 255, 150));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
            }
        };

        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.setBorder(BorderFactory.createEmptyBorder((int) (10 * scaleFactor), (int) (20 * scaleFactor),
                (int) (10 * scaleFactor), (int) (20 * scaleFactor)));

        JLabel versionLabel = new JLabel("NostalgiaLauncher Desktop v1.8.0 by eqozqq");
        versionLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);
        versionLabel.setFont(getRegularFont(Font.PLAIN, (float) (12 * scaleFactor)));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel disclaimerLabel = new JLabel(
                localeManager.get("about.disclaimer", "Not affiliated with Mojang Studios"));
        disclaimerLabel.setForeground(isDark ? new Color(180, 180, 180) : new Color(80, 80, 80));
        disclaimerLabel.setFont(getRegularFont(Font.PLAIN, (float) (10 * scaleFactor)));
        disclaimerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(versionLabel);
        infoPanel.add(disclaimerLabel);

        return infoPanel;
    }

    public JTextField getNicknameField() {
        return nicknameField;
    }

    public JComboBox<Version> getVersionComboBox() {
        return versionComboBox;
    }

    public JButton getLaunchButton() {
        return launchButton;
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    public JButton getAddVersionButton() {
        return addVersionButton;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    public void setLaunchListener(ActionListener listener) {
        if (launchListener != null) {
            launchButton.removeActionListener(launchListener);
        }
        launchListener = listener;
        launchButton.addActionListener(listener);
    }

    public void setRefreshListener(ActionListener listener) {
        if (refreshListener != null) {
            refreshButton.removeActionListener(refreshListener);
        }
        refreshListener = listener;
        refreshButton.addActionListener(listener);
    }

    public void setAddVersionListener(ActionListener listener) {
        if (addVersionListener != null) {
            addVersionButton.removeActionListener(addVersionListener);
        }
        addVersionListener = listener;
        addVersionButton.addActionListener(listener);
    }
}