package net.eqozqq.nostalgialauncherdesktop.TexturesManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import net.eqozqq.nostalgialauncherdesktop.LocaleManager;
import net.eqozqq.nostalgialauncherdesktop.Version;
import net.eqozqq.nostalgialauncherdesktop.VersionManager;
import net.eqozqq.nostalgialauncherdesktop.Instances.InstanceManager;

public class TexturesManagerPanel extends JPanel {
    private final LocaleManager localeManager;
    private final boolean isDark;

    private JList<String> versionList;
    private DefaultListModel<String> listModel;
    private String selectedVersion;

    private JPanel versionSelectionPanel;
    private JPanel texturePanel;
    private CardLayout cardLayout;
    private JPanel contentPanel;

    private JTextField archivePathField;

    public TexturesManagerPanel(LocaleManager localeManager, String themeName) {
        this.localeManager = localeManager;
        this.isDark = themeName.contains("Dark");
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 40, 20, 40));

        JPanel mainCard = createCardPanel();
        mainCard.setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel(localeManager.get("nav.textures"));
        titleLabel.setFont(getFont(Font.BOLD, 24f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        mainCard.add(titleLabel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        versionSelectionPanel = createVersionSelectionPanel();
        texturePanel = createTexturePanel();

        contentPanel.add(versionSelectionPanel, "SELECT_VERSION");
        contentPanel.add(texturePanel, "TEXTURE_PANEL");

        mainCard.add(contentPanel, BorderLayout.CENTER);
        add(mainCard, BorderLayout.CENTER);
    }

    private JPanel createCardPanel() {
        JPanel card = new JPanel() {
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

    private JPanel createVersionSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JLabel infoLabel = new JLabel(localeManager.get("label.selectVersionForTexture"));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setFont(getFont(Font.PLAIN, 14f));
        infoLabel.setForeground(isDark ? new Color(200, 200, 200) : new Color(60, 60, 60));
        panel.add(infoLabel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        versionList = new JList<>(listModel);
        versionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        versionList.setOpaque(false);
        versionList.setBackground(new Color(0, 0, 0, 0));
        versionList.setCellRenderer(new TransparentListCellRenderer());

        JScrollPane scrollPane = new JScrollPane(versionList);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(isDark ? new Color(60, 60, 60) : new Color(200, 200, 200)));
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton selectButton = new JButton(localeManager.get("button.select"));
        selectButton.setFont(getFont(Font.BOLD, 14f));
        selectButton.setPreferredSize(new Dimension(0, 40));
        selectButton.addActionListener(e -> {
            selectedVersion = versionList.getSelectedValue();
            if (selectedVersion != null) {
                cardLayout.show(contentPanel, "TEXTURE_PANEL");
            } else {
                JOptionPane.showMessageDialog(this, 
                        localeManager.get("error.noVersionSelected.textures"),
                        localeManager.get("error.noVersionSelected.title"), 
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        buttonPanel.add(selectButton, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTexturePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JButton backButton = new JButton(localeManager.get("button.back"));
        backButton.addActionListener(e -> cardLayout.show(contentPanel, "SELECT_VERSION"));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(backButton, gbc);

        JPanel fileSelectionPanel = new JPanel(new BorderLayout(5, 0));
        fileSelectionPanel.setOpaque(false);
        archivePathField = new JTextField(30);
        archivePathField.setEditable(false);
        JButton browseButton = new JButton(localeManager.get("button.browse"));
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(localeManager.get("dialog.selectTextureArchive.title"));
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    localeManager.get("fileChooser.archives"), "zip", "rar", "tar", "7z"));
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                archivePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        fileSelectionPanel.add(archivePathField, BorderLayout.CENTER);
        fileSelectionPanel.add(browseButton, BorderLayout.EAST);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(fileSelectionPanel, gbc);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonsPanel.setOpaque(false);
        JButton unpackButton = new JButton(localeManager.get("button.unpackTextures"));
        JButton restoreButton = new JButton(localeManager.get("button.restoreDefaultTextures"));

        unpackButton.setFont(getFont(Font.BOLD, 14f));
        restoreButton.setFont(getFont(Font.BOLD, 14f));

        Dimension buttonSize = new Dimension(200, 40);
        unpackButton.setPreferredSize(buttonSize);
        restoreButton.setPreferredSize(buttonSize);

        unpackButton.addActionListener(e -> unpackTextures());
        restoreButton.addActionListener(e -> restoreDefaultTextures());

        buttonsPanel.add(unpackButton);
        buttonsPanel.add(restoreButton);

        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonsPanel, gbc);

        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JPanel() {
            {
                setOpaque(false);
            }
        }, gbc);

        return panel;
    }

    private void unpackTextures() {
        String path = archivePathField.getText();
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    localeManager.get("error.noArchiveSelected.message"),
                    localeManager.get("error.noArchiveSelected.title"), 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            File archiveFile = new File(path);
            File versionDir = new File(InstanceManager.getInstance().resolvePath("versions/" + selectedVersion));
            ArchiveExtractor.extract(archiveFile, versionDir);
            
            JOptionPane.showMessageDialog(this, 
                    localeManager.get("info.texturesInstalled"),
                    localeManager.get("dialog.success.title"), 
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                    localeManager.get("error.installTexturesFailed", ex.getMessage()),
                    localeManager.get("dialog.error.title"), 
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void restoreDefaultTextures() {
        if (selectedVersion == null)
            return;

        VersionManager versionManager = new VersionManager();
        try {
            List<Version> allVersions = versionManager
                    .loadVersions("https://raw.githubusercontent.com/NLauncher/components/main/versions.json");
            Version targetVersion = allVersions.stream()
                    .filter(v -> v.getName().equals(selectedVersion))
                    .findFirst()
                    .orElse(null);

            if (targetVersion == null || targetVersion.getUrl().startsWith("file:")) {
                JOptionPane.showMessageDialog(this, 
                        localeManager.get("error.restoreOfficialOnly"),
                        localeManager.get("error.cannotRestore.title"), 
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int choice = JOptionPane.showConfirmDialog(this, 
                    localeManager.get("dialog.confirmRestore.message"),
                    localeManager.get("dialog.confirmRestore.title"), 
                    JOptionPane.YES_NO_OPTION);
            
            if (choice != JOptionPane.YES_OPTION)
                return;

            ProgressMonitor progressMonitor = new ProgressMonitor(this,
                    localeManager.get("progress.downloadingVersion", selectedVersion), "", 0, 100);
            progressMonitor.setMillisToPopup(0);

            SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
                @Override
                protected Void doInBackground() throws Exception {
                    File apkFile = versionManager.downloadVersion(targetVersion, progress -> {
                        publish((int) (progress * 100));
                    });

                    progressMonitor.setNote(localeManager.get("progress.extractingTextures"));
                    progressMonitor.setProgress(0);

                    ArchiveExtractor.extractDefaultTextures(apkFile,
                            new File(InstanceManager.getInstance().resolvePath("versions/" + selectedVersion)));
                    return null;
                }

                @Override
                protected void process(List<Integer> chunks) {
                    if (!chunks.isEmpty()) {
                        progressMonitor.setProgress(chunks.get(chunks.size() - 1));
                    }
                }

                @Override
                protected void done() {
                    progressMonitor.close();
                    try {
                        get();
                        JOptionPane.showMessageDialog(TexturesManagerPanel.this,
                                localeManager.get("info.restoreSuccess"), 
                                localeManager.get("dialog.success.title"),
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(TexturesManagerPanel.this,
                                localeManager.get("error.restoreFailed", ex.getMessage()),
                                localeManager.get("dialog.error.title"), 
                                JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            };
            worker.execute();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                    localeManager.get("error.restoreFailed", ex.getMessage()),
                    localeManager.get("dialog.error.title"), 
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void resetView() {
        listModel.clear();
        loadInstalledVersions();
        selectedVersion = null;
        archivePathField.setText("");
        cardLayout.show(contentPanel, "SELECT_VERSION");
    }

    private void loadInstalledVersions() {
        File versionsDir = new File(InstanceManager.getInstance().resolvePath("versions"));
        if (versionsDir.exists() && versionsDir.isDirectory()) {
            String[] directories = versionsDir.list((current, name) -> new File(current, name).isDirectory());
            if (directories != null) {
                Arrays.sort(directories);
                for (String dirName : directories) {
                    if (!dirName.equals("_LevelCache") && !dirName.equals("default_textures")) {
                        listModel.addElement(dirName);
                    }
                }
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

    private class TransparentListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setOpaque(isSelected);
            if (isSelected) {
                setBackground(new Color(100, 180, 255, 60));
                setForeground(isDark ? Color.WHITE : Color.BLACK);
            } else {
                setForeground(isDark ? new Color(220, 220, 220) : new Color(50, 50, 50));
            }
            return this;
        }
    }
}