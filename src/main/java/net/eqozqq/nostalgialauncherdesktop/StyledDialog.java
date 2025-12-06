package net.eqozqq.nostalgialauncherdesktop;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.InputStream;

public class StyledDialog {
    private static boolean isDark = true;
    private static final int CORNER_RADIUS = 15;

    public static void setDarkMode(boolean dark) {
        isDark = dark;
    }

    public static boolean isDarkMode() {
        return isDark;
    }

    public static void showMessage(Component parent, String message, String title, int messageType) {
        showDialog(parent, message, title, messageType, JOptionPane.DEFAULT_OPTION, null, null, null);
    }

    public static int showConfirm(Component parent, String message, String title, int optionType) {
        return showConfirm(parent, message, title, optionType, JOptionPane.QUESTION_MESSAGE);
    }

    public static int showConfirm(Component parent, String message, String title, int optionType, int messageType) {
        Object result = showDialog(parent, message, title, messageType, optionType, null, null, null);
        if (result == null) {
            return JOptionPane.CLOSED_OPTION;
        }
        if (result instanceof Integer) {
            return (Integer) result;
        }
        return JOptionPane.CLOSED_OPTION;
    }

    public static String showInput(Component parent, String message, String title, int messageType) {
        Object result = showDialog(parent, message, title, messageType, JOptionPane.OK_CANCEL_OPTION, null, null, null);
        return result instanceof String ? (String) result : null;
    }

    public static String showInput(Component parent, String message, String title, int messageType,
            String initialValue) {
        Object result = showDialog(parent, message, title, messageType, JOptionPane.OK_CANCEL_OPTION, null, null,
                initialValue);
        return result instanceof String ? (String) result : null;
    }

    private static Object showDialog(Component parent, Object message, String title, int messageType,
            int optionType, Icon icon, Object[] options, Object initialValue) {
        JPanel styledPanel = createStyledPanel();
        styledPanel.setLayout(new BorderLayout(10, 10));

        Icon dialogIcon = icon;
        if (dialogIcon == null) {
            dialogIcon = getIconForType(messageType);
        }
        if (dialogIcon != null) {
            JLabel iconLabel = new JLabel(dialogIcon);
            iconLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
            styledPanel.add(iconLabel, BorderLayout.WEST);
        }

        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        if (message instanceof String) {
            String messageStr = (String) message;
            String[] lines = messageStr.split("\n");
            for (String line : lines) {
                JLabel label = new JLabel(line);
                label.setForeground(isDark ? Color.WHITE : Color.BLACK);
                label.setFont(getFont(Font.PLAIN, 13f));
                label.setAlignmentX(Component.LEFT_ALIGNMENT);
                contentPanel.add(label);
            }
        } else if (message instanceof Component) {
            contentPanel.add((Component) message);
        }

        JTextField inputField = null;
        if (optionType == JOptionPane.OK_CANCEL_OPTION && messageType == JOptionPane.QUESTION_MESSAGE) {
            inputField = new JTextField(20);
            if (initialValue != null) {
                inputField.setText(initialValue.toString());
            }
            inputField.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(Box.createVerticalStrut(10));
            contentPanel.add(inputField);
        }

        styledPanel.add(contentPanel, BorderLayout.CENTER);

        JOptionPane optionPane = new JOptionPane(styledPanel, JOptionPane.PLAIN_MESSAGE, optionType, null, options,
                null);

        JDialog dialog = optionPane.createDialog(parent, title);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.getRootPane().setOpaque(false);

        Container contentPane = dialog.getContentPane();
        if (contentPane instanceof JComponent) {
            ((JComponent) contentPane).setOpaque(false);
        }

        dialog.setVisible(true);

        Object selectedValue = optionPane.getValue();

        if (inputField != null) {
            if (selectedValue == null
                    || (selectedValue instanceof Integer && (Integer) selectedValue != JOptionPane.OK_OPTION)) {
                return null;
            }
            return inputField.getText();
        }

        if (selectedValue == null) {
            return JOptionPane.CLOSED_OPTION;
        }
        if (options == null) {
            if (selectedValue instanceof Integer) {
                return selectedValue;
            }
            return JOptionPane.CLOSED_OPTION;
        }
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(selectedValue)) {
                return i;
            }
        }
        return JOptionPane.CLOSED_OPTION;
    }

    private static JPanel createStyledPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isDark) {
                    g2d.setColor(new Color(40, 40, 40, 230));
                } else {
                    g2d.setColor(new Color(250, 250, 250, 230));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        return panel;
    }

    private static Icon getIconForType(int messageType) {
        switch (messageType) {
            case JOptionPane.ERROR_MESSAGE:
                return UIManager.getIcon("OptionPane.errorIcon");
            case JOptionPane.INFORMATION_MESSAGE:
                return UIManager.getIcon("OptionPane.informationIcon");
            case JOptionPane.WARNING_MESSAGE:
                return UIManager.getIcon("OptionPane.warningIcon");
            case JOptionPane.QUESTION_MESSAGE:
                return UIManager.getIcon("OptionPane.questionIcon");
            default:
                return null;
        }
    }

    private static Font getFont(int style, float size) {
        try (InputStream fontStream = StyledDialog.class.getResourceAsStream("/MPLUS1p-Regular.ttf")) {
            if (fontStream != null) {
                return Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(style, size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Font("SansSerif", style, (int) size);
    }

    public static void installDefaults() {
        Color bgColor = isDark ? new Color(45, 45, 45) : new Color(245, 245, 245);
        Color fgColor = isDark ? Color.WHITE : Color.BLACK;

        UIManager.put("OptionPane.background", bgColor);
        UIManager.put("Panel.background", bgColor);
        UIManager.put("OptionPane.messageForeground", fgColor);
        UIManager.put("OptionPane.messageFont", getFont(Font.PLAIN, 13f));
        UIManager.put("OptionPane.buttonFont", getFont(Font.BOLD, 12f));
    }
}
