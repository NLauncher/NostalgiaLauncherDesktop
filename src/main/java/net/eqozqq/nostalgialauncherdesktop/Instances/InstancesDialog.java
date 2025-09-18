package net.eqozqq.nostalgialauncherdesktop.Instances;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;
import net.eqozqq.nostalgialauncherdesktop.LocaleManager;

public class InstancesDialog extends JDialog {
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);
    private final LocaleManager locale;
    private final InstanceManager mgr = InstanceManager.getInstance();

    private static final Pattern ENGLISH_NAME = Pattern.compile("^[A-Za-z0-9 _.-]+$");

    public InstancesDialog(JFrame parent, LocaleManager locale) {
        super(parent, locale.get("dialog.instances.title"), true);
        this.locale = locale;
        setSize(400, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton(locale.get("button.instance.add"));
        toolbar.add(addBtn);
        add(toolbar, BorderLayout.NORTH);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(list), BorderLayout.CENTER);
        JButton close = new JButton(locale.get("button.close"));
        close.addActionListener(e -> dispose());
        add(close, BorderLayout.SOUTH);
        addBtn.addActionListener(e -> onAdd());
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = list.locationToIndex(e.getPoint());
                    if (row >= 0) {
                        list.setSelectedIndex(row);
                        showPopup(e);
                    }
                } else if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    String sel = list.getSelectedValue();
                    if (sel != null) {
                        mgr.setActiveInstance(sel);
                        dispose();
                    }
                }
            }
        });
        reload();
        String current = mgr.getActiveInstance();
        for (int i = 0; i < model.size(); i++) {
            if (model.get(i).equals(current)) {
                list.setSelectedIndex(i);
                break;
            }
        }
    }

    private void reload() {
        model.clear();
        model.addElement(InstanceManager.getDefaultInstanceName());
        File root = InstanceManager.getInstancesRoot();
        File[] dirs = root.listFiles(File::isDirectory);
        if (dirs != null) {
            Arrays.sort(dirs, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
            for (File f : dirs) {
                model.addElement(f.getName());
            }
        }
    }

    private void showPopup(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem rename = new JMenuItem(locale.get("menu.rename"));
        JMenuItem delete = new JMenuItem(locale.get("menu.delete"));
        rename.addActionListener(a -> onRename());
        delete.addActionListener(a -> onDelete());
        popup.add(rename);
        popup.add(delete);
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    private void onAdd() {
        String name = JOptionPane.showInputDialog(this, locale.get("dialog.instance.addPrompt"));
        if (name == null) return;
        name = name.trim();
        if (name.isEmpty() || !ENGLISH_NAME.matcher(name).matches()) {
            JOptionPane.showMessageDialog(this, locale.get("error.instance.invalidName"), locale.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (name.equals(InstanceManager.getDefaultInstanceName())) {
            JOptionPane.showMessageDialog(this, locale.get("error.instance.reservedName"), locale.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        File dir = new File(InstanceManager.getInstancesRoot(), name);
        if (dir.exists()) {
            JOptionPane.showMessageDialog(this, locale.get("error.instance.exists"), locale.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        dir.mkdirs();
        reload();
    }

    private void onRename() {
        String sel = list.getSelectedValue();
        if (sel == null || sel.equals(InstanceManager.getDefaultInstanceName())) return;
        String name = JOptionPane.showInputDialog(this, locale.get("dialog.instance.renamePrompt"), sel);
        if (name == null) return;
        name = name.trim();
        if (name.isEmpty() || !ENGLISH_NAME.matcher(name).matches()) {
            JOptionPane.showMessageDialog(this, locale.get("error.instance.invalidName"), locale.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (name.equals(InstanceManager.getDefaultInstanceName())) {
            JOptionPane.showMessageDialog(this, locale.get("error.instance.reservedName"), locale.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        File oldDir = new File(InstanceManager.getInstancesRoot(), sel);
        File newDir = new File(InstanceManager.getInstancesRoot(), name);
        if (newDir.exists()) {
            JOptionPane.showMessageDialog(this, locale.get("error.instance.exists"), locale.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean success = oldDir.renameTo(newDir);
        if (!success) {
            JOptionPane.showMessageDialog(this, locale.get("error.instance.renameFailed"), locale.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (mgr.getActiveInstance().equals(sel)) {
            mgr.setActiveInstance(name);
        }
        reload();
    }

    private void onDelete() {
        String sel = list.getSelectedValue();
        if (sel == null || sel.equals(InstanceManager.getDefaultInstanceName())) return;
        int confirm = JOptionPane.showConfirmDialog(this, locale.get("dialog.instance.deleteWarning"), locale.get("dialog.warning.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        File dir = new File(InstanceManager.getInstancesRoot(), sel);
        try {
            if (dir.exists()) {
                Files.walk(dir.toPath())
                        .sorted(Comparator.reverseOrder())
                        .forEach(p -> p.toFile().delete());
            }
            if (mgr.getActiveInstance().equals(sel)) {
                mgr.setActiveInstance(InstanceManager.getDefaultInstanceName());
            }
            reload();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, locale.get("error.instance.deleteFailed", ex.getMessage()), locale.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
        }
    }
}
