package net.eqozqq.nostalgialauncherdesktop.Proxy;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.eqozqq.nostalgialauncherdesktop.LocaleManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.util.List;

public class ProxyPanel extends JPanel {

    private final LocaleManager localeManager;
    private final boolean isDark;
    private final double scaleFactor;

    private JTextField addressField;
    private JTextField portField;
    private JButton toggleButton;
    private JButton saveButton;
    private JLabel statusLabel;

    private DefaultListModel<Server> listModel;
    private JList<Server> serverList;
    private JSplitPane splitPane;
    private Server currentSelectedServer;

    public ProxyPanel(LocaleManager localeManager, String themeName, double scaleFactor) {
        this.localeManager = localeManager;
        this.isDark = themeName.contains("Dark");
        this.scaleFactor = scaleFactor;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel mainCard = createCardPanel();
        mainCard.setLayout(new BorderLayout(10, 10));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel titleLabel = new JLabel(localeManager.get("nav.proxy"));
        titleLabel.setFont(getFont(Font.BOLD, 24f));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);
        headerPanel.add(titleLabel);

        mainCard.add(headerPanel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        refreshServerList();

        serverList = new JList<>(listModel);
        serverList.setCellRenderer(new ServerGridRenderer());
        serverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serverList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        serverList.setVisibleRowCount(-1);
        serverList.setOpaque(false);
        serverList.setBackground(new Color(0, 0, 0, 0));

        serverList.setFixedCellWidth((int) (280 * scaleFactor));
        serverList.setFixedCellHeight((int) (80 * scaleFactor));

        JScrollPane listScrollPane = new JScrollPane(serverList);
        listScrollPane.setOpaque(false);
        listScrollPane.getViewport().setOpaque(false);
        listScrollPane.setBorder(null);
        listScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        listScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(listScrollPane, BorderLayout.CENTER);

        JLabel listLabel = new JLabel(localeManager.get("proxy.savedServers", "Saved Servers"));
        listLabel.setFont(getFont(Font.BOLD, (float) (14 * scaleFactor)));
        listLabel.setForeground(isDark ? new Color(200, 200, 200) : new Color(100, 100, 100));
        listLabel.setBorder(new EmptyBorder(0, (int) (5 * scaleFactor), (int) (5 * scaleFactor), 0));
        leftPanel.add(listLabel, BorderLayout.NORTH);

        JPanel rightPanel = createDirectConnectPanel();
        rightPanel.setMinimumSize(new Dimension((int) (350 * scaleFactor), 0));
        rightPanel.setPreferredSize(new Dimension((int) (350 * scaleFactor), 0));

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(0);
        splitPane.setResizeWeight(1.0);

        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem(localeManager.get("menu.delete", "Delete"));
        contextMenu.add(deleteItem);

        serverList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    serverList.setSelectedIndex(serverList.locationToIndex(e.getPoint()));
                    if (serverList.getSelectedIndex() != -1) {
                        contextMenu.show(serverList, e.getX(), e.getY());
                    }
                }
            }
        });

        deleteItem.addActionListener(e -> deleteServer(serverList.getSelectedValue()));

        serverList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Server selected = serverList.getSelectedValue();
                if (selected != null) {
                    currentSelectedServer = selected;
                    loadServerInfo(selected);
                }
            }
        });

        mainCard.add(splitPane, BorderLayout.CENTER);
        add(mainCard, BorderLayout.CENTER);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                updateUIState();
            }
        });

        updateUIState();
    }

    private JPanel createDirectConnectPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isDark) {
                    g2d.setColor(new Color(30, 30, 30, 180));
                } else {
                    g2d.setColor(new Color(245, 245, 245, 180));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder((int) (15 * scaleFactor), (int) (20 * scaleFactor),
                (int) (15 * scaleFactor), (int) (20 * scaleFactor)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets((int) (6 * scaleFactor), (int) (6 * scaleFactor), (int) (6 * scaleFactor),
                (int) (6 * scaleFactor));
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        int y = 0;

        JLabel headerLabel = new JLabel(localeManager.get("proxy.directConnect", "Direct Connect"));
        headerLabel.setFont(getFont(Font.BOLD, (float) (18 * scaleFactor)));
        headerLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = y++;
        panel.add(headerLabel, gbc);

        gbc.gridy = y++;
        panel.add(Box.createVerticalStrut((int) (10 * scaleFactor)), gbc);

        addressField = new JTextField();
        addressField.setFont(getFont(Font.PLAIN, (float) (14 * scaleFactor)));
        addressField.putClientProperty("JTextField.placeholderText", localeManager.get("proxy.placeholder.address"));
        addressField.setPreferredSize(new Dimension(0, (int) (40 * scaleFactor)));
        gbc.gridy = y++;
        panel.add(addressField, gbc);

        portField = new JTextField("19132");
        portField.setFont(getFont(Font.PLAIN, (float) (14 * scaleFactor)));
        portField.putClientProperty("JTextField.placeholderText", localeManager.get("proxy.placeholder.port"));
        portField.setPreferredSize(new Dimension(0, (int) (40 * scaleFactor)));
        gbc.gridy = y++;
        panel.add(portField, gbc);

        saveButton = new JButton(localeManager.get("proxy.button.saveToList", "Save to List"));
        saveButton.setFont(getFont(Font.PLAIN, (float) (12 * scaleFactor)));
        saveButton.setPreferredSize(new Dimension(0, (int) (40 * scaleFactor)));
        saveButton.addActionListener(e -> saveCurrentServer());
        gbc.gridy = y++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(saveButton, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = y++;
        gbc.weighty = 0.5;
        panel.add(Box.createVerticalStrut((int) (20 * scaleFactor)), gbc);

        statusLabel = new JLabel(localeManager.get("proxy.status.stopped"));
        statusLabel.setFont(getFont(Font.PLAIN, (float) (12 * scaleFactor)));
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = y++;
        gbc.weighty = 0;
        panel.add(statusLabel, gbc);

        toggleButton = new JButton(localeManager.get("proxy.button.enable"));
        toggleButton.setFont(getFont(Font.BOLD, (float) (16 * scaleFactor)));
        toggleButton.setPreferredSize(new Dimension(0, (int) (55 * scaleFactor)));
        toggleButton.addActionListener(e -> toggleProxy());
        gbc.gridy = y++;
        panel.add(toggleButton, gbc);

        gbc.gridy = y++;
        panel.add(Box.createVerticalStrut((int) (10 * scaleFactor)), gbc);

        return panel;
    }

    private void loadServerInfo(Server server) {
        if (server == null)
            return;
        addressField.setText(server.getAddress());
        portField.setText(String.valueOf(server.getPort()));
    }

    private void saveCurrentServer() {
        String address = addressField.getText().trim();
        String portStr = portField.getText().trim();

        if (address.isEmpty()) {
            JOptionPane.showMessageDialog(this, localeManager.get("proxy.error.invalidAddress"));
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, localeManager.get("proxy.error.invalidPort"));
            return;
        }

        for (int i = 0; i < listModel.size(); i++) {
            Server s = listModel.get(i);
            if (s.getAddress().equals(address) && s.getPort() == port) {
                return;
            }
        }

        listModel.addElement(new Server(address, port));
        saveList();
        serverList.repaint();
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

    private void refreshServerList() {
        listModel.clear();
        List<Server> servers = ServerStorage.loadServers();
        if (servers != null) {
            for (Server s : servers) {
                listModel.addElement(s);
            }
        }
    }

    private void toggleProxy() {
        ProxyManager manager = ProxyManager.getInstance();
        if (manager.isRunning()) {
            manager.stopProxy();
        } else {
            String address = addressField.getText().trim();
            String portStr = portField.getText().trim();

            if (address.isEmpty()) {
                JOptionPane.showMessageDialog(this, localeManager.get("proxy.error.invalidAddress"));
                return;
            }

            int port;
            try {
                port = Integer.parseInt(portStr);
                if (port < 1 || port > 65535)
                    throw new NumberFormatException();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, localeManager.get("proxy.error.invalidPort"));
                return;
            }

            manager.startProxy(address, port);
        }
        updateUIState();
        serverList.repaint();
    }

    private void updateUIState() {
        ProxyManager manager = ProxyManager.getInstance();
        boolean running = manager.isRunning();

        SwingUtilities.invokeLater(() -> {
            if (running) {
                toggleButton.setText(localeManager.get("proxy.button.disable"));
                statusLabel.setText(String.format(localeManager.get("proxy.status.running"),
                        manager.getCurrentAddress(), manager.getCurrentPort()));
                statusLabel.setForeground(new Color(100, 200, 100));

                addressField.setEnabled(false);
                portField.setEnabled(false);
                saveButton.setEnabled(false);
            } else {
                toggleButton.setText(localeManager.get("proxy.button.enable"));
                statusLabel.setText(localeManager.get("proxy.status.stopped"));
                statusLabel.setForeground(Color.GRAY);
                addressField.setEnabled(true);
                portField.setEnabled(true);
                saveButton.setEnabled(true);
            }
        });
    }

    private void deleteServer(Server server) {
        if (server != null) {
            listModel.removeElement(server);
            saveList();
            if (currentSelectedServer == server) {
                serverList.clearSelection();
                currentSelectedServer = null;
            }
        }
    }

    private void saveList() {
        List<Server> servers = java.util.Collections.list(listModel.elements());
        ServerStorage.saveServers(servers);
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

    private class ServerGridRenderer extends JPanel implements ListCellRenderer<Server> {
        private JLabel addressLabel;
        private JLabel portLabel;
        private JLabel statusLabel;

        public ServerGridRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(
                    (int) (10 * scaleFactor), (int) (15 * scaleFactor),
                    (int) (10 * scaleFactor), (int) (15 * scaleFactor)));

            addressLabel = new JLabel();
            addressLabel.setFont(ProxyPanel.this.getFont(Font.BOLD, (float) (16 * scaleFactor)));

            portLabel = new JLabel();
            portLabel.setFont(ProxyPanel.this.getFont(Font.PLAIN, (float) (13 * scaleFactor)));

            statusLabel = new JLabel();
            statusLabel.setFont(ProxyPanel.this.getFont(Font.PLAIN, (float) (12 * scaleFactor)));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            add(addressLabel, gbc);

            gbc.gridy++;
            gbc.insets = new Insets((int) (2 * scaleFactor), 0, 0, 0);
            add(portLabel, gbc);

            gbc.gridy++;
            gbc.weighty = 1.0;
            gbc.anchor = GridBagConstraints.SOUTHWEST;
            add(statusLabel, gbc);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Server> list, Server value, int index,
                boolean isSelected, boolean cellHasFocus) {

            addressLabel.setText(value.getAddress());
            portLabel.setText("Port: " + value.getPort());

            ProxyManager manager = ProxyManager.getInstance();
            boolean isRunning = manager.isRunning() &&
                    manager.getCurrentAddress().equals(value.getAddress()) &&
                    manager.getCurrentPort() == value.getPort();

            if (isRunning) {
                statusLabel.setText(localeManager.get("proxy.status.active", "Active"));
                statusLabel.setForeground(new Color(100, 200, 100));
            } else {
                statusLabel.setText(localeManager.get("proxy.status.idle", "Idle"));
                statusLabel.setForeground(isDark ? new Color(150, 150, 150) : new Color(100, 100, 100));
            }

            Color fg = isDark ? Color.WHITE : Color.BLACK;
            Color subFg = isDark ? new Color(200, 200, 200) : new Color(80, 80, 80);

            if (isSelected) {
                fg = isDark ? Color.WHITE : Color.BLACK;
                subFg = isDark ? Color.WHITE : Color.BLACK;
            }

            addressLabel.setForeground(fg);
            portLabel.setForeground(subFg);

            if (!isRunning) {
                statusLabel.setForeground(subFg);
            }

            this.putClientProperty("isSelected", isSelected);

            return this;
        }

        @Override

        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int margin = (int) (5 * scaleFactor);

            Boolean isSelected = (Boolean) getClientProperty("isSelected");
            if (Boolean.TRUE.equals(isSelected)) {
                g2d.setColor(isDark ? new Color(255, 255, 255, 40) : new Color(0, 0, 0, 30));
            } else {
                g2d.setColor(isDark ? new Color(0, 0, 0, 60) : new Color(255, 255, 255, 60));
            }

            g2d.fillRoundRect(margin, margin, getWidth() - (margin * 2), getHeight() - (margin * 2), 10, 10);

            g2d.dispose();
            super.paintComponent(g);
        }
    }
}