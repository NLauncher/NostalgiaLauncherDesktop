package net.eqozqq.nostalgialauncherdesktop.Instances;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.util.List;

import net.eqozqq.nostalgialauncherdesktop.LocaleManager;

public class InstancesPanel extends JPanel {
    private final LocaleManager localeManager;
    private final boolean isDark;
    
    private JList<String> instancesList;
    private DefaultListModel<String> listModel;
    private JSplitPane splitPane;
    private JPanel rightPanel;
    private Runnable onInstanceChanged;
    
    private JLabel selectedInstanceLabel;
    private JLabel statusLabel;
    private JButton selectButton;
    private JButton renameButton;
    private JButton deleteButton;
    
    private String selectedInstance;

    public InstancesPanel(LocaleManager localeManager, String themeName) {
        this.localeManager = localeManager;
        this.isDark = themeName.contains("Dark");
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel mainCard = createCardPanel();
        mainCard.setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel(localeManager.get("nav.instances"));
        titleLabel.setFont(getFont(Font.BOLD, 24f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        mainCard.add(titleLabel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        instancesList = new JList<>(listModel);
        instancesList.setCellRenderer(new InstanceGridRenderer());
        instancesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        instancesList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        instancesList.setVisibleRowCount(-1);
        instancesList.setOpaque(false);
        instancesList.setBackground(new Color(0, 0, 0, 0));
        
        instancesList.setFixedCellWidth(290);
        instancesList.setFixedCellHeight(100);

        JScrollPane listScrollPane = new JScrollPane(instancesList);
        listScrollPane.setOpaque(false);
        listScrollPane.getViewport().setOpaque(false);
        listScrollPane.setBorder(null);
        listScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        listScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        listScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        listScrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(listScrollPane, BorderLayout.CENTER);

        JButton createButton = new JButton(localeManager.get("button.instance.add"));
        createButton.setFont(getFont(Font.BOLD, 14f));
        createButton.setPreferredSize(new Dimension(0, 45));
        createButton.addActionListener(e -> createInstance());
        
        JPanel createButtonPanel = new JPanel(new BorderLayout());
        createButtonPanel.setOpaque(false);
        createButtonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        createButtonPanel.add(createButton, BorderLayout.CENTER);
        
        leftPanel.add(createButtonPanel, BorderLayout.SOUTH);

        rightPanel = createRightPanel();
        rightPanel.setMinimumSize(new Dimension(450, 0));
        rightPanel.setPreferredSize(new Dimension(450, 0));

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(0);
        splitPane.setResizeWeight(1.0);
        
        rightPanel.setVisible(false);
        splitPane.setDividerLocation(1.0);

        mainCard.add(splitPane, BorderLayout.CENTER);

        instancesList.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = instancesList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        Rectangle cellBounds = instancesList.getCellBounds(index, index);
                        if (cellBounds != null && cellBounds.contains(e.getPoint())) {
                            instancesList.setSelectedIndex(index);
                        }
                    }
                }
            }
        });

        instancesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedInstance = instancesList.getSelectedValue();
                if (selectedInstance != null) {
                    updateRightPanel(selectedInstance);
                    rightPanel.setVisible(true);
                    splitPane.setDividerLocation(splitPane.getWidth() - rightPanel.getPreferredSize().width);
                } else {
                    rightPanel.setVisible(false);
                }
            }
        });

        reload();
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
        selectedInstanceLabel = new JLabel(""); 
        selectedInstanceLabel.setFont(getFont(Font.BOLD, 18f));
        selectedInstanceLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);
        
        JButton closeBtn = new JButton("×");
        closeBtn.setFont(getFont(Font.BOLD, 20f));
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setForeground(isDark ? Color.GRAY : Color.DARK_GRAY);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> {
            instancesList.clearSelection();
            rightPanel.setVisible(false);
        });
        
        headerPanel.add(selectedInstanceLabel, BorderLayout.WEST);
        headerPanel.add(closeBtn, BorderLayout.EAST);
        
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        panel.add(headerPanel, gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        statusLabel = new JLabel();
        statusLabel.setFont(getFont(Font.PLAIN, 14f));
        panel.add(statusLabel, gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        selectButton = new JButton(localeManager.get("button.select"));
        selectButton.setFont(getFont(Font.BOLD, 14f));
        selectButton.addActionListener(e -> selectInstance());
        panel.add(selectButton, gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        renameButton = new JButton(localeManager.get("menu.rename"));
        renameButton.setFont(getFont(Font.BOLD, 14f));
        renameButton.addActionListener(e -> renameInstance());
        panel.add(renameButton, gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        deleteButton = new JButton(localeManager.get("menu.delete"));
        deleteButton.setFont(getFont(Font.BOLD, 14f));
        deleteButton.addActionListener(e -> deleteInstance());
        panel.add(deleteButton, gbc);
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

    private void updateRightPanel(String instanceName) {
        selectedInstanceLabel.setText(instanceName);
        
        boolean isActive = instanceName.equals(InstanceManager.getInstance().getActiveInstance());
        boolean isDefault = instanceName.equals(InstanceManager.getDefaultInstanceName());
        
        if (isActive) {
            statusLabel.setText(localeManager.get("status.ready")); 
            statusLabel.setForeground(new Color(76, 175, 80)); 
            selectButton.setEnabled(false);
            selectButton.setText(localeManager.get("status.ready")); 
        } else {
            statusLabel.setText("Inactive");
            statusLabel.setForeground(isDark ? Color.GRAY : Color.DARK_GRAY);
            selectButton.setEnabled(true);
            selectButton.setText(localeManager.get("button.select"));
        }
        
        renameButton.setEnabled(!isDefault);
        deleteButton.setEnabled(!isDefault);
    }

    public void setOnInstanceChanged(Runnable callback) {
        this.onInstanceChanged = callback;
    }

    public void reload() {
        listModel.clear();
        List<String> instances = InstanceManager.getInstance().getInstances();
        for (String instance : instances) {
            listModel.addElement(instance);
        }
        if (selectedInstance != null && instances.contains(selectedInstance)) {
            updateRightPanel(selectedInstance);
        } else {
            rightPanel.setVisible(false);
        }
    }

    private void createInstance() {
        String name = JOptionPane.showInputDialog(this, 
                localeManager.get("dialog.instance.addPrompt"),
                localeManager.get("dialog.instances.title"), 
                JOptionPane.PLAIN_MESSAGE);
        
        if (name != null && !name.trim().isEmpty()) {
            try {
                InstanceManager.getInstance().createInstance(name.trim());
                reload();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                        e.getMessage(), 
                        localeManager.get("dialog.error.title"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void renameInstance() {
        if (selectedInstance == null) return;
        
        if (selectedInstance.equals(InstanceManager.getDefaultInstanceName())) {
             JOptionPane.showMessageDialog(this, 
                    localeManager.get("error.instance.reservedName"),
                    localeManager.get("dialog.error.title"), 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newName = (String) JOptionPane.showInputDialog(this, 
                localeManager.get("menu.rename"),
                localeManager.get("dialog.instances.title"), 
                JOptionPane.PLAIN_MESSAGE,
                null, null, selectedInstance);
        
        if (newName != null && !newName.trim().isEmpty() && !newName.trim().equals(selectedInstance)) {
            try {
                InstanceManager.getInstance().renameInstance(selectedInstance, newName.trim());
                selectedInstance = newName.trim();
                reload();
                instancesList.setSelectedValue(selectedInstance, true);
                if (onInstanceChanged != null) {
                    onInstanceChanged.run();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                        e.getMessage(), 
                        localeManager.get("dialog.error.title"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void selectInstance() {
        if (selectedInstance == null) return;

        if (selectedInstance.equals(InstanceManager.getInstance().getActiveInstance())) {
            return;
        }

        InstanceManager.getInstance().setActiveInstance(selectedInstance);
        updateRightPanel(selectedInstance);
        instancesList.repaint();
        
        if (onInstanceChanged != null) {
            onInstanceChanged.run();
        }
    }

    private void deleteInstance() {
        if (selectedInstance == null) return;

        if (selectedInstance.equals(InstanceManager.getDefaultInstanceName())) {
            JOptionPane.showMessageDialog(this, 
                    localeManager.get("error.instance.reservedName"),
                    localeManager.get("dialog.error.title"), 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
                localeManager.get("dialog.instance.deleteWarning"),
                localeManager.get("dialog.warning.title"), 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                InstanceManager.getInstance().deleteInstance(selectedInstance);
                reload();
                rightPanel.setVisible(false);
                if (onInstanceChanged != null) {
                    onInstanceChanged.run();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                        e.getMessage(), 
                        localeManager.get("dialog.error.title"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Font getFont(int style, float size) {
        try (InputStream fontStream = getClass().getResourceAsStream("/MPLUS1p-Regular.ttf")) {
            if (fontStream != null) {
                return Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(style, size);
            }
        } catch (Exception e) {
        }
        return new Font("SansSerif", style, (int) size);
    }

    private class InstanceGridRenderer extends JPanel implements ListCellRenderer<String> {
        private JLabel nameLabel;
        private JLabel statusLabel;
        private boolean isSelected;
        private final int GAP = 8; 

        public InstanceGridRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10 + GAP, 15 + GAP, 10 + GAP, 15 + GAP));

            nameLabel = new JLabel();
            nameLabel.setFont(InstancesPanel.this.getFont(Font.BOLD, 16f));
            
            statusLabel = new JLabel();
            statusLabel.setFont(InstancesPanel.this.getFont(Font.PLAIN, 12f));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            add(nameLabel, gbc);

            gbc.gridy++;
            gbc.insets = new Insets(5, 0, 0, 0);
            gbc.weighty = 1.0;
            gbc.anchor = GridBagConstraints.SOUTHWEST;
            add(statusLabel, gbc);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                boolean isSelected, boolean cellHasFocus) {
            this.isSelected = isSelected;
            
            nameLabel.setText(value);
            
            boolean isActive = value.equals(InstanceManager.getInstance().getActiveInstance());
            if (isActive) {
                statusLabel.setText(localeManager.get("status.ready")); 
                statusLabel.setForeground(new Color(76, 175, 80));
            } else {
                statusLabel.setText("Inactive");
                statusLabel.setForeground(isDark ? new Color(150, 150, 150) : new Color(100, 100, 100));
            }

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