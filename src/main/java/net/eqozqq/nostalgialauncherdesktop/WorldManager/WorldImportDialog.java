package net.eqozqq.nostalgialauncherdesktop.WorldManager;

import net.eqozqq.nostalgialauncherdesktop.Instances.InstanceManager;
import net.eqozqq.nostalgialauncherdesktop.LocaleManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class WorldImportDialog extends JDialog {

    private final LocaleManager localeManager;
    private JTextField fileField;
    private JComboBox<String> instanceComboBox;
    private JButton importButton;
    private File selectedArchive;
    private boolean success = false;

    public WorldImportDialog(Frame owner, LocaleManager localeManager) {
        super(owner, localeManager.get("dialog.importWorld.title", "Import World"), true);
        this.localeManager = localeManager;

        setLayout(new BorderLayout());
        setResizable(false);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        content.add(new JLabel(localeManager.get("label.selectArchive", "Select Archive:")), gbc);

        fileField = new JTextField(20);
        fileField.setEditable(false);
        gbc.gridx = 1;
        content.add(fileField, gbc);

        JButton browseButton = new JButton(localeManager.get("button.browse", "Browse"));
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Archives (zip, tar, 7z)", "zip", "tar", "gz", "tgz", "7z"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedArchive = chooser.getSelectedFile();
                fileField.setText(selectedArchive.getName());
            }
        });
        gbc.gridx = 2;
        content.add(browseButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
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
        if (selectedArchive == null || !selectedArchive.exists()) {
            JOptionPane.showMessageDialog(this, localeManager.get("error.noFileSelected", "Please select an archive."),
                    localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedInstanceName = (String) instanceComboBox.getSelectedItem();
        InstanceManager instanceManager = InstanceManager.getInstance();

        String savedInstance = instanceManager.getActiveInstance();
        instanceManager.setActiveInstance(selectedInstanceName);

        File targetWorldsDir = new File(instanceManager.resolvePath("game/storage/games/com.mojang/minecraftWorlds/"));
        if (!targetWorldsDir.exists())
            targetWorldsDir.mkdirs();

        try {
            WorldImporter.importWorld(selectedArchive, targetWorldsDir);
            success = true;
            JOptionPane.showMessageDialog(this, localeManager.get("info.importSuccess"),
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