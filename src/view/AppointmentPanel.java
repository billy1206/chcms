package view;

import controller.AppointmentController;
import controller.DoctorController;
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
import model.Doctor;
import model.Patient;

public class AppointmentPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final AppointmentController appointmentController;
    private final PatientController patientController;
    private final DoctorController doctorController;

    private final JComboBox<Patient> patientCombo = new JComboBox<>();
    private final JComboBox<Doctor> doctorCombo = new JComboBox<>();
    private final JTextField dateField = new JTextField();
    private final JTextField timeField = new JTextField();
    private final JTextField reasonField = new JTextField();
    private final JTextField searchField = new JTextField(18);

    private final DefaultTableModel tableModel = UIHelper.readOnlyModel(
            new String[]{"ID", "Date & time", "Patient", "Doctor", "Reason", "Status"});
    private final JTable table = UIHelper.table(tableModel);

    public AppointmentPanel(AppointmentController appointmentController,
                            PatientController patientController,
                            DoctorController doctorController) {
        this.appointmentController = appointmentController;
        this.patientController = patientController;
        this.doctorController = doctorController;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        add(UIHelper.title("Appointment Booking"), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.WEST);
        add(buildTableArea(), BorderLayout.CENTER);
        reload();
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Book an appointment"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {"Patient *", "Doctor *", "Date (dd/MM/yyyy) *",
                "Time (HH:mm, 24h) *", "Reason *"};
        java.awt.Component[] inputs = {patientCombo, doctorCombo, dateField, timeField, reasonField};

        for (int i = 0; i < labels.length; i++) {
            c.gridx = 0; c.gridy = i; c.weightx = 0; c.gridwidth = 1;
            JLabel label = new JLabel(labels[i]);
            label.setFont(UIHelper.LABEL_FONT);
            form.add(label, c);
            c.gridx = 1; c.weightx = 1;
            inputs[i].setPreferredSize(UIHelper.field());
            form.add(inputs[i], c);
        }

        JButton book = UIHelper.button("Book appointment");
        book.addActionListener(e -> onBook());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(book);

        c.gridx = 0; c.gridy = labels.length; c.gridwidth = 2;
        form.add(buttons, c);

        JLabel hint = new JLabel("<html><i>Slots last 30 minutes.<br>"
                + "Double bookings are rejected.</i></html>");
        hint.setFont(UIHelper.LABEL_FONT);
        c.gridy = labels.length + 1;
        form.add(hint, c);
        return form;
    }

    private JPanel buildTableArea() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JButton search = UIHelper.button("Search");
        JButton showAll = UIHelper.button("Show all");
        JButton sortDate = UIHelper.button("Sort by date (Quick)");
        JButton sortPatient = UIHelper.button("Sort by patient (Bubble)");
        JButton cancel = UIHelper.button("Cancel selected");
        JButton complete = UIHelper.button("Mark completed");

        search.addActionListener(e -> refreshTable(appointmentController.search(searchField.getText())));
        showAll.addActionListener(e -> { searchField.setText(""); refreshTable(appointmentController.findAll()); });
        sortDate.addActionListener(e -> refreshTable(appointmentController.sortedByDateTime()));
        sortPatient.addActionListener(e -> refreshTable(appointmentController.sortedByPatientName()));
        cancel.addActionListener(e -> changeStatus(true));
        complete.addActionListener(e -> changeStatus(false));

        JPanel bar = UIHelper.toolbar(
                new java.awt.Component[]{new JLabel("Search:"), searchField, search, showAll},
                new java.awt.Component[]{sortDate, sortPatient},
                new java.awt.Component[]{cancel, complete});

        panel.add(bar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void onBook() {
        Patient patient = (Patient) patientCombo.getSelectedItem();
        Doctor doctor = (Doctor) doctorCombo.getSelectedItem();
        if (patient == null || doctor == null) {
            UIHelper.error(this, "Register at least one patient and one doctor first.");
            return;
        }
        try {
            Appointment a = appointmentController.bookAppointment(patient.getId(), doctor.getId(),
                    dateField.getText(), timeField.getText(), reasonField.getText());
            UIHelper.info(this, "Appointment " + a.getId() + " booked for "
                    + a.getFormattedDateTime() + ".");
            dateField.setText(""); timeField.setText(""); reasonField.setText("");
            refreshTable(appointmentController.findAll());
        } catch (ValidationException ex) {
            UIHelper.error(this, ex.getMessage());
        } catch (IOException ex) {
            UIHelper.error(this, "Could not write to the data file: " + ex.getMessage());
        }
    }

    private void changeStatus(boolean cancelIt) {
        int row = table.getSelectedRow();
        if (row == -1) {
            UIHelper.error(this, "Select an appointment row first.");
            return;
        }
        String id = tableModel.getValueAt(row, 0).toString();
        try {
            if (cancelIt) {
                if (!UIHelper.confirm(this, "Cancel appointment " + id + "?")) return;
                appointmentController.cancelAppointment(id);
            } else {
                appointmentController.completeAppointment(id);
            }
            refreshTable(appointmentController.findAll());
        } catch (ValidationException ex) {
            UIHelper.error(this, ex.getMessage());
        } catch (IOException ex) {
            UIHelper.error(this, "Could not write to the data file: " + ex.getMessage());
        }
    }

    public void refreshTable(List<Appointment> appointments) {
        tableModel.setRowCount(0);
        for (Appointment a : appointments) {
            tableModel.addRow(new Object[]{a.getId(), a.getFormattedDateTime(),
                    a.getPatient().getFullName(), a.getDoctor().getFullName(),
                    a.getReason(), a.getStatus()});
        }
    }

    /** Re-reads people from the model so newly registered names appear here. */
    public void reload() {
        patientCombo.setModel(new DefaultComboBoxModel<>(
                patientController.findAll().toArray(new Patient[0])));
        doctorCombo.setModel(new DefaultComboBoxModel<>(
                doctorController.findAll().toArray(new Doctor[0])));
        refreshTable(appointmentController.findAll());
    }
}
