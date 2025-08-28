package net.eqozqq.nostalgialauncherdesktop;

import com.formdev.flatlaf.FlatLaf;
import javax.swing.*;
import java.awt.*;

public class VersionListCellRenderer extends DefaultListCellRenderer {
    private VersionManager versionManager;
    private Color defaultBackground;
    private Color installedBackground;
    private Color installedDarkBackground;
    private Color defaultDarkBackground;

    public VersionListCellRenderer(VersionManager versionManager) {
        this.versionManager = versionManager;
        updateColors();
    }
    
    public void updateColors() {
        defaultBackground = UIManager.getColor("List.background");
        installedBackground = new Color(220, 220, 220);
        installedDarkBackground = new Color(50, 50, 50);
        defaultDarkBackground = new Color(30, 30, 30);
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof Version) {
            Version version = (Version) value;
            if (versionManager.isVersionInstalled(version)) {
                if (FlatLaf.isLafDark()) {
                    if (isSelected) {
                        setBackground(UIManager.getColor("List.selectionBackground"));
                    } else {
                        setBackground(installedDarkBackground);
                    }
                } else {
                    if (isSelected) {
                        setBackground(list.getSelectionBackground().darker());
                    } else {
                        setBackground(installedBackground);
                    }
                }
            } else {
                if (FlatLaf.isLafDark()) {
                    if (isSelected) {
                        setBackground(UIManager.getColor("List.selectionBackground"));
                    } else {
                        setBackground(defaultDarkBackground);
                    }
                } else {
                    if (isSelected) {
                        setBackground(list.getSelectionBackground());
                    } else {
                        setBackground(defaultBackground);
                    }
                }
            }
        }
        return this;
    }
}