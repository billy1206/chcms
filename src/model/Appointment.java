package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Association class linking exactly one Patient to exactly one Doctor
 * at a point in time. Multiplicity: Patient 1 --- 0..* Appointment 0..* --- 1 Doctor.
 */
public class Appointment implements Identifiable {

    public static final int SLOT_MINUTES = 30;
    private static final DateTimeFormatter DISPLAY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String id;
    private Patient patient;
    private Doctor doctor;
    private LocalDateTime dateTime;
    private String reason;
    private AppointmentStatus status;

    public Appointment(String id, Patient patient, Doctor doctor,
                       LocalDateTime dateTime, String reason) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.dateTime = dateTime;
        this.reason = reason;
        this.status = AppointmentStatus.SCHEDULED;
    }

    public LocalDateTime getEndTime() {
        return dateTime.plus(Duration.ofMinutes(SLOT_MINUTES));
    }

    /** True when two appointments overlap in time for the same doctor. */
    public boolean clashesWith(Appointment other) {
        if (other == null || other.status == AppointmentStatus.CANCELLED) return false;
        if (!doctor.getId().equals(other.doctor.getId())) return false;
        return dateTime.isBefore(other.getEndTime()) && other.dateTime.isBefore(getEndTime());
    }

    public void cancel() { this.status = AppointmentStatus.CANCELLED; }
    public void complete() { this.status = AppointmentStatus.COMPLETED; }

    public String getFormattedDateTime() { return dateTime.format(DISPLAY); }

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Appointment)) return false;
        return Objects.equals(id, ((Appointment) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return id + " | " + getFormattedDateTime() + " | " + patient.getFullName()
                + " -> " + doctor.getFullName();
    }
}
