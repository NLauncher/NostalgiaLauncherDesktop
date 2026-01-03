package net.eqozqq.nostalgialauncherdesktop.TexturesManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
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
    private JPanel rightPanel;
    private JSplitPane splitPane;
    
    private JLabel selectedVersionLabel;
    private JTextField archivePathField;
    private String selectedVersion;

    public TexturesManagerPanel(LocaleManager localeManager, String themeName) {
        this.localeManager = localeManager;
        this.isDark = themeName.contains("Dark");
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel mainCard = createCardPanel();
        mainCard.setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel(localeManager.get("nav.textures"));
        titleLabel.setFont(getFont(Font.BOLD, 24f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        mainCard.add(titleLabel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        versionList = new JList<>(listModel);
        versionList.setCellRenderer(new VersionGridRenderer());
        versionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        versionList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        versionList.setVisibleRowCount(-1);
        versionList.setOpaque(false);
        versionList.setBackground(new Color(0, 0, 0, 0));
        
        versionList.setFixedCellWidth(290);
        versionList.setFixedCellHeight(80);

        JScrollPane listScrollPane = new JScrollPane(versionList);
        listScrollPane.setOpaque(false);
        listScrollPane.getViewport().setOpaque(false);
        listScrollPane.setBorder(null);
        listScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        listScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        listScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        listScrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        rightPanel = createRightPanel();
        rightPanel.setMinimumSize(new Dimension(450, 0));
        rightPanel.setPreferredSize(new Dimension(450, 0));

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, rightPanel);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(0);
        splitPane.setResizeWeight(1.0);
        
        rightPanel.setVisible(false);
        splitPane.setDividerLocation(1.0);

        mainCard.add(splitPane, BorderLayout.CENTER);

        versionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedVersion = versionList.getSelectedValue();
                if (selectedVersion != null) {
                    updateRightPanel(selectedVersion);
                    rightPanel.setVisible(true);
                    splitPane.setDividerLocation(splitPane.getWidth() - rightPanel.getPreferredSize().width);
                } else {
                    rightPanel.setVisible(false);
                }
            }
        });

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

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isDark) {
                    g2d.setColor(new Color(45, 45, 45, 240)); 
                } else {
                    g2d.setColor(new Color(245, 245, 245, 240));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        int y = 0;

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        selectedVersionLabel = new JLabel(""); 
        selectedVersionLabel.setFont(getFont(Font.BOLD, 18f));
        selectedVersionLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);
        
        JButton closeBtn = new JButton("×");
        closeBtn.setFont(getFont(Font.BOLD, 20f));
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setForeground(isDark ? Color.GRAY : Color.DARK_GRAY);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> {
            versionList.clearSelection();
            rightPanel.setVisible(false);
        });
        
        headerPanel.add(selectedVersionLabel, BorderLayout.WEST);
        headerPanel.add(closeBtn, BorderLayout.EAST);
        
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        panel.add(headerPanel, gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        JLabel installLabel = new JLabel(localeManager.get("button.unpackTextures")); 
        installLabel.setFont(getFont(Font.BOLD, 14f));
        installLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);
        panel.add(installLabel, gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        JPanel fileSelectionPanel = new JPanel(new BorderLayout(5, 0));
        fileSelectionPanel.setOpaque(false);
        archivePathField = new JTextField();
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
        panel.add(fileSelectionPanel, gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        JButton unpackButton = new JButton(localeManager.get("button.unpackTextures"));
        unpackButton.setFont(getFont(Font.BOLD, 14f));
        unpackButton.addActionListener(e -> unpackTextures());
        panel.add(unpackButton, gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 6, 6, 6);
        panel.add(new JSeparator(), gbc);
        gbc.insets = new Insets(6, 6, 6, 6);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        JLabel restoreLabel = new JLabel(localeManager.get("button.restoreDefaultTextures"));
        restoreLabel.setFont(getFont(Font.BOLD, 14f));
        restoreLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);
        panel.add(restoreLabel, gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        JButton restoreButton = new JButton(localeManager.get("button.restoreDefaultTextures"));
        restoreButton.setFont(getFont(Font.BOLD, 14f));
        restoreButton.addActionListener(e -> restoreDefaultTextures());
        panel.add(restoreButton, gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JPanel() {
            {
                setOpaque(false);
            }
        }, gbc);

        return panel;
    }

    private void updateRightPanel(String version) {
        selectedVersionLabel.setText(version);
        archivePathField.setText("");
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
            ArchiveExtractor.install(archiveFile, versionDir);
            
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
        rightPanel.setVisible(false);
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

    private class VersionGridRenderer extends JPanel implements ListCellRenderer<String> {
        private JLabel nameLabel;
        private boolean isSelected;
        private final int GAP = 8; 

        public VersionGridRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10 + GAP, 15 + GAP, 10 + GAP, 15 + GAP));

            nameLabel = new JLabel();
            nameLabel.setFont(TexturesManagerPanel.this.getFont(Font.BOLD, 16f));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.CENTER;
            add(nameLabel, gbc);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                boolean isSelected, boolean cellHasFocus) {
            this.isSelected = isSelected;
            nameLabel.setText(value);

            Color fg = isDark ? Color.WHITE : Color.BLACK;
            if (isSelected) {
                fg = isDark ? Color.WHITE : Color.BLACK;
            }
            nameLabel.setForeground(fg);

            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isSelected) {
                g2d.setColor(isDark ? new Color(255, 255, 255, 50) : new Color(0, 0, 0, 40));
                g2d.fillRoundRect(GAP, GAP, getWidth() - GAP*2, getHeight() - GAP*2, 15, 15);
            } else {
                g2d.setColor(isDark ? new Color(50, 50, 50, 180) : new Color(250, 250, 250, 220));
                g2d.fillRoundRect(GAP, GAP, getWidth() - GAP*2, getHeight() - GAP*2, 15, 15);
            }

            g2d.dispose();
            super.paintComponent(g);
        }
    }
    
    private class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            scrollbar.setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return new Dimension(8, super.getPreferredSize(c).height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isThumbRollover() || isDragging) {
                g2d.setColor(isDark ? new Color(255, 255, 255, 80) : new Color(0, 0, 0, 80));
            } else {
                g2d.setColor(isDark ? new Color(255, 255, 255, 40) : new Color(0, 0, 0, 40));
            }
            
            g2d.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 8, 8);
            g2d.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        }
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            return btn;
        }
    }
}