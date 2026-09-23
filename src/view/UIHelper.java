package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Small shared helpers so every screen looks and behaves the same way. */
public final class UIHelper {

    public static final Color PRIMARY = new Color(0, 102, 153);
    public static final Color LIGHT = new Color(245, 248, 250);
    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 20);
    public static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 13);

    private UIHelper() { }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE_FONT);
        label.setForeground(PRIMARY);
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        return label;
    }

    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setFont(LABEL_FONT);
        return b;
    }

    public static DefaultTableModel readOnlyModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static JTable table(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        return table;
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Invalid input", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Please confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static Dimension field() {
        return new Dimension(220, 26);
    }

    /**
     * Builds a toolbar of stacked rows. A single FlowLayout row silently clips
     * buttons when the window is narrow, so each group gets its own row.
     */
    public static javax.swing.JPanel toolbar(Component[]... rows) {
        javax.swing.JPanel bar = new javax.swing.JPanel(new java.awt.GridLayout(rows.length, 1));
        for (Component[] row : rows) {
            javax.swing.JPanel line = new javax.swing.JPanel(
                    new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 3));
            for (Component component : row) {
                line.add(component);
            }
            bar.add(line);
        }
        return bar;
    }
}
