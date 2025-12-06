package net.eqozqq.nostalgialauncherdesktop.WorldManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.eqozqq.nostalgialauncherdesktop.LocaleManager;
import net.eqozqq.nostalgialauncherdesktop.StyledDialog;
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
    private DefaultListModel<File> listModel;
    private JList<File> worldsList;
    private JPanel infoPanel;
    private Level currentLevel;
    private File currentWorldFolder;

    private JLabel lastPlayedLabel;
    private JTextField worldNameField;
    private JTextField folderNameField;
    private JTextField seedField;
    private JTextField timeField;
    private JTextField dayCycleLockField;
    private JLabel playerLocationLabel;
    private JComboBox<String> gamemodeComboBox;
    private JCheckBox allowFlyingBox;
    private JCheckBox flyingBox;
    private JCheckBox invulnerableBox;
    private JCheckBox instaBuildBox;
    private JCheckBox spawnMobsBox;

    public WorldsManagerPanel(LocaleManager localeManager, String themeName) {
        this.localeManager = localeManager;
        this.isDark = themeName.contains("Dark");
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 40, 20, 40));

        JPanel mainCard = createCardPanel();
        mainCard.setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel(localeManager.get("nav.worlds"));
        titleLabel.setFont(getFont(Font.BOLD, 24f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        mainCard.add(titleLabel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        worldsList = new JList<>(listModel);
        worldsList.setCellRenderer(new WorldListRenderer());
        worldsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        worldsList.setOpaque(false);
        worldsList.setBackground(new Color(0, 0, 0, 0));

        JScrollPane listScrollPane = new JScrollPane(worldsList);
        listScrollPane.setOpaque(false);
        listScrollPane.getViewport().setOpaque(false);
        listScrollPane
                .setBorder(BorderFactory.createLineBorder(isDark ? new Color(60, 60, 60) : new Color(200, 200, 200)));
        listScrollPane.setPreferredSize(new Dimension(250, 0));

        infoPanel = createWorldInfoPanel();
        infoPanel.setVisible(false);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, infoPanel);
        splitPane.setDividerLocation(280);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        mainCard.add(splitPane, BorderLayout.CENTER);

        worldsList.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = worldsList.locationToIndex(e.getPoint());
                    if (row != -1) {
                        worldsList.setSelectedIndex(row);
                        showPopupMenu(e);
                    }
                }
            }
        });

        worldsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                File selected = worldsList.getSelectedValue();
                if (selected != null) {
                    loadWorldInfo(selected);
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
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        int y = 0;

        gbc.gridx = 0;
        gbc.gridy = y;
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
            StyledDialog.showMessage(this, localeManager.get("info.seedCopied.message"),
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

        gbc.gridx = 1;
        gbc.gridy = y;
        JPanel timeButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
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
        panel.add(createLabel(localeManager.get("label.playerLocation")), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        playerLocationLabel = new JLabel();
        playerLocationLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);
        panel.add(playerLocationLabel, gbc);
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
        gbc.insets = new Insets(15, 4, 4, 4);
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
            infoPanel.setVisible(false);
            return;
        }
        try {
            currentLevel = NBTConverter.readLevel(readLevelDat(levelDat));
            populateInfoPanel();
            infoPanel.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            infoPanel.setVisible(false);
            StyledDialog.showMessage(this, localeManager.get("error.readWorldData"),
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
        playerLocationLabel.setText(String.format("X: %.2f, Y: %.2f, Z: %.2f", loc.getX(), loc.getY(), loc.getZ()));

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

            populateInfoPanel();
            StyledDialog.showMessage(this, localeManager.get("info.worldSaved"),
                    localeManager.get("dialog.success.title"), JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            StyledDialog.showMessage(this, localeManager.get("error.invalidNumberFormat"),
                    localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            StyledDialog.showMessage(this, localeManager.get("error.saveWorldData", ex.getMessage()),
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
                    if (!worldFolder.getName().equals("_LevelCache")) {
                        listModel.addElement(worldFolder);
                    }
                }
            }
        }
        infoPanel.setVisible(false);
    }

    private void showPopupMenu(MouseEvent e) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem renameFolderItem = new JMenuItem(localeManager.get("menu.renameFolder"));
        JMenuItem backupWorldItem = new JMenuItem(localeManager.get("menu.backupWorld"));
        JMenuItem deleteWorldItem = new JMenuItem(localeManager.get("menu.deleteWorld"));

        renameFolderItem.addActionListener(ae -> renameSelectedWorldFolder());
        backupWorldItem.addActionListener(ae -> backupSelectedWorld());
        deleteWorldItem.addActionListener(ae -> deleteSelectedWorld());

        popupMenu.add(renameFolderItem);
        popupMenu.add(backupWorldItem);
        popupMenu.add(deleteWorldItem);

        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void renameSelectedWorldFolder() {
        File selectedWorld = worldsList.getSelectedValue();
        if (selectedWorld == null)
            return;

        String newName = StyledDialog.showInput(this, localeManager.get("dialog.renameFolder.message"),
                localeManager.get("dialog.renameFolder.title"), JOptionPane.QUESTION_MESSAGE, selectedWorld.getName());
        if (newName != null && !newName.trim().isEmpty() && !newName.equals(selectedWorld.getName())) {
            File newFile = new File(selectedWorld.getParent(), newName.trim());
            if (newFile.exists()) {
                StyledDialog.showMessage(this, localeManager.get("error.folderExists"),
                        localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (selectedWorld.renameTo(newFile)) {
                loadWorlds();
            } else {
                StyledDialog.showMessage(this, localeManager.get("error.renameFolderFailed"),
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
        File selectedWorld = worldsList.getSelectedValue();
        if (selectedWorld == null)
            return;

        File backupsDir = new File(InstanceManager.getInstance().resolvePath(BACKUPS_PATH));
        if (!backupsDir.exists()) {
            backupsDir.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String zipFileName = selectedWorld.getName() + "_" + timeStamp + ".zip";
        File zipFile = new File(backupsDir, zipFileName);

        try {
            zipDirectory(selectedWorld, zipFile);
            StyledDialog.showMessage(this, localeManager.get("info.backupSuccess", zipFile.getAbsolutePath()),
                    localeManager.get("dialog.success.title"), JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            StyledDialog.showMessage(this, localeManager.get("error.backupFailed", ex.getMessage()),
                    localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void deleteSelectedWorld() {
        File selectedWorld = worldsList.getSelectedValue();
        if (selectedWorld == null)
            return;

        int response = StyledDialog.showConfirm(this,
                localeManager.get("dialog.deleteWorld.message", selectedWorld.getName()),
                localeManager.get("dialog.deleteWorld.title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            try {
                FileUtils.deleteDirectory(selectedWorld);
                loadWorlds();
            } catch (IOException ex) {
                StyledDialog.showMessage(this, localeManager.get("error.deleteWorldFailed", ex.getMessage()),
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

    private class WorldListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setOpaque(isSelected);
            if (value instanceof File) {
                setText(((File) value).getName());
                setIcon(UIManager.getIcon("FileView.directoryIcon"));
            }
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