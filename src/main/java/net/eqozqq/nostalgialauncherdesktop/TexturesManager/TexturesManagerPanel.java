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
    private final double scaleFactor;
    private JList<File> versionList;
    private DefaultListModel<File> listModel;

    private JButton addButton;
    private JButton restoreButton;
    private File texturesDir;

    public TexturesManagerPanel(LocaleManager localeManager, String themeName, double scaleFactor) {
        this.localeManager = localeManager;
        this.isDark = themeName.contains("Dark");
        this.scaleFactor = scaleFactor;
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

        texturesDir = new File(InstanceManager.getDataRoot(), "textures");
        if (!texturesDir.exists())
            texturesDir.mkdirs();

        listModel = new DefaultListModel<>();
        versionList = new JList<>(listModel);
        versionList.setCellRenderer(new VersionGridRenderer());
        versionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        versionList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        versionList.setVisibleRowCount(-1);
        versionList.setOpaque(false);
        versionList.setBackground(new Color(0, 0, 0, 0));

        versionList.setFixedCellWidth((int) (290 * scaleFactor));
        versionList.setFixedCellHeight((int) (80 * scaleFactor));

        JScrollPane listScrollPane = new JScrollPane(versionList);
        listScrollPane.setOpaque(false);
        listScrollPane.getViewport().setOpaque(false);
        listScrollPane.setBorder(null);
        listScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        listScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        listScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        listScrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        mainCard.add(listScrollPane, BorderLayout.CENTER);
        add(mainCard, BorderLayout.CENTER);

        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

        versionList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int index = versionList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        File textureFile = listModel.getElementAt(index);
                        if (textureFile != null && textureFile.exists()) {
                            TextureInstallDialog dialog = new TextureInstallDialog(
                                    (Frame) SwingUtilities.getWindowAncestor(TexturesManagerPanel.this),
                                    localeManager, scaleFactor, textureFile);
                            dialog.setVisible(true);
                        }
                    }
                }
            }
        });
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

    private JPanel createFooterPanel() {
        JPanel footerCard = new JPanel(new FlowLayout(FlowLayout.CENTER, (int) (20 * scaleFactor), 0)) {
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
        footerCard.setOpaque(false);
        footerCard.setBorder(BorderFactory.createEmptyBorder((int) (15 * scaleFactor), (int) (20 * scaleFactor),
                (int) (15 * scaleFactor), (int) (20 * scaleFactor)));

        addButton = createStyledButton(localeManager.get("button.addTexture", "Add Texture"), true, 14f);
        addButton.addActionListener(e -> {
            TextureAddDialog dialog = new TextureAddDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    localeManager, isDark, scaleFactor);
            dialog.setVisible(true);
            if (dialog.isSuccess()) {
                loadUserTextures();
            }
        });
        footerCard.add(addButton);

        restoreButton = createStyledButton(localeManager.get("button.restoreDefaultTextures"), false, 12f);
        restoreButton.addActionListener(e -> restoreDefaultTextures());
        footerCard.add(restoreButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder((int) (15 * scaleFactor), 0, 0, 0));
        wrapper.add(footerCard, BorderLayout.CENTER);

        return wrapper;
    }

    private JButton createStyledButton(String text, boolean bold, float fontSize) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isDark) {
                    g2d.setColor(new Color(65, 65, 65, 230));
                } else {
                    g2d.setColor(new Color(240, 240, 240, 230));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(getFont(bold ? Font.BOLD : Font.PLAIN, fontSize * (float) scaleFactor));
        btn.setForeground(isDark ? Color.WHITE : Color.BLACK);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension((int) (180 * scaleFactor), (int) (35 * scaleFactor)));
        return btn;
    }

    private void updateUIState() {
    }

    private void unpackTextures() {
    }

    private void restoreDefaultTextures() {
        String selectedInstanceName = (String) JOptionPane.showInputDialog(this,
                localeManager.get("label.selectInstance", "Select Instance:"),
                localeManager.get("dialog.restore.title", "Restore Textures"),
                JOptionPane.QUESTION_MESSAGE, null,
                InstanceManager.getInstance().getInstances().toArray(),
                InstanceManager.getInstance().getActiveInstance());

        if (selectedInstanceName == null)
            return;

        String oldInstance = InstanceManager.getInstance().getActiveInstance();
        InstanceManager.getInstance().setActiveInstance(selectedInstanceName);

        File versionsDir = new File(InstanceManager.getInstance().resolvePath("versions"));
        String[] versions = null;
        if (versionsDir.exists() && versionsDir.isDirectory()) {
            versions = versionsDir.list((current, name) -> new File(current, name).isDirectory());
        }

        if (versions == null || versions.length == 0) {
            InstanceManager.getInstance().setActiveInstance(oldInstance);
            return;
        }

        final String selectedVersionName = (String) JOptionPane.showInputDialog(this,
                localeManager.get("label.selectVersion", "Select Version:"),
                localeManager.get("dialog.restore.title", "Restore Textures"),
                JOptionPane.QUESTION_MESSAGE, null,
                versions, versions[0]);

        if (selectedVersionName == null) {
            InstanceManager.getInstance().setActiveInstance(oldInstance);
            return;
        }

        final VersionManager versionManager = new VersionManager();
        try {
            List<Version> allVersions = versionManager
                    .loadVersions("https://raw.githubusercontent.com/NLauncher/components/main/versions.json");
            Version targetVersion = allVersions.stream()
                    .filter(v -> v.getName().equals(selectedVersionName))
                    .findFirst()
                    .orElse(null);

            if (targetVersion == null || targetVersion.getUrl().startsWith("file:")) {
                JOptionPane.showMessageDialog(this,
                        localeManager.get("error.restoreOfficialOnly"),
                        localeManager.get("error.cannotRestore.title"),
                        JOptionPane.WARNING_MESSAGE);
                InstanceManager.getInstance().setActiveInstance(oldInstance);
                return;
            }

            int choice = JOptionPane.showConfirmDialog(this,
                    localeManager.get("dialog.confirmRestore.message"),
                    localeManager.get("dialog.confirmRestore.title"),
                    JOptionPane.YES_NO_OPTION);

            if (choice != JOptionPane.YES_OPTION)
                return;

            ProgressMonitor progressMonitor = new ProgressMonitor(this,
                    localeManager.get("progress.downloadingVersion", selectedVersionName), "", 0, 100);
            progressMonitor.setMillisToPopup(0);

            SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
                @Override
                protected Void doInBackground() throws Exception {
                    File apkFile = versionManager.downloadVersion(targetVersion, progress -> {
                        publish((int) (progress * 100));
                    }, () -> isCancelled());

                    progressMonitor.setNote(localeManager.get("progress.extractingTextures"));
                    progressMonitor.setProgress(0);

                    ArchiveExtractor.extractDefaultTextures(apkFile,
                            new File(InstanceManager.getInstance().resolvePath("versions/" + selectedVersionName)));
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
        loadUserTextures();
    }

    private void loadUserTextures() {
        listModel.clear();
        if (texturesDir.exists() && texturesDir.isDirectory()) {
            File[] files = texturesDir
                    .listFiles((dir, name) -> name.endsWith(".zip") || name.endsWith(".rar") || name.endsWith(".tar") ||
                            name.endsWith(".7z") || name.endsWith(".mcpack"));
            if (files != null) {
                Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                for (File file : files) {
                    listModel.addElement(file);
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

    private class VersionGridRenderer extends JPanel implements ListCellRenderer<File> {
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
        public Component getListCellRendererComponent(JList<? extends File> list, File value, int index,
                boolean isSelected, boolean cellHasFocus) {
            this.isSelected = isSelected;

            String displayName = value.getName();
            int lastDot = displayName.lastIndexOf('.');
            if (lastDot > 0) {
                displayName = displayName.substring(0, lastDot);
            }
            nameLabel.setText(displayName);

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
                g2d.setColor(isDark ? new Color(255, 255, 255, 40) : new Color(0, 0, 0, 30));
                g2d.fillRoundRect(GAP, GAP, getWidth() - GAP * 2, getHeight() - GAP * 2, 15, 15);
            } else {
                g2d.setColor(isDark ? new Color(0, 0, 0, 60) : new Color(255, 255, 255, 60));
                g2d.fillRoundRect(GAP, GAP, getWidth() - GAP * 2, getHeight() - GAP * 2, 15, 15);
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