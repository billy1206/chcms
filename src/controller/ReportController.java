package controller;

import exception.ValidationException;
import java.time.LocalDate;
import java.util.List;
import model.Appointment;
import model.AppointmentStatus;
import model.Clinic;
import model.Doctor;
import model.Patient;
import model.Treatment;
import util.Validator;

/** Read-only controller that builds the text reports shown on the Reports screen. */
public class ReportController {

    private final Clinic clinic;

    public ReportController(Clinic clinic) {
        this.clinic = clinic;
    }

    public List<Appointment> doctorSchedule(String doctorId, String date)
            throws ValidationException {
        Doctor doctor = clinic.getDoctors().findById(doctorId)
                .orElseThrow(() -> new ValidationException("Doctor", "Select a doctor."));
        LocalDate day = Validator.requireDate("Schedule date", date);
        return clinic.generateDoctorSchedule(doctor, day);
    }

    public String buildSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("CLINIC SUMMARY REPORT\n");
        sb.append("=".repeat(46)).append('\n');
        sb.append("Clinic        : ").append(clinic.getName()).append('\n');
        sb.append("Address       : ").append(clinic.getAddress()).append('\n');
        sb.append("Generated     : ").append(LocalDate.now()).append("\n\n");
        sb.append("Registered patients   : ").append(clinic.getPatients().count()).append('\n');
        sb.append("Registered doctors    : ").append(clinic.getDoctors().count()).append('\n');
        sb.append("Total appointments    : ").append(clinic.getAppointments().count()).append('\n');
        sb.append("  scheduled           : ").append(clinic.countByStatus(AppointmentStatus.SCHEDULED)).append('\n');
        sb.append("  completed           : ").append(clinic.countByStatus(AppointmentStatus.COMPLETED)).append('\n');
        sb.append("  cancelled           : ").append(clinic.countByStatus(AppointmentStatus.CANCELLED)).append('\n');
        sb.append("Treatments recorded   : ").append(clinic.getTreatments().count()).append('\n');
        sb.append(String.format("Total revenue         : $%.2f%n", clinic.getTotalRevenue()));
        return sb.toString();
    }

    public String buildPatientHistory(String patientId) throws ValidationException {
        Patient patient = clinic.getPatients().findById(patientId)
                .orElseThrow(() -> new ValidationException("Patient", "Select a patient."));
        StringBuilder sb = new StringBuilder();
        sb.append("PATIENT HISTORY - ").append(patient.getFullName())
                .append(" (").append(patient.getId()).append(")\n");
        sb.append("=".repeat(46)).append('\n');
        sb.append("Age       : ").append(patient.getAge()).append('\n');
        sb.append("Medicare  : ").append(patient.getMedicareNumber()).append('\n');
        sb.append("Allergies : ").append(patient.getAllergies()).append("\n\n");

        sb.append("Appointments:\n");
        boolean any = false;
        for (Appointment a : clinic.getAppointments().findAll()) {
            if (a.getPatient().getId().equals(patientId)) {
                any = true;
                sb.append("  ").append(a.getFormattedDateTime())
                        .append("  Dr ").append(a.getDoctor().getFullName())
                        .append("  [").append(a.getStatus()).append("]  ")
                        .append(a.getReason()).append('\n');
            }
        }
        if (!any) sb.append("  (none)\n");

        sb.append("\nTreatments:\n");
        any = false;
        double total = 0;
        for (Treatment t : clinic.getTreatments().findAll()) {
            if (t.getAppointment().getPatient().getId().equals(patientId)) {
                any = true;
                total += t.getTotalCharge();
                sb.append("  ").append(t.getTreatmentDate()).append("  ")
                        .append(t.getDescription()).append("  med: ").append(t.getMedication())
                        .append(String.format("  $%.2f%n", t.getTotalCharge()));
            }
        }
        if (!any) sb.append("  (none)\n");
        sb.append(String.format("%nTotal charged: $%.2f%n", total));
        return sb.toString();
    }
}
