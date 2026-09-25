package view;

import controller.AppointmentController;
import controller.TreatmentController;
import exception.ValidationException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import model.Appointment;
import model.AppointmentStatus;
import model.Treatment;

public class TreatmentPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final TreatmentController treatmentController;
    private final AppointmentController appointmentController;

    private final JComboBox<Appointment> appointmentCombo = new JComboBox<>();
    private final JTextField descriptionField = new JTextField();
    private final JTextField medicationField = new JTextField();
    private final JTextField costField = new JTextField();
    private final JTextField searchField = new JTextField(18);

    private final DefaultTableModel tableModel = UIHelper.readOnlyModel(
            new String[]{"ID", "Date", "Patient", "Doctor", "Description", "Medication", "Total charge"});
    private final JTable table = UIHelper.table(tableModel);

    public TreatmentPanel(TreatmentController treatmentController,
                          AppointmentController appointmentController) {
        this.treatmentController = treatmentController;
        this.appointmentController = appointmentController;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        add(UIHelper.title("Treatment Entry"), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.WEST);
        add(buildTableArea(), BorderLayout.CENTER);
        reload();
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Record a treatment"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {"Appointment *", "Description *", "Medication", "Cost ($) *"};
        java.awt.Component[] inputs = {appointmentCombo, descriptionField, medicationField, costField};

        for (int i = 0; i < labels.length; i++) {
            c.gridx = 0; c.gridy = i; c.weightx = 0; c.gridwidth = 1;
            JLabel label = new JLabel(labels[i]);
            label.setFont(UIHelper.LABEL_FONT);
            form.add(label, c);
            c.gridx = 1; c.weightx = 1;
            inputs[i].setPreferredSize(UIHelper.field());
            form.add(inputs[i], c);
        }

        JButton save = UIHelper.button("Save treatment");
        save.addActionListener(e -> onSave());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(save);

        c.gridx = 0; c.gridy = labels.length; c.gridwidth = 2;
        form.add(buttons, c);

        JLabel hint = new JLabel("<html><i>Saving a treatment marks the<br>"
                + "appointment as COMPLETED.</i></html>");
        hint.setFont(UIHelper.LABEL_FONT);
        c.gridy = labels.length + 1;
        form.add(hint, c);
        return form;
    }

    private JPanel buildTableArea() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JButton search = UIHelper.button("Search");
        JButton showAll = UIHelper.button("Show all");
        JButton sortCost = UIHelper.button("Sort by cost (Quick)");
        JButton delete = UIHelper.button("Delete selected");

        search.addActionListener(e -> refreshTable(treatmentController.search(searchField.getText())));
        showAll.addActionListener(e -> { searchField.setText(""); refreshTable(treatmentController.findAll()); });
        sortCost.addActionListener(e -> refreshTable(treatmentController.sortedByCostDescending()));
        delete.addActionListener(e -> onDelete());

        JPanel bar = UIHelper.toolbar(
                new java.awt.Component[]{new JLabel("Search:"), searchField, search, showAll},
                new java.awt.Component[]{sortCost, delete});

        panel.add(bar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void onSave() {
        Appointment appointment = (Appointment) appointmentCombo.getSelectedItem();
        if (appointment == null) {
            UIHelper.error(this, "Book an appointment before recording a treatment.");
            return;
        }
        try {
            Treatment t = treatmentController.recordTreatment(appointment.getId(),
                    descriptionField.getText(), medicationField.getText(), costField.getText());
            UIHelper.info(this, "Treatment " + t.getId() + " saved. Total charge $"
                    + String.format("%.2f", t.getTotalCharge()) + ".");
            descriptionField.setText(""); medicationField.setText(""); costField.setText("");
            reload();
        } catch (ValidationException ex) {
            UIHelper.error(this, ex.getMessage());
        } catch (IOException ex) {
            UIHelper.error(this, "Could not write to the data file: " + ex.getMessage());
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row == -1) {
            UIHelper.error(this, "Select a treatment row first.");
            return;
        }
        String id = tableModel.getValueAt(row, 0).toString();
        if (!UIHelper.confirm(this, "Delete treatment " + id + "?")) return;
        try {
            treatmentController.deleteTreatment(id);
            reload();
        } catch (ValidationException ex) {
            UIHelper.error(this, ex.getMessage());
        } catch (IOException ex) {
            UIHelper.error(this, "Could not write to the data file: " + ex.getMessage());
        }
    }

    public void refreshTable(List<Treatment> treatments) {
        tableModel.setRowCount(0);
        for (Treatment t : treatments) {
            tableModel.addRow(new Object[]{t.getId(), t.getTreatmentDate(),
                    t.getAppointment().getPatient().getFullName(),
                    t.getAppointment().getDoctor().getFullName(),
                    t.getDescription(), t.getMedication(),
                    String.format("$%.2f", t.getTotalCharge())});
        }
    }

    public void reload() {
        List<Appointment> bookable = appointmentController.findAll();
        bookable.removeIf(a -> a.getStatus() == AppointmentStatus.CANCELLED);
        appointmentCombo.setModel(new DefaultComboBoxModel<>(bookable.toArray(new Appointment[0])));
        refreshTable(treatmentController.findAll());
    }
}
