package controller;

import exception.ValidationException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import model.Appointment;
import model.AppointmentStatus;
import model.Clinic;
import model.Treatment;
import util.IdGenerator;
import util.SearchUtil;
import util.SortUtil;
import util.Validator;

public class TreatmentController {

    private final Clinic clinic;

    public TreatmentController(Clinic clinic) {
        this.clinic = clinic;
    }

    public Treatment recordTreatment(String appointmentId, String description,
                                     String medication, String cost)
            throws ValidationException, IOException {

        Appointment appointment = clinic.getAppointments().findById(appointmentId)
                .orElseThrow(() -> new ValidationException("Appointment",
                        "Select the appointment this treatment belongs to."));
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ValidationException("Appointment",
                    "A treatment cannot be recorded against a cancelled appointment.");
        }

        String validDescription = Validator.requireText("Description", description);
        double validCost = Validator.requirePositiveAmount("Cost", cost);

        String id = IdGenerator.next("T", clinic.getTreatments().findAll());
        Treatment treatment = new Treatment(id, appointment, validDescription,
                medication, validCost, LocalDate.now());

        clinic.getTreatments().add(treatment);
        appointment.complete();
        clinic.getAppointments().update(appointment);
        clinic.getTreatments().save();
        clinic.getAppointments().save();
        return treatment;
    }

    public void deleteTreatment(String id) throws ValidationException, IOException {
        if (!clinic.getTreatments().deleteById(id)) {
            throw new ValidationException("Treatment " + id + " was not found.");
        }
        clinic.getTreatments().save();
    }

    public List<Treatment> search(String keyword) {
        String needle = keyword == null ? "" : keyword.trim().toLowerCase();
        if (needle.isEmpty()) {
            return clinic.getTreatments().findAll();
        }
        return SearchUtil.linearSearch(clinic.getTreatments().findAll(), t ->
                t.getId().toLowerCase().contains(needle)
                        || t.getDescription().toLowerCase().contains(needle)
                        || t.getMedication().toLowerCase().contains(needle)
                        || t.getAppointment().getPatient().getFullName().toLowerCase().contains(needle));
    }

    public List<Treatment> sortedByCostDescending() {
        List<Treatment> list = clinic.getTreatments().findAll();
        SortUtil.quickSort(list, Comparator.comparingDouble(Treatment::getCost).reversed());
        return list;
    }

    public List<Treatment> findAll() {
        return clinic.getTreatments().findAll();
    }
}
