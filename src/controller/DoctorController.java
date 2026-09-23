package controller;

import exception.ValidationException;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import model.Clinic;
import model.Doctor;
import util.IdGenerator;
import util.SearchUtil;
import util.SortUtil;
import util.Validator;

public class DoctorController {

    private final Clinic clinic;

    public DoctorController(Clinic clinic) {
        this.clinic = clinic;
    }

    public Doctor registerDoctor(String fullName, String dob, String phone, String email,
                                 String specialisation, String fee, Set<DayOfWeek> availableDays)
            throws ValidationException, IOException {

        String validName = Validator.requireName("Full name", fullName);
        LocalDate validDob = Validator.requirePastDate("Date of birth", dob);
        String validPhone = Validator.requirePhone(phone);
        String validEmail = Validator.requireEmail(email);
        String validSpecialisation = Validator.requireText("Specialisation", specialisation);
        double validFee = Validator.requirePositiveAmount("Consultation fee", fee);

        if (availableDays == null || availableDays.isEmpty()) {
            throw new ValidationException("Available days",
                    "Select at least one working day for the doctor.");
        }
        if (validDob.isAfter(LocalDate.now().minusYears(24))) {
            throw new ValidationException("Date of birth",
                    "A registered doctor must be at least 24 years old.");
        }

        String id = IdGenerator.next("D", clinic.getDoctors().findAll());
        Doctor doctor = new Doctor(id, validName, validDob, validPhone, validEmail,
                validSpecialisation, validFee, availableDays);
        clinic.getDoctors().add(doctor);
        clinic.getDoctors().save();
        return doctor;
    }

    public void deleteDoctor(String id) throws ValidationException, IOException {
        boolean hasAppointments = clinic.getAppointments().findAll().stream()
                .anyMatch(a -> a.getDoctor().getId().equals(id));
        if (hasAppointments) {
            throw new ValidationException("Cannot delete doctor " + id
                    + " because appointments still reference this record.");
        }
        if (!clinic.getDoctors().deleteById(id)) {
            throw new ValidationException("Doctor " + id + " was not found.");
        }
        clinic.getDoctors().save();
    }

    public List<Doctor> search(String keyword) {
        String needle = keyword == null ? "" : keyword.trim().toLowerCase();
        if (needle.isEmpty()) {
            return clinic.getDoctors().findAll();
        }
        return SearchUtil.linearSearch(clinic.getDoctors().findAll(), d ->
                d.getId().toLowerCase().contains(needle)
                        || d.getFullName().toLowerCase().contains(needle)
                        || d.getSpecialisation().toLowerCase().contains(needle));
    }

    public Doctor findById(String id) {
        return SearchUtil.findByKey(clinic.getDoctors().findAll(), Doctor::getId, id);
    }

    public List<Doctor> sortedByFee() {
        List<Doctor> list = clinic.getDoctors().findAll();
        SortUtil.quickSort(list, Comparator.comparingDouble(Doctor::getConsultationFee));
        return list;
    }

    public List<Doctor> sortedByName() {
        List<Doctor> list = clinic.getDoctors().findAll();
        SortUtil.bubbleSort(list, Comparator.comparing(d -> d.getFullName().toLowerCase()));
        return list;
    }

    public List<Doctor> findAll() {
        return clinic.getDoctors().findAll();
    }
}
