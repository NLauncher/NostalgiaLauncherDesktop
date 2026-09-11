package net.eqozqq.nostalgialauncherdesktop.WorldManager;

import net.eqozqq.nostalgialauncherdesktop.Instances.InstanceManager;
import net.eqozqq.nostalgialauncherdesktop.LocaleManager;
import net.eqozqq.nostalgialauncherdesktop.NativeFileChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorldImportDialog extends JDialog {

    private final LocaleManager localeManager;
    private JRadioButton radioArchive;
    private JRadioButton radioFolder;
    private JLabel sourceLabel;
    private JTextField pathField;
    private JComboBox<String> instanceComboBox;
    private JButton importButton;
    private List<File> selectedArchives = new ArrayList<>();
    private File selectedFolder;
    private boolean success = false;

    public WorldImportDialog(Frame owner, LocaleManager localeManager) {
        super(owner, localeManager.get("dialog.importWorld.title", "Import World"), true);
        this.localeManager = localeManager;

        setLayout(new BorderLayout());
        setResizable(false);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        radioArchive = new JRadioButton(localeManager.get("radio.import.archives", "Archive(s)"), true);
        radioFolder = new JRadioButton(localeManager.get("radio.import.folder", "Folder"), false);
        ButtonGroup group = new ButtonGroup();
        group.add(radioArchive);
        group.add(radioFolder);
        radioPanel.add(radioArchive);
        radioPanel.add(radioFolder);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        content.add(radioPanel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        sourceLabel = new JLabel(localeManager.get("label.selectArchive", "Select Archive(s):"));
        content.add(sourceLabel, gbc);

        pathField = new JTextField(20);
        pathField.setEditable(false);
        gbc.gridx = 1;
        content.add(pathField, gbc);

        JButton browseButton = new JButton(localeManager.get("button.browse", "Browse..."));
        browseButton.addActionListener(e -> {
            if (radioArchive.isSelected()) {
                File[] files = NativeFileChooser.chooseFiles(this,
                        localeManager.get("dialog.importWorld.title", "Import World"),
                        new String[]{".zip", ".tar", ".gz", ".tgz", ".7z", ".rar"},
                        "*.zip;*.tar;*.gz;*.tgz;*.7z;*.rar");
                if (files != null && files.length > 0) {
                    selectedArchives = Arrays.asList(files);
                    selectedFolder = null;
                    if (files.length == 1) {
                        pathField.setText(files[0].getName());
                    } else {
                        pathField.setText(localeManager.get("info.selectedFiles", files.length));
                    }
                }
            } else {
                File folder = NativeFileChooser.chooseDirectory(this,
                        localeManager.get("dialog.selectWorldFolder.title", "Select World(s) Folder"));
                if (folder != null) {
                    selectedFolder = folder;
                    selectedArchives.clear();
                    pathField.setText(folder.getAbsolutePath());
                }
            }
        });
        gbc.gridx = 2;
        content.add(browseButton, gbc);

        radioArchive.addActionListener(e -> {
            sourceLabel.setText(localeManager.get("label.selectArchive", "Select Archive(s):"));
            pathField.setText(selectedArchives.isEmpty() ? "" : (selectedArchives.size() == 1 ? selectedArchives.get(0).getName() : localeManager.get("info.selectedFiles", selectedArchives.size())));
        });
        radioFolder.addActionListener(e -> {
            sourceLabel.setText(localeManager.get("label.selectFolder", "Select Folder:"));
            pathField.setText(selectedFolder != null ? selectedFolder.getAbsolutePath() : "");
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        content.add(new JLabel(localeManager.get("label.selectInstance", "Select Instance:")), gbc);

        instanceComboBox = new JComboBox<>(InstanceManager.getInstance().getInstances().toArray(new String[0]));
        instanceComboBox.setSelectedItem(InstanceManager.getInstance().getActiveInstance());
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        content.add(instanceComboBox, gbc);

        importButton = new JButton(localeManager.get("button.import", "Import"));
        importButton.setFont(importButton.getFont().deriveFont(Font.BOLD));
        importButton.addActionListener(e -> handleImport());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(importButton);

        add(content, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private void handleImport() {
        boolean isArchiveMode = radioArchive.isSelected();
        if (isArchiveMode && selectedArchives.isEmpty()) {
            JOptionPane.showMessageDialog(this, localeManager.get("error.noFileSelected", "Please select an archive."),
                    localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!isArchiveMode && (selectedFolder == null || !selectedFolder.exists())) {
            JOptionPane.showMessageDialog(this, localeManager.get("error.noSourceSelected", "Please select archive(s) or a folder."),
                    localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedInstanceName = (String) instanceComboBox.getSelectedItem();
        InstanceManager instanceManager = InstanceManager.getInstance();

        String savedInstance = instanceManager.getActiveInstance();
        instanceManager.setActiveInstance(selectedInstanceName);

        File targetWorldsDir = net.eqozqq.nostalgialauncherdesktop.NostalgiaLauncherDesktop.getInstance().getWorldsDirectory();
        if (!targetWorldsDir.exists())
            targetWorldsDir.mkdirs();

        try {
            int importedCount = 0;
            if (isArchiveMode) {
                importedCount = WorldImporter.importMultiple(selectedArchives, targetWorldsDir);
            } else {
                importedCount = WorldImporter.importFromFolder(selectedFolder, targetWorldsDir);
            }
            success = true;
            String msg = importedCount > 1
                    ? localeManager.get("info.bulkImportSuccess", importedCount)
                    : localeManager.get("info.importSuccess");
            JOptionPane.showMessageDialog(this, msg,
                    localeManager.get("dialog.success.title"), JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, localeManager.get("error.importFailed", e.getMessage()),
                    localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
        } finally {
            instanceManager.setActiveInstance(savedInstance);
        }
    }

    public boolean isSuccess() {
        return success;
    }
}