package net.eqozqq.nostalgialauncherdesktop.Proxy;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.eqozqq.nostalgialauncherdesktop.LocaleManager;
import net.eqozqq.nostalgialauncherdesktop.FontManager;
import net.eqozqq.nostalgialauncherdesktop.Instances.InstanceManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

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

        mainCard.add(leftPanel, BorderLayout.CENTER);
        add(mainCard, BorderLayout.CENTER);

        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

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

        loadLastProxySettings();
        updateUIState();
    }

    private JPanel createFooterPanel() {
        JPanel footerCard = new JPanel(new GridBagLayout()) {
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

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets((int) (5 * scaleFactor), 0, (int) (5 * scaleFactor), (int) (15 * scaleFactor));
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        addressField = new JTextField();
        addressField.setFont(getFont(Font.PLAIN, (float) (14 * scaleFactor)));
        addressField.putClientProperty("JTextField.placeholderText", localeManager.get("proxy.placeholder.address"));
        addressField.setPreferredSize(new Dimension(0, (int) (45 * scaleFactor)));
        gbc.gridx = 0;
        gbc.gridy = 0;
        footerCard.add(addressField, gbc);

        portField = new JTextField("19132");
        portField.setFont(getFont(Font.PLAIN, (float) (14 * scaleFactor)));
        portField.putClientProperty("JTextField.placeholderText", localeManager.get("proxy.placeholder.port"));
        portField.setPreferredSize(new Dimension(0, (int) (45 * scaleFactor)));
        gbc.gridy = 1;
        footerCard.add(portField, gbc);

        gbc.weightx = 0;
        gbc.insets = new Insets((int) (5 * scaleFactor), 0, (int) (5 * scaleFactor), 0);

        toggleButton = new JButton(localeManager.get("proxy.button.enable")) {
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
        toggleButton.setOpaque(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setFocusPainted(false);
        toggleButton.setFont(getFont(Font.BOLD, (float) (14 * scaleFactor)));
        toggleButton.setForeground(isDark ? Color.WHITE : Color.BLACK);
        toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleButton.setPreferredSize(new Dimension((int) (180 * scaleFactor), (int) (45 * scaleFactor)));
        toggleButton.addActionListener(e -> toggleProxy());
        gbc.gridx = 1;
        gbc.gridy = 0;
        footerCard.add(toggleButton, gbc);

        saveButton = new JButton(localeManager.get("proxy.button.saveToList", "Save to List")) {
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
        saveButton.setOpaque(false);
        saveButton.setContentAreaFilled(false);
        saveButton.setBorderPainted(false);
        saveButton.setFocusPainted(false);
        saveButton.setFont(getFont(Font.PLAIN, (float) (12 * scaleFactor)));
        saveButton.setForeground(isDark ? Color.WHITE : Color.BLACK);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.setPreferredSize(new Dimension((int) (180 * scaleFactor), (int) (45 * scaleFactor)));
        saveButton.addActionListener(e -> saveCurrentServer());
        gbc.gridy = 1;
        footerCard.add(saveButton, gbc);

        statusLabel = new JLabel(localeManager.get("proxy.status.stopped"));
        statusLabel.setFont(getFont(Font.PLAIN, (float) (12 * scaleFactor)));
        statusLabel.setForeground(Color.GRAY);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, (int) (5 * scaleFactor), 0, 0);
        footerCard.add(statusLabel, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder((int) (15 * scaleFactor), 0, 0, 0));
        wrapper.add(footerCard, BorderLayout.CENTER);

        return wrapper;
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
            if (s != null && s.getAddress() != null && s.getAddress().equals(address) && s.getPort() == port) {
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
                if (s != null && s.getAddress() != null && !s.getAddress().trim().isEmpty()) {
                    listModel.addElement(s);
                }
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
            saveLastProxySettings();
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

    private void loadLastProxySettings() {
        File settingsFile = new File(InstanceManager.getDataRoot(), "launcher.properties");
        if (settingsFile.exists()) {
            Properties properties = new Properties();
            try (FileInputStream fis = new FileInputStream(settingsFile)) {
                properties.load(fis);
                String lastAddress = properties.getProperty("proxyAddress");
                String lastPort = properties.getProperty("proxyPort");
                if (lastAddress != null) {
                    addressField.setText(lastAddress);
                }
                if (lastPort != null) {
                    portField.setText(lastPort);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveLastProxySettings() {
        String address = addressField.getText().trim();
        String portStr = portField.getText().trim();
        File settingsFile = new File(InstanceManager.getDataRoot(), "launcher.properties");
        Properties properties = new Properties();
        if (settingsFile.exists()) {
            try (FileInputStream fis = new FileInputStream(settingsFile)) {
                properties.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        properties.setProperty("proxyAddress", address);
        properties.setProperty("proxyPort", portStr);
        try (FileOutputStream fos = new FileOutputStream(settingsFile)) {
            properties.store(fos, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        return FontManager.getRegularFont(style, size);
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