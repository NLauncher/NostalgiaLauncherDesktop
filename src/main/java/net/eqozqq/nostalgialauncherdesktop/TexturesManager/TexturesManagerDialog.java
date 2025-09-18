package net.eqozqq.nostalgialauncherdesktop.TexturesManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import net.eqozqq.nostalgialauncherdesktop.LocaleManager;
import net.eqozqq.nostalgialauncherdesktop.Version;
import net.eqozqq.nostalgialauncherdesktop.VersionManager;
import net.eqozqq.nostalgialauncherdesktop.Instances.InstanceManager;

public class TexturesManagerDialog extends JDialog {

    private JList<String> versionList;
    private DefaultListModel<String> listModel;
    private String selectedVersion;
    private LocaleManager localeManager;

    public TexturesManagerDialog(JFrame parent, LocaleManager localeManager) {
        super(parent, localeManager.get("dialog.texturesManager.title"), true);
        this.localeManager = localeManager;
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new CardLayout());

        add(createVersionSelectionPanel(), "SELECT_VERSION");
    }

    private JPanel createVersionSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel(localeManager.get("label.selectVersionForTexture"), SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        versionList = new JList<>(listModel);
        versionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(versionList), BorderLayout.CENTER);

        JButton selectButton = new JButton(localeManager.get("button.select"));
        selectButton.setPreferredSize(new Dimension(0, 35));
        selectButton.addActionListener(e -> {
            selectedVersion = versionList.getSelectedValue();
            if (selectedVersion != null) {
                getContentPane().add(createTexturePanel(), "TEXTURE_PANEL");
                CardLayout cl = (CardLayout)(getContentPane().getLayout());
                cl.show(getContentPane(), "TEXTURE_PANEL");
                setTitle(localeManager.get("dialog.texturesManager.titleForVersion", selectedVersion));
            } else {
                JOptionPane.showMessageDialog(this, localeManager.get("error.noVersionSelected.textures"), localeManager.get("error.noVersionSelected.title"), JOptionPane.WARNING_MESSAGE);
            }
        });
        panel.add(selectButton, BorderLayout.SOUTH);

        loadInstalledVersions();
        return panel;
    }

    private JPanel createTexturePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JPanel fileSelectionPanel = new JPanel(new BorderLayout(5, 0));
        JTextField archivePathField = new JTextField(30);
        archivePathField.setEditable(false);
        JButton browseButton = new JButton(localeManager.get("button.browse"));
        fileSelectionPanel.add(archivePathField, BorderLayout.CENTER);
        fileSelectionPanel.add(browseButton, BorderLayout.EAST);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(fileSelectionPanel, gbc);

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        JButton unpackButton = new JButton(localeManager.get("button.unpackTextures"));
        JButton restoreButton = new JButton(localeManager.get("button.restoreDefaultTextures"));
        
        Dimension buttonSize = new Dimension(0, 35);
        unpackButton.setPreferredSize(buttonSize);
        restoreButton.setPreferredSize(buttonSize);

        buttonsPanel.add(unpackButton);
        buttonsPanel.add(restoreButton);
        
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonsPanel, gbc);


        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(localeManager.get("dialog.selectTextureArchive.title"));
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(localeManager.get("fileChooser.archives"), "zip", "rar", "tar", "7z"));
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                archivePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        unpackButton.addActionListener(e -> {
            String path = archivePathField.getText();
            if (path.isEmpty()) {
                JOptionPane.showMessageDialog(this, localeManager.get("error.noArchiveSelected.message"), localeManager.get("error.noArchiveSelected.title"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                File archiveFile = new File(path);
                File versionDir = new File(InstanceManager.getInstance().resolvePath("versions/" + selectedVersion));
                ArchiveExtractor.extract(archiveFile, versionDir);
                JOptionPane.showMessageDialog(this, localeManager.get("info.texturesInstalled"), localeManager.get("dialog.success.title"), JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, localeManager.get("error.installTexturesFailed", ex.getMessage()), localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        restoreButton.addActionListener(e -> restoreDefaultTextures());

        return panel;
    }

    private void loadInstalledVersions() {
        File versionsDir = new File(InstanceManager.getInstance().resolvePath("versions"));
        if (versionsDir.exists() && versionsDir.isDirectory()) {
            String[] directories = versionsDir.list((current, name) -> new File(current, name).isDirectory());
            if (directories != null) {
                Arrays.sort(directories);
                for (String dirName : directories) {
                    if (!dirName.equals("_LevelCache") && !dirName.equals("default_textures")) {
                        listModel.addElement(dirName);
                    }
                }
            }
        }
    }
    
    private void restoreDefaultTextures() {
        VersionManager versionManager = new VersionManager();
        try {
            List<Version> allVersions = versionManager.loadVersions("https://raw.githubusercontent.com/NLauncher/components/main/versions.json");
            Version targetVersion = allVersions.stream()
                .filter(v -> v.getName().equals(selectedVersion))
                .findFirst()
                .orElse(null);

            if (targetVersion == null || targetVersion.getUrl().startsWith("file:")) {
                JOptionPane.showMessageDialog(this, localeManager.get("error.restoreOfficialOnly"), localeManager.get("error.cannotRestore.title"), JOptionPane.WARNING_MESSAGE);
                return;
            }

            int choice = JOptionPane.showConfirmDialog(this, localeManager.get("dialog.confirmRestore.message"), localeManager.get("dialog.confirmRestore.title"), JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) return;

            ProgressMonitor progressMonitor = new ProgressMonitor(this, localeManager.get("progress.downloadingVersion", selectedVersion), "", 0, 100);
            progressMonitor.setMillisToPopup(0);

            File apkFile = versionManager.downloadVersion(targetVersion, progress -> {
                progressMonitor.setProgress((int) (progress * 100));
            });
            
            progressMonitor.setNote(localeManager.get("progress.extractingTextures"));
            progressMonitor.setProgress(0);

            ArchiveExtractor.extractDefaultTextures(apkFile, new File(InstanceManager.getInstance().resolvePath("versions/" + selectedVersion)));
            
            progressMonitor.close();
            JOptionPane.showMessageDialog(this, localeManager.get("info.restoreSuccess"), localeManager.get("dialog.success.title"), JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, localeManager.get("error.restoreFailed", ex.getMessage()), localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}