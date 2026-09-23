package controller;

import exception.ValidationException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import model.Clinic;
import model.Patient;
import util.IdGenerator;
import util.SearchUtil;
import util.SortUtil;
import util.Validator;

/**
 * Controller layer: validates raw text from the View, builds Model objects,
 * asks the Model to store them, and reports failures back as exceptions.
 * It contains no Swing code, so the same controller could drive a console UI.
 */
public class PatientController {

    private final Clinic clinic;

    public PatientController(Clinic clinic) {
        this.clinic = clinic;
    }

    public Patient registerPatient(String fullName, String dob, String phone, String email,
                                   String medicare, String address, String allergies)
            throws ValidationException, IOException {

        String validName = Validator.requireName("Full name", fullName);
        LocalDate validDob = Validator.requirePastDate("Date of birth", dob);
        String validPhone = Validator.requirePhone(phone);
        String validEmail = Validator.requireEmail(email);
        String validMedicare = Validator.requireMedicare(medicare);
        String validAddress = Validator.requireText("Address", address);

        for (Patient existing : clinic.getPatients().findAll()) {
            if (existing.getMedicareNumber().equals(validMedicare)) {
                throw new ValidationException("Medicare number",
                        "Patient " + existing.getId() + " already uses that Medicare number.");
            }
        }

        String id = IdGenerator.next("P", clinic.getPatients().findAll());
        Patient patient = new Patient(id, validName, validDob, validPhone, validEmail,
                validMedicare, validAddress, allergies);
        clinic.getPatients().add(patient);
        clinic.getPatients().save();
        return patient;
    }

    public void updatePatient(String id, String phone, String email, String address,
                              String allergies) throws ValidationException, IOException {
        Patient patient = clinic.getPatients().findById(id)
                .orElseThrow(() -> new ValidationException("Patient " + id + " was not found."));
        patient.setPhone(Validator.requirePhone(phone));
        patient.setEmail(Validator.requireEmail(email));
        patient.setAddress(Validator.requireText("Address", address));
        patient.setAllergies(allergies == null || allergies.isBlank() ? "None" : allergies.trim());
        clinic.getPatients().update(patient);
        clinic.getPatients().save();
    }

    public void deletePatient(String id) throws ValidationException, IOException {
        boolean hasAppointments = clinic.getAppointments().findAll().stream()
                .anyMatch(a -> a.getPatient().getId().equals(id));
        if (hasAppointments) {
            throw new ValidationException("Cannot delete patient " + id
                    + " because appointments still reference this record.");
        }
        if (!clinic.getPatients().deleteById(id)) {
            throw new ValidationException("Patient " + id + " was not found.");
        }
        clinic.getPatients().save();
    }

    /** Linear Search across id, name, phone and Medicare number. */
    public List<Patient> search(String keyword) {
        String needle = keyword == null ? "" : keyword.trim().toLowerCase();
        if (needle.isEmpty()) {
            return clinic.getPatients().findAll();
        }
        return SearchUtil.linearSearch(clinic.getPatients().findAll(), p ->
                p.getId().toLowerCase().contains(needle)
                        || p.getFullName().toLowerCase().contains(needle)
                        || p.getPhone().contains(needle)
                        || p.getMedicareNumber().contains(needle));
    }

    /** Binary Search on the exact patient id (list is sorted inside findByKey). */
    public Patient findById(String id) {
        return SearchUtil.findByKey(clinic.getPatients().findAll(), Patient::getId, id);
    }

    /** Bubble Sort by surname-insensitive full name. */
    public List<Patient> sortedByName() {
        List<Patient> list = clinic.getPatients().findAll();
        SortUtil.bubbleSort(list, Comparator.comparing(p -> p.getFullName().toLowerCase()));
        return list;
    }

    /** Quick Sort by age, oldest first. */
    public List<Patient> sortedByAgeDescending() {
        List<Patient> list = clinic.getPatients().findAll();
        SortUtil.quickSort(list, Comparator.comparingInt(Patient::getAge).reversed());
        return list;
    }

    public List<Patient> findAll() {
        return clinic.getPatients().findAll();
    }
}
