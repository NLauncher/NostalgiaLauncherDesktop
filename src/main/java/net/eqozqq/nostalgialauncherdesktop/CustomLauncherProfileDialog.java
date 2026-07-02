package net.eqozqq.nostalgialauncherdesktop;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomLauncherProfileDialog extends JDialog {
    private JTextField nameField;
    private JTextField exePathField;
    private JTextField customWorldsPathField;
    private JTextField customTexturesPathField;
    private JTextField customOptionsPathField;
    private DefaultListModel<String> conditionsModel;
    private JList<String> conditionsList;
    private boolean confirmed = false;
    private final LocaleManager localeManager;

    public CustomLauncherProfileDialog(Window owner, LocaleManager localeManager, CustomLauncherProfile profile) {
        super(owner,
                profile == null ? localeManager.get("dialog.customLauncher.title", "Add Custom Launcher Profile")
                        : localeManager.get("dialog.customLauncher.edit.title", "Edit Custom Launcher Profile"),
                ModalityType.APPLICATION_MODAL);
        this.localeManager = localeManager;
        initialize(profile);
    }

    private void initialize(CustomLauncherProfile profile) {
        setSize(500, 560);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel(localeManager.get("label.profileName", "Profile Name:")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        nameField = new JTextField();
        mainPanel.add(nameField, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel(localeManager.get("label.mainExecutable", "Main Executable:")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel exePanel = new JPanel(new BorderLayout(5, 0));
        exePanel.setOpaque(false);
        exePathField = new JTextField();
        exePanel.add(exePathField, BorderLayout.CENTER);
        JButton browseBtn = new JButton(localeManager.get("button.browse", "Browse..."));
        browseBtn.addActionListener(e -> {
            File file = NativeFileChooser.chooseFile(this,
                    localeManager.get("label.mainExecutable", "Main Executable"),
                    null, null);
            if (file != null) {
                exePathField.setText(file.getAbsolutePath());
            }
        });
        exePanel.add(browseBtn, BorderLayout.EAST);
        mainPanel.add(exePanel, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel(localeManager.get("label.customWorldsFolder", "Worlds Folder:")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel worldsPanel = new JPanel(new BorderLayout(5, 0));
        worldsPanel.setOpaque(false);
        customWorldsPathField = new JTextField();
        worldsPanel.add(customWorldsPathField, BorderLayout.CENTER);
        JButton browseWorldsBtn = new JButton(localeManager.get("button.browse", "Browse..."));
        browseWorldsBtn.addActionListener(e -> {
            File dir = NativeFileChooser.chooseDirectory(this,
                    localeManager.get("dialog.selectWorldsFolder", "Select Worlds Folder"));
            if (dir != null) {
                customWorldsPathField.setText(dir.getAbsolutePath());
            }
        });
        worldsPanel.add(browseWorldsBtn, BorderLayout.EAST);
        mainPanel.add(worldsPanel, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel(localeManager.get("label.customTexturesFolder", "Assets Folder:")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel texturesPanel = new JPanel(new BorderLayout(5, 0));
        texturesPanel.setOpaque(false);
        customTexturesPathField = new JTextField();
        texturesPanel.add(customTexturesPathField, BorderLayout.CENTER);
        JButton browseTexturesBtn = new JButton(localeManager.get("button.browse", "Browse..."));
        browseTexturesBtn.addActionListener(e -> {
            File dir = NativeFileChooser.chooseDirectory(this,
                    localeManager.get("dialog.selectTexturesFolder", "Select Assets Folder"));
            if (dir != null) {
                customTexturesPathField.setText(dir.getAbsolutePath());
            }
        });
        texturesPanel.add(browseTexturesBtn, BorderLayout.EAST);
        mainPanel.add(texturesPanel, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel(localeManager.get("label.customOptionsFile", "options.txt File:")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel optionsPanel = new JPanel(new BorderLayout(5, 0));
        optionsPanel.setOpaque(false);
        customOptionsPathField = new JTextField();
        optionsPanel.add(customOptionsPathField, BorderLayout.CENTER);
        JButton browseOptionsBtn = new JButton(localeManager.get("button.browse", "Browse..."));
        browseOptionsBtn.addActionListener(e -> {
            File file = NativeFileChooser.chooseFile(this,
                    localeManager.get("dialog.selectOptionsFile", "Select options.txt"),
                    new String[] { ".txt" }, "options.txt");
            if (file != null) {
                customOptionsPathField.setText(file.getAbsolutePath());
            }
        });
        optionsPanel.add(browseOptionsBtn, BorderLayout.EAST);
        mainPanel.add(optionsPanel, gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        mainPanel.add(
                new JLabel(localeManager.get("label.launchConditions", "Launch Conditions (required files/dirs):")),
                gbc);

        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        conditionsModel = new DefaultListModel<>();
        conditionsList = new JList<>(conditionsModel);
        JScrollPane scrollPane = new JScrollPane(conditionsList);

        JPanel listPanel = new JPanel(new BorderLayout(5, 0));
        listPanel.setOpaque(false);
        listPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel listButtons = new JPanel(new GridLayout(2, 1, 0, 5));
        listButtons.setOpaque(false);
        JButton addCondBtn = new JButton(localeManager.get("button.addCondition", "Add Condition"));
        addCondBtn.addActionListener(e -> {
            String val = JOptionPane.showInputDialog(this,
                    localeManager.get("dialog.addCondition.message",
                            "Enter path relative to game folder (e.g. assets, openal32.dll):"),
                    localeManager.get("dialog.addCondition.title", "Add Launch Condition"), JOptionPane.PLAIN_MESSAGE);
            if (val != null) {
                val = val.trim();
                if (!val.isEmpty() && !conditionsModel.contains(val)) {
                    conditionsModel.addElement(val);
                }
            }
        });
        JButton removeCondBtn = new JButton(localeManager.get("button.removeCondition", "Remove"));
        removeCondBtn.addActionListener(e -> {
            int idx = conditionsList.getSelectedIndex();
            if (idx >= 0) {
                conditionsModel.remove(idx);
            }
        });
        listButtons.add(addCondBtn);
        listButtons.add(removeCondBtn);
        listPanel.add(listButtons, BorderLayout.EAST);
        mainPanel.add(listPanel, gbc);

        if (profile != null) {
            nameField.setText(profile.getName());
            exePathField.setText(profile.getExecutablePath());
            customWorldsPathField.setText(profile.getCustomWorldsPath() != null ? profile.getCustomWorldsPath() : "");
            customTexturesPathField
                    .setText(profile.getCustomTexturesPath() != null ? profile.getCustomTexturesPath() : "");
            customOptionsPathField
                    .setText(profile.getCustomOptionsPath() != null ? profile.getCustomOptionsPath() : "");
            for (String p : profile.getRequiredPaths()) {
                conditionsModel.addElement(p);
            }
        }

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setBorder(new EmptyBorder(0, 15, 15, 15));
        JButton okBtn = new JButton(localeManager.get("button.save", "Save"));
        okBtn.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        localeManager.get("error.profileNameEmpty", "Profile name cannot be empty."),
                        localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (exePathField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        localeManager.get("error.executablePathEmpty", "Executable path cannot be empty."),
                        localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            confirmed = true;
            dispose();
        });
        JButton cancelBtn = new JButton(localeManager.get("button.cancel", "Cancel"));
        cancelBtn.addActionListener(e -> dispose());
        buttonsPanel.add(okBtn);
        buttonsPanel.add(cancelBtn);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public CustomLauncherProfile getProfile() {
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < conditionsModel.size(); i++) {
            paths.add(conditionsModel.get(i));
        }
        String wPath = customWorldsPathField.getText().trim();
        String tPath = customTexturesPathField.getText().trim();
        String oPath = customOptionsPathField.getText().trim();
        return new CustomLauncherProfile(
                nameField.getText().trim(),
                exePathField.getText().trim(),
                paths,
                wPath.isEmpty() ? null : wPath,
                tPath.isEmpty() ? null : tPath,
                oPath.isEmpty() ? null : oPath);
    }
}