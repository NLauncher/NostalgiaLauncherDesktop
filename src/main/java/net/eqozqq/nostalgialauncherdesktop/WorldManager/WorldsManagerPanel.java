package net.eqozqq.nostalgialauncherdesktop.WorldManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.eqozqq.nostalgialauncherdesktop.LocaleManager;
import net.eqozqq.nostalgialauncherdesktop.Instances.InstanceManager;
import org.apache.commons.io.FileUtils;
import org.spout.nbt.CompoundTag;
import org.spout.nbt.stream.NBTInputStream;
import org.spout.nbt.stream.NBTOutputStream;

public class WorldsManagerPanel extends JPanel {
    private static final String WORLDS_PATH = "game/storage/games/com.mojang/minecraftWorlds/";
    private static final String BACKUPS_PATH = "backups/";

    private final LocaleManager localeManager;
    private final boolean isDark;
    private DefaultListModel<WorldEntry> listModel;
    private JList<WorldEntry> worldsList;
    private JPanel infoPanel;
    private JSplitPane splitPane;
    private Level currentLevel;
    private File currentWorldFolder;

    private JLabel lastPlayedLabel;
    private JTextField worldNameField;
    private JTextField folderNameField;
    private JTextField seedField;
    private JTextField timeField;
    private JTextField dayCycleLockField;
    private JTextField playerLocationField;
    private JComboBox<String> gamemodeComboBox;
    private JCheckBox allowFlyingBox;
    private JCheckBox flyingBox;
    private JCheckBox invulnerableBox;
    private JCheckBox instaBuildBox;
    private JCheckBox spawnMobsBox;

    private static class WorldEntry {
        File folder;
        String name;
        long seed;
        int gameType;

        public WorldEntry(File folder, String name, long seed, int gameType) {
            this.folder = folder;
            this.name = name;
            this.seed = seed;
            this.gameType = gameType;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public WorldsManagerPanel(LocaleManager localeManager, String themeName) {
        this.localeManager = localeManager;
        this.isDark = themeName.contains("Dark");
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel mainCard = createCardPanel();
        mainCard.setLayout(new BorderLayout(10, 10));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel titleLabel = new JLabel(localeManager.get("nav.worlds"));
        titleLabel.setFont(getFont(Font.BOLD, 24f));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);

        JLabel warningLabel = new JLabel(localeManager.get("message.backupRecommendation"));
        warningLabel.setFont(getFont(Font.PLAIN, 12f));
        warningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        warningLabel.setForeground(isDark ? new Color(200, 200, 200) : new Color(100, 100, 100));

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(3));
        headerPanel.add(warningLabel);

        mainCard.add(headerPanel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        worldsList = new JList<>(listModel);
        worldsList.setCellRenderer(new WorldGridRenderer());
        worldsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        worldsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        worldsList.setVisibleRowCount(-1);
        worldsList.setOpaque(false);
        worldsList.setBackground(new Color(0, 0, 0, 0));

        worldsList.setFixedCellWidth(290);
        worldsList.setFixedCellHeight(145);

        JScrollPane listScrollPane = new JScrollPane(worldsList);
        listScrollPane.setOpaque(false);
        listScrollPane.getViewport().setOpaque(false);
        listScrollPane.setBorder(null);
        listScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        listScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        listScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        listScrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        infoPanel = createWorldInfoPanel();
        infoPanel.setMinimumSize(new Dimension(500, 0));
        infoPanel.setPreferredSize(new Dimension(500, 0));

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, infoPanel);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(0);
        splitPane.setResizeWeight(1.0);

        infoPanel.setVisible(false);
        splitPane.setDividerLocation(1.0);

        mainCard.add(splitPane, BorderLayout.CENTER);

        worldsList.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = worldsList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        Rectangle cellBounds = worldsList.getCellBounds(index, index);
                        if (cellBounds != null && cellBounds.contains(e.getPoint())) {
                            worldsList.setSelectedIndex(index);
                            showPopupMenu(e);
                        }
                    }
                }
            }
        });

        worldsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                WorldEntry selected = worldsList.getSelectedValue();
                if (selected != null) {
                    loadWorldInfo(selected.folder);
                    infoPanel.setVisible(true);
                    splitPane.setDividerLocation(splitPane.getWidth() - infoPanel.getPreferredSize().width);
                } else {
                    infoPanel.setVisible(false);
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

    private JPanel createWorldInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isDark) {
                    g2d.setColor(new Color(45, 45, 45, 150));
                } else {
                    g2d.setColor(new Color(245, 245, 245, 150));
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
        gbc.anchor = GridBagConstraints.WEST;
        int y = 0;

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel editLabel = new JLabel(localeManager.get("nav.worlds"));
        editLabel.setFont(getFont(Font.BOLD, 18f));
        editLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);

        JButton closeBtn = new JButton("×");
        closeBtn.setFont(getFont(Font.BOLD, 20f));
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setForeground(isDark ? Color.GRAY : Color.DARK_GRAY);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> {
            worldsList.clearSelection();
            infoPanel.setVisible(false);
        });

        headerPanel.add(editLabel, BorderLayout.WEST);
        headerPanel.add(closeBtn, BorderLayout.EAST);

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 3;
        panel.add(headerPanel, gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        panel.add(createLabel(localeManager.get("label.lastPlayed")), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        lastPlayedLabel = new JLabel();
        lastPlayedLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);
        panel.add(lastPlayedLabel, gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        panel.add(createLabel(localeManager.get("label.worldName")), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        worldNameField = new JTextField();
        panel.add(worldNameField, gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(createLabel(localeManager.get("label.folderName")), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        folderNameField = new JTextField();
        folderNameField.setEditable(false);
        panel.add(folderNameField, gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        panel.add(createLabel(localeManager.get("label.seed")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        seedField = new JTextField();
        seedField.setEditable(false);
        panel.add(seedField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        JButton copySeedButton = new JButton(localeManager.get("button.copy"));
        copySeedButton.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(seedField.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(this, localeManager.get("info.seedCopied.message"),
                    localeManager.get("dialog.success.title"), JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(copySeedButton, gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(createLabel(localeManager.get("label.gamemode")), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gamemodeComboBox = new JComboBox<>(new String[] {
                localeManager.get("combo.gamemode.survival"),
                localeManager.get("combo.gamemode.creative")
        });
        panel.add(gamemodeComboBox, gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        panel.add(createLabel(localeManager.get("label.time")), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        timeField = new JTextField();
        panel.add(timeField, gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 3;
        JPanel timeButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        timeButtonsPanel.setOpaque(false);
        JButton morningButton = new JButton(localeManager.get("button.setMorning"));
        JButton nightButton = new JButton(localeManager.get("button.setNight"));
        morningButton.addActionListener(e -> timeField.setText("0"));
        nightButton.addActionListener(e -> timeField.setText("12000"));
        timeButtonsPanel.add(morningButton);
        timeButtonsPanel.add(nightButton);
        panel.add(timeButtonsPanel, gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        panel.add(createLabel(localeManager.get("label.playerLocation")), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;

        playerLocationField = new JTextField();
        playerLocationField.setEditable(false);
        playerLocationField.setOpaque(false);
        playerLocationField.setBorder(null);
        playerLocationField.setForeground(isDark ? Color.WHITE : Color.BLACK);
        panel.add(playerLocationField, gbc);
        y++;

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        panel.add(createLabel(localeManager.get("label.lockDayCycle")), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        dayCycleLockField = new JTextField();
        panel.add(dayCycleLockField, gbc);
        y++;

        allowFlyingBox = new JCheckBox(localeManager.get("checkbox.allowFlying"));
        flyingBox = new JCheckBox(localeManager.get("checkbox.currentlyFlying"));
        invulnerableBox = new JCheckBox(localeManager.get("checkbox.invulnerable"));
        instaBuildBox = new JCheckBox(localeManager.get("checkbox.instaBuild"));
        spawnMobsBox = new JCheckBox(localeManager.get("checkbox.spawnMobs"));

        allowFlyingBox.setOpaque(false);
        flyingBox.setOpaque(false);
        invulnerableBox.setOpaque(false);
        instaBuildBox.setOpaque(false);
        spawnMobsBox.setOpaque(false);

        Color cbColor = isDark ? Color.WHITE : Color.BLACK;
        allowFlyingBox.setForeground(cbColor);
        flyingBox.setForeground(cbColor);
        invulnerableBox.setForeground(cbColor);
        instaBuildBox.setForeground(cbColor);
        spawnMobsBox.setForeground(cbColor);

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 3;
        panel.add(allowFlyingBox, gbc);
        y++;
        gbc.gridy = y;
        panel.add(flyingBox, gbc);
        y++;
        gbc.gridy = y;
        panel.add(invulnerableBox, gbc);
        y++;
        gbc.gridy = y;
        panel.add(instaBuildBox, gbc);
        y++;
        gbc.gridy = y;
        panel.add(spawnMobsBox, gbc);
        y++;

        gbc.gridy = y;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 4, 4, 4);
        JButton saveButton = new JButton(localeManager.get("button.saveChanges"));
        saveButton.setFont(getFont(Font.BOLD, 14f));
        saveButton.addActionListener(e -> saveWorldInfo());
        panel.add(saveButton, gbc);

        gbc.gridy = y + 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JPanel() {
            {
                setOpaque(false);
            }
        }, gbc);

        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(isDark ? new Color(200, 200, 200) : new Color(60, 60, 60));
        label.setFont(getFont(Font.PLAIN, 13f));
        return label;
    }

    private void loadWorldInfo(File worldFolder) {
        this.currentWorldFolder = worldFolder;
        File levelDat = new File(worldFolder, "level.dat");
        if (!levelDat.exists()) {
            return;
        }
        try {
            currentLevel = NBTConverter.readLevel(readLevelDat(levelDat));
            populateInfoPanel();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, localeManager.get("error.readWorldData"),
                    localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateInfoPanel() {
        lastPlayedLabel.setText(DateFormat.getDateTimeInstance().format(new Date(currentLevel.getLastPlayed() * 1000)));
        worldNameField.setText(currentLevel.getLevelName());
        folderNameField.setText(currentWorldFolder.getName());
        seedField.setText(String.valueOf(currentLevel.getRandomSeed()));
        gamemodeComboBox.setSelectedIndex(currentLevel.getGameType());
        timeField.setText(String.valueOf(currentLevel.getTime()));
        dayCycleLockField.setText(String.valueOf(currentLevel.getDayCycleStopTime()));

        Vector3f loc = currentLevel.getPlayer().getLocation();
        if (loc != null) {
            String locText = String.format("X: %.2f, Y: %.2f, Z: %.2f", loc.getX(), loc.getY(), loc.getZ());
            playerLocationField.setText(locText);
            playerLocationField.setToolTipText(locText);
        } else {
            playerLocationField.setText("N/A");
            playerLocationField.setToolTipText(null);
        }

        playerLocationField.setCaretPosition(0);

        PlayerAbilities abilities = currentLevel.getPlayer().getAbilities();
        allowFlyingBox.setSelected(abilities.mayFly);
        flyingBox.setSelected(abilities.flying);
        invulnerableBox.setSelected(abilities.invulnerable);
        instaBuildBox.setSelected(abilities.instabuild);
        spawnMobsBox.setSelected(currentLevel.getSpawnMobs());
    }

    private void saveWorldInfo() {
        if (currentLevel == null || currentWorldFolder == null)
            return;

        try {
            currentLevel.setLastPlayed(System.currentTimeMillis() / 1000L);
            currentLevel.setLevelName(worldNameField.getText());
            currentLevel.setGameType(gamemodeComboBox.getSelectedIndex());
            currentLevel.setTime(Long.parseLong(timeField.getText()));
            currentLevel.setDayCycleStopTime(Long.parseLong(dayCycleLockField.getText()));

            PlayerAbilities abilities = currentLevel.getPlayer().getAbilities();
            abilities.mayFly = allowFlyingBox.isSelected();
            abilities.flying = flyingBox.isSelected();
            abilities.invulnerable = invulnerableBox.isSelected();
            abilities.instabuild = instaBuildBox.isSelected();
            currentLevel.setSpawnMobs(spawnMobsBox.isSelected());

            File levelDat = new File(currentWorldFolder, "level.dat");
            CompoundTag rootTag = NBTConverter.writeLevel(currentLevel);
            writeLevelDat(rootTag, levelDat);

            WorldEntry selectedEntry = worldsList.getSelectedValue();
            if (selectedEntry != null) {
                selectedEntry.name = currentLevel.getLevelName();
                selectedEntry.gameType = currentLevel.getGameType();
                worldsList.repaint();
            }

            populateInfoPanel();
            JOptionPane.showMessageDialog(this, localeManager.get("info.worldSaved"),
                    localeManager.get("dialog.success.title"), JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, localeManager.get("error.invalidNumberFormat"),
                    localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, localeManager.get("error.saveWorldData", ex.getMessage()),
                    localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void loadWorlds() {
        listModel.clear();
        File worldsDir = new File(InstanceManager.getInstance().resolvePath(WORLDS_PATH));
        if (worldsDir.exists() && worldsDir.isDirectory()) {
            File[] worldFolders = worldsDir.listFiles(File::isDirectory);
            if (worldFolders != null) {
                for (File worldFolder : worldFolders) {
                    if (worldFolder.getName().equals("_LevelCache"))
                        continue;

                    File levelDat = new File(worldFolder, "level.dat");
                    if (levelDat.exists()) {
                        try {
                            Level lvl = NBTConverter.readLevel(readLevelDat(levelDat));
                            listModel.addElement(new WorldEntry(
                                    worldFolder,
                                    lvl.getLevelName(),
                                    lvl.getRandomSeed(),
                                    lvl.getGameType()));
                        } catch (Exception e) {
                            listModel.addElement(new WorldEntry(worldFolder, worldFolder.getName(), 0, 0));
                        }
                    }
                }
            }
        }
        infoPanel.setVisible(false);
    }

    private void showPopupMenu(MouseEvent e) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem renameWorldItem = new JMenuItem(localeManager.get("menu.renameWorld"));
        JMenuItem renameFolderItem = new JMenuItem(localeManager.get("menu.renameFolder"));
        JMenuItem backupWorldItem = new JMenuItem(localeManager.get("menu.backupWorld"));
        JMenuItem deleteWorldItem = new JMenuItem(localeManager.get("menu.deleteWorld"));

        renameWorldItem.addActionListener(ae -> renameSelectedWorld());
        renameFolderItem.addActionListener(ae -> renameSelectedWorldFolder());
        backupWorldItem.addActionListener(ae -> backupSelectedWorld());
        deleteWorldItem.addActionListener(ae -> deleteSelectedWorld());

        popupMenu.add(renameWorldItem);
        popupMenu.add(renameFolderItem);
        popupMenu.add(backupWorldItem);
        popupMenu.add(deleteWorldItem);

        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void renameSelectedWorld() {
        WorldEntry selected = worldsList.getSelectedValue();
        if (selected == null)
            return;

        String newName = (String) JOptionPane.showInputDialog(this,
                localeManager.get("dialog.renameWorld.message"),
                localeManager.get("dialog.renameWorld.title"),
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                selected.name);

        if (newName != null && !newName.trim().isEmpty() && !newName.equals(selected.name)) {
            try {
                File levelDat = new File(selected.folder, "level.dat");
                if (levelDat.exists()) {
                    Level level = NBTConverter.readLevel(readLevelDat(levelDat));
                    level.setLevelName(newName.trim());
                    CompoundTag rootTag = NBTConverter.writeLevel(level);
                    writeLevelDat(rootTag, levelDat);

                    selected.name = newName.trim();
                    worldsList.repaint();

                    if (currentWorldFolder != null && currentWorldFolder.equals(selected.folder)) {
                        worldNameField.setText(selected.name);
                        currentLevel.setLevelName(selected.name);
                    }

                    JOptionPane.showMessageDialog(this, localeManager.get("info.worldRenamed"),
                            localeManager.get("dialog.success.title"), JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, localeManager.get("error.saveWorldData", e.getMessage()),
                        localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void renameSelectedWorldFolder() {
        WorldEntry selected = worldsList.getSelectedValue();
        if (selected == null)
            return;

        String newName = (String) JOptionPane.showInputDialog(this,
                localeManager.get("dialog.renameFolder.message"),
                localeManager.get("dialog.renameFolder.title"),
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                selected.folder.getName());

        if (newName != null && !newName.trim().isEmpty() && !newName.equals(selected.folder.getName())) {
            File newFile = new File(selected.folder.getParent(), newName.trim());
            if (newFile.exists()) {
                JOptionPane.showMessageDialog(this, localeManager.get("error.folderExists"),
                        localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (selected.folder.renameTo(newFile)) {
                loadWorlds();
            } else {
                JOptionPane.showMessageDialog(this, localeManager.get("error.renameFolderFailed"),
                        localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private CompoundTag readLevelDat(File levelDatFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(levelDatFile);
                BufferedInputStream is = new BufferedInputStream(fis)) {
            is.skip(8);
            try (NBTInputStream nbtStream = new NBTInputStream(is, false, true)) {
                return (CompoundTag) nbtStream.readTag();
            }
        }
    }

    private void writeLevelDat(CompoundTag data, File levelDatFile) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (NBTOutputStream nbtOut = new NBTOutputStream(bos, false, true)) {
            nbtOut.writeTag(data);
        }

        try (FileOutputStream os = new FileOutputStream(levelDatFile);
                DataOutputStream dos = new DataOutputStream(os)) {
            int length = bos.size();
            dos.writeInt(3);
            dos.writeInt(Integer.reverseBytes(length));
            bos.writeTo(dos);
        }
    }

    private void backupSelectedWorld() {
        WorldEntry selected = worldsList.getSelectedValue();
        if (selected == null)
            return;

        File backupsDir = new File(InstanceManager.getInstance().resolvePath(BACKUPS_PATH));
        if (!backupsDir.exists()) {
            backupsDir.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String zipFileName = selected.folder.getName() + "_" + timeStamp + ".zip";
        File zipFile = new File(backupsDir, zipFileName);

        try {
            zipDirectory(selected.folder, zipFile);
            JOptionPane.showMessageDialog(this, localeManager.get("info.backupSuccess", zipFile.getAbsolutePath()),
                    localeManager.get("dialog.success.title"), JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, localeManager.get("error.backupFailed", ex.getMessage()),
                    localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void deleteSelectedWorld() {
        WorldEntry selected = worldsList.getSelectedValue();
        if (selected == null)
            return;

        int response = JOptionPane.showConfirmDialog(this,
                localeManager.get("dialog.deleteWorld.message", selected.name),
                localeManager.get("dialog.deleteWorld.title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            try {
                FileUtils.deleteDirectory(selected.folder);
                loadWorlds();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, localeManager.get("error.deleteWorldFailed", ex.getMessage()),
                        localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void zipDirectory(File sourceDir, File outputFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputFile); ZipOutputStream zos = new ZipOutputStream(fos)) {
            Files.walk(sourceDir.toPath()).filter(path -> !Files.isDirectory(path)).forEach(path -> {
                ZipEntry zipEntry = new ZipEntry(sourceDir.toPath().relativize(path).toString());
                try {
                    zos.putNextEntry(zipEntry);
                    Files.copy(path, zos);
                    zos.closeEntry();
                } catch (IOException ex) {
                    System.err.println("Error while zipping file: " + path);
                }
            });
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

    private class WorldGridRenderer extends JPanel implements ListCellRenderer<WorldEntry> {
        private JLabel nameLabel;
        private JLabel seedLabel;
        private JLabel modeLabel;
        private boolean isSelected;
        private final int GAP = 8;

        public WorldGridRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10 + GAP, 15 + GAP, 10 + GAP, 15 + GAP));

            nameLabel = new JLabel();
            nameLabel.setFont(WorldsManagerPanel.this.getFont(Font.BOLD, 16f));

            seedLabel = new JLabel();
            seedLabel.setFont(WorldsManagerPanel.this.getFont(Font.PLAIN, 12f));

            modeLabel = new JLabel();
            modeLabel.setFont(WorldsManagerPanel.this.getFont(Font.PLAIN, 13f));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            add(nameLabel, gbc);

            gbc.gridy++;
            gbc.insets = new Insets(5, 0, 0, 0);
            add(seedLabel, gbc);

            gbc.gridy++;
            gbc.weighty = 1.0;
            gbc.anchor = GridBagConstraints.SOUTHWEST;
            add(modeLabel, gbc);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends WorldEntry> list, WorldEntry value, int index,
                boolean isSelected, boolean cellHasFocus) {
            this.isSelected = isSelected;

            nameLabel.setText(value.name);
            seedLabel.setText("Seed: " + value.seed);

            String modeStr = value.gameType == 1 ? localeManager.get("combo.gamemode.creative")
                    : localeManager.get("combo.gamemode.survival");
            modeLabel.setText(modeStr);

            Color fg = isDark ? Color.WHITE : Color.BLACK;
            Color subFg = isDark ? new Color(200, 200, 200) : new Color(80, 80, 80);

            if (isSelected) {
                fg = isDark ? Color.WHITE : Color.BLACK;
                subFg = isDark ? Color.WHITE : Color.BLACK;
            }

            nameLabel.setForeground(fg);
            seedLabel.setForeground(subFg);
            modeLabel.setForeground(subFg);

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