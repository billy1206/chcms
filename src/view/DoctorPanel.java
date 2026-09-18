package view;

import controller.DoctorController;
import exception.ValidationException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import model.Doctor;

public class DoctorPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final DoctorController controller;

    private final JTextField nameField = new JTextField();
    private final JTextField dobField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField specialisationField = new JTextField();
    private final JTextField feeField = new JTextField();
    private final JTextField searchField = new JTextField(18);

    private final DayOfWeek[] weekdays = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY};
    private final JCheckBox[] dayBoxes = new JCheckBox[weekdays.length];

    private final DefaultTableModel tableModel = UIHelper.readOnlyModel(
            new String[]{"ID", "Full name", "Specialisation", "Fee", "Available days", "Phone"});
    private final JTable table = UIHelper.table(tableModel);

    public DoctorPanel(DoctorController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        add(UIHelper.title("Doctor Registration"), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.WEST);
        add(buildTableArea(), BorderLayout.CENTER);
        refreshTable(controller.findAll());
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("New doctor details"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {"Full name *", "Date of birth (dd/MM/yyyy) *", "Phone *",
                "Email *", "Specialisation *", "Consultation fee ($) *"};
        JTextField[] fields = {nameField, dobField, phoneField, emailField,
                specialisationField, feeField};

        for (int i = 0; i < labels.length; i++) {
            c.gridx = 0; c.gridy = i; c.weightx = 0;
            JLabel label = new JLabel(labels[i]);
            label.setFont(UIHelper.LABEL_FONT);
            form.add(label, c);
            c.gridx = 1; c.weightx = 1;
            fields[i].setPreferredSize(UIHelper.field());
            form.add(fields[i], c);
        }

        JPanel days = new JPanel(new GridLayout(2, 3));
        days.setBorder(BorderFactory.createTitledBorder("Available days *"));
        for (int i = 0; i < weekdays.length; i++) {
            String n = weekdays[i].name();
            dayBoxes[i] = new JCheckBox(n.charAt(0) + n.substring(1, 3).toLowerCase());
            days.add(dayBoxes[i]);
        }
        c.gridx = 0; c.gridy = labels.length; c.gridwidth = 2;
        form.add(days, c);

        JButton register = UIHelper.button("Register doctor");
        JButton clear = UIHelper.button("Clear");
        register.addActionListener(e -> onRegister());
        clear.addActionListener(e -> clearForm());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(register);
        buttons.add(clear);
        c.gridy = labels.length + 1;
        form.add(buttons, c);
        return form;
    }

    private JPanel buildTableArea() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JButton search = UIHelper.button("Search");
        JButton showAll = UIHelper.button("Show all");
        JButton sortName = UIHelper.button("Sort by name (Bubble)");
        JButton sortFee = UIHelper.button("Sort by fee (Quick)");
        JButton delete = UIHelper.button("Delete selected");

        search.addActionListener(e -> refreshTable(controller.search(searchField.getText())));
        showAll.addActionListener(e -> { searchField.setText(""); refreshTable(controller.findAll()); });
        sortName.addActionListener(e -> refreshTable(controller.sortedByName()));
        sortFee.addActionListener(e -> refreshTable(controller.sortedByFee()));
        delete.addActionListener(e -> onDelete());

        JPanel bar = UIHelper.toolbar(
                new java.awt.Component[]{new JLabel("Search:"), searchField, search, showAll},
                new java.awt.Component[]{sortName, sortFee, delete});

        panel.add(bar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private Set<DayOfWeek> selectedDays() {
        Set<DayOfWeek> set = EnumSet.noneOf(DayOfWeek.class);
        for (int i = 0; i < dayBoxes.length; i++) {
            if (dayBoxes[i].isSelected()) set.add(weekdays[i]);
        }
        return set;
    }

    private void onRegister() {
        try {
            Doctor d = controller.registerDoctor(nameField.getText(), dobField.getText(),
                    phoneField.getText(), emailField.getText(), specialisationField.getText(),
                    feeField.getText(), selectedDays());
            UIHelper.info(this, "Doctor registered with ID " + d.getId() + ".");
            clearForm();
            refreshTable(controller.findAll());
        } catch (ValidationException ex) {
            UIHelper.error(this, ex.getMessage());
        } catch (IOException ex) {
            UIHelper.error(this, "Could not write to the data file: " + ex.getMessage());
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row == -1) {
            UIHelper.error(this, "Select a doctor row first.");
            return;
        }
        String id = tableModel.getValueAt(row, 0).toString();
        if (!UIHelper.confirm(this, "Delete doctor " + id + "?")) return;
        try {
            controller.deleteDoctor(id);
            refreshTable(controller.findAll());
        } catch (ValidationException ex) {
            UIHelper.error(this, ex.getMessage());
        } catch (IOException ex) {
            UIHelper.error(this, "Could not write to the data file: " + ex.getMessage());
        }
    }

    private void clearForm() {
        nameField.setText(""); dobField.setText(""); phoneField.setText("");
        emailField.setText(""); specialisationField.setText(""); feeField.setText("");
        for (JCheckBox box : dayBoxes) box.setSelected(false);
    }

    public void refreshTable(List<Doctor> doctors) {
        tableModel.setRowCount(0);
        for (Doctor d : doctors) {
            tableModel.addRow(new Object[]{d.getId(), d.getFullName(), d.getSpecialisation(),
                    String.format("$%.2f", d.getConsultationFee()),
                    String.join(", ", d.getAvailableDayNames()), d.getPhone()});
        }
    }

    public void reload() {
        refreshTable(controller.findAll());
    }
}
