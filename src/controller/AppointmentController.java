package controller;

import exception.ValidationException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import model.Appointment;
import model.AppointmentStatus;
import model.Clinic;
import model.Doctor;
import model.Patient;
import util.IdGenerator;
import util.SearchUtil;
import util.SortUtil;
import util.Validator;

public class AppointmentController {

    private final Clinic clinic;

    public AppointmentController(Clinic clinic) {
        this.clinic = clinic;
    }

    public Appointment bookAppointment(String patientId, String doctorId, String date,
                                       String time, String reason)
            throws ValidationException, IOException {

        Patient patient = clinic.getPatients().findById(patientId)
                .orElseThrow(() -> new ValidationException("Patient", "Select a patient first."));
        Doctor doctor = clinic.getDoctors().findById(doctorId)
                .orElseThrow(() -> new ValidationException("Doctor", "Select a doctor first."));

        LocalDateTime when = Validator.requireFutureDateTime(date, time);
        String validReason = Validator.requireText("Reason", reason);

        String id = IdGenerator.next("A", clinic.getAppointments().findAll());
        Appointment appointment = new Appointment(id, patient, doctor, when, validReason);

        clinic.bookAppointment(appointment);   // clinic rules + repository add
        clinic.getAppointments().save();
        return appointment;
    }

    public void cancelAppointment(String id) throws ValidationException, IOException {
        Appointment appointment = clinic.getAppointments().findById(id)
                .orElseThrow(() -> new ValidationException("Appointment " + id + " was not found."));
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ValidationException("Appointment " + id + " is already cancelled.");
        }
        appointment.cancel();
        clinic.getAppointments().update(appointment);
        clinic.getAppointments().save();
    }

    public void completeAppointment(String id) throws ValidationException, IOException {
        Appointment appointment = clinic.getAppointments().findById(id)
                .orElseThrow(() -> new ValidationException("Appointment " + id + " was not found."));
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ValidationException("A cancelled appointment cannot be completed.");
        }
        appointment.complete();
        clinic.getAppointments().update(appointment);
        clinic.getAppointments().save();
    }

    public void deleteAppointment(String id) throws ValidationException, IOException {
        boolean hasTreatment = clinic.getTreatments().findAll().stream()
                .anyMatch(t -> t.getAppointment().getId().equals(id));
        if (hasTreatment) {
            throw new ValidationException("Cannot delete appointment " + id
                    + " because a treatment record depends on it.");
        }
        if (!clinic.getAppointments().deleteById(id)) {
            throw new ValidationException("Appointment " + id + " was not found.");
        }
        clinic.getAppointments().save();
    }

    /** Linear Search over patient name, doctor name, id and reason. */
    public List<Appointment> search(String keyword) {
        String needle = keyword == null ? "" : keyword.trim().toLowerCase();
        if (needle.isEmpty()) {
            return clinic.getAppointments().findAll();
        }
        return SearchUtil.linearSearch(clinic.getAppointments().findAll(), a ->
                a.getId().toLowerCase().contains(needle)
                        || a.getPatient().getFullName().toLowerCase().contains(needle)
                        || a.getDoctor().getFullName().toLowerCase().contains(needle)
                        || a.getReason().toLowerCase().contains(needle)
                        || a.getStatus().name().toLowerCase().contains(needle));
    }

    /** Binary Search on the exact appointment id. */
    public Appointment findById(String id) {
        return SearchUtil.findByKey(clinic.getAppointments().findAll(), Appointment::getId, id);
    }

    public List<Appointment> sortedByDateTime() {
        List<Appointment> list = clinic.getAppointments().findAll();
        SortUtil.quickSort(list, Comparator.comparing(Appointment::getDateTime));
        return list;
    }

    public List<Appointment> sortedByPatientName() {
        List<Appointment> list = clinic.getAppointments().findAll();
        SortUtil.bubbleSort(list,
                Comparator.comparing(a -> a.getPatient().getFullName().toLowerCase()));
        return list;
    }

    public List<Appointment> forDate(LocalDate date) {
        return SearchUtil.linearSearch(clinic.getAppointments().findAll(),
                a -> a.getDateTime().toLocalDate().equals(date));
    }

    public List<Appointment> findAll() {
        return clinic.getAppointments().findAll();
    }
}
