package net.eqozqq.nostalgialauncherdesktop.WorldManager;

import javax.swing.*;
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
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.spout.nbt.CompoundTag;
import org.spout.nbt.Tag;
import org.spout.nbt.stream.NBTInputStream;
import org.spout.nbt.stream.NBTOutputStream;
import org.spout.nbt.StringTag;

public class WorldsManagerDialog extends JDialog {
    private static final String WORLDS_PATH = "game/storage/games/com.mojang/minecraftWorlds/";
    private static final String BACKUPS_PATH = "backups/";
    private DefaultListModel<File> listModel;
    private JList<File> worldsList;
    private JPanel infoPanel;
    private Level currentLevel;
    private File currentWorldFolder;

    private JLabel lastPlayedLabel;
    private JTextField worldNameField, folderNameField, seedField, timeField, dayCycleLockField;
    private JLabel playerLocationLabel;
    private JComboBox<String> gamemodeComboBox;
    private JCheckBox allowFlyingBox, flyingBox, invulnerableBox, instaBuildBox, spawnMobsBox;

    public WorldsManagerDialog(JFrame parent) {
        super(parent, "Worlds Manager", true);
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        worldsList = new JList<>(listModel);
        worldsList.setCellRenderer(new WorldListRenderer());
        worldsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScrollPane = new JScrollPane(worldsList);
        listScrollPane.setMinimumSize(new Dimension(200, 0));

        infoPanel = createWorldInfoPanel();
        infoPanel.setVisible(false);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, infoPanel);
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);

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
        
        loadWorlds();
    }

    private JPanel createWorldInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        int y = 0;

        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Last Played:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; lastPlayedLabel = new JLabel(); panel.add(lastPlayedLabel, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1; panel.add(new JLabel("World Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; worldNameField = new JTextField(); panel.add(worldNameField, gbc);
        y++;
        
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1; panel.add(new JLabel("Folder Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; folderNameField = new JTextField(); folderNameField.setEditable(false); panel.add(folderNameField, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Seed:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; seedField = new JTextField(); seedField.setEditable(false); panel.add(seedField, gbc);
        gbc.gridx = 2; gbc.weightx = 0.0; JButton copySeedButton = new JButton("Copy"); panel.add(copySeedButton, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Gamemode:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gamemodeComboBox = new JComboBox<>(new String[]{"Survival", "Creative"}); panel.add(gamemodeComboBox, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1; panel.add(new JLabel("Time:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; timeField = new JTextField(); panel.add(timeField, gbc);
        y++;

        gbc.gridx = 1; gbc.gridy = y;
        JPanel timeButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton morningButton = new JButton("Set to Morning");
        JButton nightButton = new JButton("Set to Night");
        timeButtonsPanel.add(morningButton);
        timeButtonsPanel.add(nightButton);
        panel.add(timeButtonsPanel, gbc);
        y++;
        
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Player Location:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; playerLocationLabel = new JLabel(); panel.add(playerLocationLabel, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1; panel.add(new JLabel("Lock Day Cycle (-1 to unlock):"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; dayCycleLockField = new JTextField(); panel.add(dayCycleLockField, gbc);
        y++;

        allowFlyingBox = new JCheckBox("Allow flying");
        flyingBox = new JCheckBox("Currently flying");
        invulnerableBox = new JCheckBox("Invulnerable mode");
        instaBuildBox = new JCheckBox("Instantly break blocks");
        spawnMobsBox = new JCheckBox("Allow mob spawning");
        
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 3; panel.add(allowFlyingBox, gbc); y++;
        gbc.gridy = y; panel.add(flyingBox, gbc); y++;
        gbc.gridy = y; panel.add(invulnerableBox, gbc); y++;
        gbc.gridy = y; panel.add(instaBuildBox, gbc); y++;
        gbc.gridy = y; panel.add(spawnMobsBox, gbc); y++;
        
        gbc.gridy = y; gbc.gridwidth = 3; gbc.anchor = GridBagConstraints.CENTER;
        JButton saveButton = new JButton("Save Changes");
        panel.add(saveButton, gbc);
        
        copySeedButton.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(seedField.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(this, "Seed copied to clipboard.", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        morningButton.addActionListener(e -> timeField.setText("0"));
        nightButton.addActionListener(e -> timeField.setText("12000"));

        saveButton.addActionListener(e -> saveWorldInfo());

        gbc.gridy = y + 1;
        gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);

        return panel;
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
            JOptionPane.showMessageDialog(this, "Failed to read world data.", "Error", JOptionPane.ERROR_MESSAGE);
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
        if (currentLevel == null || currentWorldFolder == null) return;

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
            JOptionPane.showMessageDialog(this, "World information saved.", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format in one of the fields.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save world data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void loadWorlds() {
        listModel.clear();
        File worldsDir = new File(WORLDS_PATH);
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
        JMenuItem renameFolderItem = new JMenuItem("Rename folder");
        JMenuItem backupWorldItem = new JMenuItem("Backup world");
        JMenuItem deleteWorldItem = new JMenuItem("Delete world");

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
        if (selectedWorld == null) return;

        String newName = JOptionPane.showInputDialog(this, "Enter new folder name:", selectedWorld.getName());
        if (newName != null && !newName.trim().isEmpty() && !newName.equals(selectedWorld.getName())) {
            File newFile = new File(selectedWorld.getParent(), newName.trim());
            if (newFile.exists()) {
                JOptionPane.showMessageDialog(this, "A folder with this name already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (selectedWorld.renameTo(newFile)) {
                loadWorlds();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to rename the folder.", "Error", JOptionPane.ERROR_MESSAGE);
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
        if (selectedWorld == null) return;

        File backupsDir = new File(BACKUPS_PATH);
        if (!backupsDir.exists()) {
            backupsDir.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String zipFileName = selectedWorld.getName() + "_" + timeStamp + ".zip";
        File zipFile = new File(backupsDir, zipFileName);

        try {
            zipDirectory(selectedWorld, zipFile);
            JOptionPane.showMessageDialog(this, "World backed up successfully to:\n" + zipFile.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to create backup: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void deleteSelectedWorld() {
        File selectedWorld = worldsList.getSelectedValue();
        if (selectedWorld == null) return;

        int response = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete the world '" + selectedWorld.getName() + "'?\nThis action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            try {
                FileUtils.deleteDirectory(selectedWorld);
                loadWorlds();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to delete the world: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                } catch (IOException e) {
                    System.err.println("Error while zipping file: " + path);
                }
            });
        }
    }

    private static class WorldListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof File) {
                setText(((File) value).getName());
                setIcon(UIManager.getIcon("FileView.directoryIcon"));
            }
            return c;
        }
    }
}