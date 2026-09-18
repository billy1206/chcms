package view;

import controller.DoctorController;
import controller.PatientController;
import controller.ReportController;
import exception.ValidationException;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import model.Appointment;
import model.Doctor;
import model.Patient;

public class ReportsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final ReportController reportController;
    private final PatientController patientController;
    private final DoctorController doctorController;

    private final JComboBox<Doctor> doctorCombo = new JComboBox<>();
    private final JComboBox<Patient> patientCombo = new JComboBox<>();
    private final JTextField dateField = new JTextField(10);
    private final JTextArea output = new JTextArea();

    public ReportsPanel(ReportController reportController,
                        PatientController patientController,
                        DoctorController doctorController) {
        this.reportController = reportController;
        this.patientController = patientController;
        this.doctorController = doctorController;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        add(UIHelper.title("Reports"), BorderLayout.NORTH);
        add(buildControls(), BorderLayout.SOUTH);

        output.setEditable(false);
        output.setFont(new Font("Monospaced", Font.PLAIN, 13));
        add(new JScrollPane(output), BorderLayout.CENTER);
        reload();
    }

    private JPanel buildControls() {
        JButton summary = UIHelper.button("Clinic summary");
        JButton schedule = UIHelper.button("Doctor schedule");
        JButton history = UIHelper.button("Patient history");

        summary.addActionListener(e -> output.setText(reportController.buildSummary()));
        schedule.addActionListener(e -> onSchedule());
        history.addActionListener(e -> onHistory());

        JPanel bar = UIHelper.toolbar(
                new java.awt.Component[]{summary, new JLabel("Doctor:"), doctorCombo,
                        new JLabel("Date (dd/MM/yyyy):"), dateField, schedule},
                new java.awt.Component[]{new JLabel("Patient:"), patientCombo, history});
        bar.setBorder(BorderFactory.createTitledBorder("Generate a report"));
        return bar;
    }

    private void onSchedule() {
        Doctor doctor = (Doctor) doctorCombo.getSelectedItem();
        if (doctor == null) {
            UIHelper.error(this, "Register a doctor first.");
            return;
        }
        try {
            List<Appointment> schedule =
                    reportController.doctorSchedule(doctor.getId(), dateField.getText());
            StringBuilder sb = new StringBuilder();
            sb.append("DOCTOR SCHEDULE - ").append(doctor.getFullName()).append('\n');
            sb.append("Date: ").append(dateField.getText().trim()).append('\n');
            sb.append("=".repeat(46)).append('\n');
            if (schedule.isEmpty()) {
                sb.append("No appointments booked for this day.\n");
            } else {
                for (Appointment a : schedule) {
                    sb.append(a.getDateTime().toLocalTime()).append("  ")
                            .append(a.getPatient().getFullName())
                            .append("  (").append(a.getReason()).append(")  [")
                            .append(a.getStatus()).append("]\n");
                }
                sb.append("\nTotal appointments: ").append(schedule.size()).append('\n');
            }
            output.setText(sb.toString());
        } catch (ValidationException ex) {
            UIHelper.error(this, ex.getMessage());
        }
    }

    private void onHistory() {
        Patient patient = (Patient) patientCombo.getSelectedItem();
        if (patient == null) {
            UIHelper.error(this, "Register a patient first.");
            return;
        }
        try {
            output.setText(reportController.buildPatientHistory(patient.getId()));
        } catch (ValidationException ex) {
            UIHelper.error(this, ex.getMessage());
        }
    }

    public void reload() {
        doctorCombo.setModel(new DefaultComboBoxModel<>(
                doctorController.findAll().toArray(new Doctor[0])));
        patientCombo.setModel(new DefaultComboBoxModel<>(
                patientController.findAll().toArray(new Patient[0])));
    }
}
