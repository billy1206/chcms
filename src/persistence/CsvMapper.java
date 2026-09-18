package persistence;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import model.Administrator;
import model.Appointment;
import model.AppointmentStatus;
import model.Doctor;
import model.Patient;
import model.Treatment;

/**
 * Single Responsibility: converts model objects to and from one text line.
 * Keeping this out of the model classes means the model never changes when the
 * storage format changes.
 */
public final class CsvMapper {

    public static final String DELIMITER = "|";
    private static final String SPLIT = "\\|";

    private CsvMapper() { }

    private static String clean(String value) {
        if (value == null) return "";
        return value.replace(DELIMITER, "/").replace("\n", " ").replace("\r", " ").trim();
    }

    private static String[] split(String line, int expected) {
        String[] parts = line.split(SPLIT, -1);
        if (parts.length != expected) {
            throw new IllegalArgumentException("expected " + expected + " fields, found " + parts.length);
        }
        return parts;
    }

    // ---------------------------------------------------------------- Patient
    public static String patientToLine(Patient p) {
        return String.join(DELIMITER,
                clean(p.getId()), clean(p.getFullName()), p.getDateOfBirth().toString(),
                clean(p.getPhone()), clean(p.getEmail()), clean(p.getMedicareNumber()),
                clean(p.getAddress()), clean(p.getAllergies()));
    }

    public static Patient patientFromLine(String line) {
        String[] f = split(line, 8);
        return new Patient(f[0], f[1], LocalDate.parse(f[2]), f[3], f[4], f[5], f[6], f[7]);
    }

    // ----------------------------------------------------------------- Doctor
    public static String doctorToLine(Doctor d) {
        StringBuilder days = new StringBuilder();
        for (DayOfWeek day : d.getAvailableDays()) {
            if (days.length() > 0) days.append(';');
            days.append(day.name());
        }
        return String.join(DELIMITER,
                clean(d.getId()), clean(d.getFullName()), d.getDateOfBirth().toString(),
                clean(d.getPhone()), clean(d.getEmail()), clean(d.getSpecialisation()),
                String.valueOf(d.getConsultationFee()), days.toString());
    }

    public static Doctor doctorFromLine(String line) {
        String[] f = split(line, 8);
        Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        if (!f[7].isBlank()) {
            for (String name : f[7].split(";")) {
                days.add(DayOfWeek.valueOf(name.trim()));
            }
        }
        return new Doctor(f[0], f[1], LocalDate.parse(f[2]), f[3], f[4], f[5],
                Double.parseDouble(f[6]), days);
    }

    // ---------------------------------------------------------- Administrator
    public static String adminToLine(Administrator a) {
        return String.join(DELIMITER,
                clean(a.getId()), clean(a.getFullName()), a.getDateOfBirth().toString(),
                clean(a.getPhone()), clean(a.getEmail()), clean(a.getUsername()),
                clean(a.getPassword()));
    }

    public static Administrator adminFromLine(String line) {
        String[] f = split(line, 7);
        return new Administrator(f[0], f[1], LocalDate.parse(f[2]), f[3], f[4], f[5], f[6]);
    }

    // ------------------------------------------------------------ Appointment
    public static String appointmentToLine(Appointment a) {
        return String.join(DELIMITER,
                clean(a.getId()), clean(a.getPatient().getId()), clean(a.getDoctor().getId()),
                a.getDateTime().toString(), clean(a.getReason()), a.getStatus().name());
    }

    public static Appointment appointmentFromLine(String line,
                                                  Repository<Patient> patients,
                                                  Repository<Doctor> doctors) {
        String[] f = split(line, 6);
        Optional<Patient> patient = patients.findById(f[1]);
        Optional<Doctor> doctor = doctors.findById(f[2]);
        if (patient.isEmpty() || doctor.isEmpty()) {
            throw new IllegalStateException("appointment " + f[0] + " references a missing person");
        }
        Appointment appointment = new Appointment(f[0], patient.get(), doctor.get(),
                LocalDateTime.parse(f[3]), f[4]);
        appointment.setStatus(AppointmentStatus.valueOf(f[5]));
        return appointment;
    }

    // -------------------------------------------------------------- Treatment
    public static String treatmentToLine(Treatment t) {
        return String.join(DELIMITER,
                clean(t.getId()), clean(t.getAppointment().getId()), clean(t.getDescription()),
                clean(t.getMedication()), String.valueOf(t.getCost()),
                t.getTreatmentDate().toString());
    }

    public static Treatment treatmentFromLine(String line, Repository<Appointment> appointments) {
        String[] f = split(line, 6);
        Optional<Appointment> appointment = appointments.findById(f[1]);
        if (appointment.isEmpty()) {
            throw new IllegalStateException("treatment " + f[0] + " references a missing appointment");
        }
        return new Treatment(f[0], appointment.get(), f[2], f[3],
                Double.parseDouble(f[4]), LocalDate.parse(f[5]));
    }
}
