package net.eqozqq.nostalgialauncherdesktop;

import javax.swing.*;
import java.awt.*;

public class VersionComboBoxRenderer extends DefaultListCellRenderer {
    private VersionManager versionManager;
    private Color defaultBackground;
    private Color installedBackground;

    public VersionComboBoxRenderer(VersionManager versionManager) {
        this.versionManager = versionManager;
        this.defaultBackground = UIManager.getColor("List.background");
        this.installedBackground = new Color(200, 200, 200);
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof Version) {
            Version version = (Version) value;
            if (versionManager.isVersionInstalled(version)) {
                if (isSelected) {
                    setBackground(list.getSelectionBackground().darker());
                } else {
                    setBackground(installedBackground);
                }
            } else {
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                } else {
                    setBackground(defaultBackground);
                }
            }
        }
        return this;
    }
}