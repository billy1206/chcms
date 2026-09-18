package view;

import controller.PatientController;
import exception.ValidationException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import model.Patient;
import javax.swing.table.DefaultTableModel;

/** View: Register Patient screen. It only collects text and displays results. */
public class PatientPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final PatientController controller;

    private final JTextField nameField = new JTextField();
    private final JTextField dobField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField medicareField = new JTextField();
    private final JTextField addressField = new JTextField();
    private final JTextField allergiesField = new JTextField();
    private final JTextField searchField = new JTextField(18);

    private final DefaultTableModel tableModel = UIHelper.readOnlyModel(
            new String[]{"ID", "Full name", "Age", "Phone", "Email", "Medicare", "Allergies"});
    private final JTable table = UIHelper.table(tableModel);

    public PatientPanel(PatientController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        add(UIHelper.title("Patient Registration"), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.WEST);
        add(buildTableArea(), BorderLayout.CENTER);
        refreshTable(controller.findAll());
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("New patient details"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {"Full name *", "Date of birth (dd/MM/yyyy) *", "Phone *",
                "Email *", "Medicare number (10 digits) *", "Address *", "Allergies"};
        JTextField[] fields = {nameField, dobField, phoneField, emailField,
                medicareField, addressField, allergiesField};

        for (int i = 0; i < labels.length; i++) {
            c.gridx = 0; c.gridy = i; c.weightx = 0;
            JLabel label = new JLabel(labels[i]);
            label.setFont(UIHelper.LABEL_FONT);
            form.add(label, c);
            c.gridx = 1; c.weightx = 1;
            fields[i].setPreferredSize(UIHelper.field());
            form.add(fields[i], c);
        }

        JButton register = UIHelper.button("Register patient");
        JButton clear = UIHelper.button("Clear");
        register.addActionListener(e -> onRegister());
        clear.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(register);
        buttons.add(clear);

        c.gridx = 0; c.gridy = labels.length; c.gridwidth = 2;
        form.add(buttons, c);
        return form;
    }

    private JPanel buildTableArea() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JButton search = UIHelper.button("Search");
        JButton showAll = UIHelper.button("Show all");
        JButton sortName = UIHelper.button("Sort by name (Bubble)");
        JButton sortAge = UIHelper.button("Sort by age (Quick)");
        JButton delete = UIHelper.button("Delete selected");

        search.addActionListener(e -> refreshTable(controller.search(searchField.getText())));
        showAll.addActionListener(e -> { searchField.setText(""); refreshTable(controller.findAll()); });
        sortName.addActionListener(e -> refreshTable(controller.sortedByName()));
        sortAge.addActionListener(e -> refreshTable(controller.sortedByAgeDescending()));
        delete.addActionListener(e -> onDelete());

        JPanel bar = UIHelper.toolbar(
                new java.awt.Component[]{new JLabel("Search:"), searchField, search, showAll},
                new java.awt.Component[]{sortName, sortAge, delete});

        panel.add(bar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void onRegister() {
        try {
            Patient p = controller.registerPatient(nameField.getText(), dobField.getText(),
                    phoneField.getText(), emailField.getText(), medicareField.getText(),
                    addressField.getText(), allergiesField.getText());
            UIHelper.info(this, "Patient registered with ID " + p.getId() + ".");
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
            UIHelper.error(this, "Select a patient row first.");
            return;
        }
        String id = tableModel.getValueAt(row, 0).toString();
        if (!UIHelper.confirm(this, "Delete patient " + id + "?")) {
            return;
        }
        try {
            controller.deletePatient(id);
            refreshTable(controller.findAll());
        } catch (ValidationException ex) {
            UIHelper.error(this, ex.getMessage());
        } catch (IOException ex) {
            UIHelper.error(this, "Could not write to the data file: " + ex.getMessage());
        }
    }

    private void clearForm() {
        nameField.setText(""); dobField.setText(""); phoneField.setText("");
        emailField.setText(""); medicareField.setText(""); addressField.setText("");
        allergiesField.setText("");
    }

    public void refreshTable(List<Patient> patients) {
        tableModel.setRowCount(0);
        for (Patient p : patients) {
            tableModel.addRow(new Object[]{p.getId(), p.getFullName(), p.getAge(),
                    p.getPhone(), p.getEmail(), p.getMedicareNumber(), p.getAllergies()});
        }
    }

    public void reload() {
        refreshTable(controller.findAll());
    }
}
