package net.eqozqq.nostalgialauncherdesktop.Instances;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.InputStream;
import java.util.List;

import net.eqozqq.nostalgialauncherdesktop.LocaleManager;

public class InstancesPanel extends JPanel {
    private final LocaleManager localeManager;
    private final boolean isDark;
    private JList<String> instancesList;
    private DefaultListModel<String> listModel;
    private JButton createButton;
    private JButton selectButton;
    private JButton deleteButton;
    private Runnable onInstanceChanged;

    public InstancesPanel(LocaleManager localeManager, String themeName) {
        this.localeManager = localeManager;
        this.isDark = themeName.contains("Dark");
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 40, 20, 40));

        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel(localeManager.get("nav.instances"));
        titleLabel.setFont(getFont(Font.BOLD, 24f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(titleLabel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        instancesList = new JList<>(listModel);
        instancesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        instancesList.setFont(getFont(Font.PLAIN, 14f));
        instancesList.setBackground(new Color(0, 0, 0, 0));
        instancesList.setOpaque(false);
        instancesList.setCellRenderer(new TransparentListCellRenderer());

        JScrollPane scrollPane = new JScrollPane(instancesList);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(isDark ? new Color(60, 60, 60) : new Color(200, 200, 200)));
        card.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        createButton = createButton(localeManager.get("button.instance.add"));
        selectButton = createButton(localeManager.get("button.select"));
        deleteButton = createButton(localeManager.get("menu.delete"));

        buttonPanel.add(createButton);
        buttonPanel.add(selectButton);
        buttonPanel.add(deleteButton);

        card.add(buttonPanel, BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);

        createButton.addActionListener(e -> createInstance());
        selectButton.addActionListener(e -> selectInstance());
        deleteButton.addActionListener(e -> deleteInstance());

        reload();
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

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(getFont(Font.BOLD, 14f));
        btn.setPreferredSize(new Dimension(160, 45));
        return btn;
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

    public void setOnInstanceChanged(Runnable callback) {
        this.onInstanceChanged = callback;
    }

    public void reload() {
        listModel.clear();
        List<String> instances = InstanceManager.getInstance().getInstances();
        String current = InstanceManager.getInstance().getActiveInstance();
        for (String instance : instances) {
            if (instance.equals(current)) {
                listModel.addElement(instance + " (Active)");
            } else {
                listModel.addElement(instance);
            }
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

    private void selectInstance() {
        String selected = instancesList.getSelectedValue();
        if (selected == null) return;

        String cleanName = selected.replace(" (Active)", "");
        if (cleanName.equals(InstanceManager.getInstance().getActiveInstance())) {
            return;
        }

        InstanceManager.getInstance().setActiveInstance(cleanName);
        reload();
        if (onInstanceChanged != null) {
            onInstanceChanged.run();
        }
    }

    private void deleteInstance() {
        String selected = instancesList.getSelectedValue();
        if (selected == null) return;

        String cleanName = selected.replace(" (Active)", "");
        if (cleanName.equals(InstanceManager.getDefaultInstanceName())) {
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
                InstanceManager.getInstance().deleteInstance(cleanName);
                reload();
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
}