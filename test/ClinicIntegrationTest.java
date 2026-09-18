import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import controller.AppointmentController;
import controller.DoctorController;
import controller.PatientController;
import exception.ValidationException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import model.Clinic;
import model.Doctor;
import model.Patient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integration test - Clinic + controllers + FileRepository.
 * Objective: prove the booking rules fire and that data survives a save/load
 * cycle, which is the Task 7 File I/O requirement.
 */
class ClinicIntegrationTest {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Clinic newClinic(Path dir) {
        return new Clinic("Test Clinic", "1 Test St", "0390000000", dir.toString());
    }

    private String nextDateFor(DayOfWeek day) {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek() != day) {
            date = date.plusDays(1);
        }
        return date.format(FMT);
    }

    @Test
    @DisplayName("TC-C01 registering a patient assigns a sequential id and persists it")
    void registerPatientPersists(@TempDir Path dir) throws Exception {
        Clinic clinic = newClinic(dir);
        PatientController controller = new PatientController(clinic);
        Patient p = controller.registerPatient("John Smith", "14/02/1995", "0411222333",
                "john@example.com", "2123456701", "12 Swanston St", "Penicillin");
        assertEquals("P001", p.getId());

        Clinic reloaded = newClinic(dir);
        reloaded.loadAll();
        assertEquals(1, reloaded.getPatients().count());
        assertEquals("John Smith", reloaded.getPatients().findById("P001").get().getFullName());
    }

    @Test
    @DisplayName("TC-C02 a duplicate Medicare number is rejected")
    void duplicateMedicareRejected(@TempDir Path dir) throws Exception {
        PatientController controller = new PatientController(newClinic(dir));
        controller.registerPatient("John Smith", "14/02/1995", "0411222333",
                "john@example.com", "2123456701", "12 Swanston St", "None");
        ValidationException ex = assertThrows(ValidationException.class,
                () -> controller.registerPatient("Jane Doe", "01/01/1990", "0422333444",
                        "jane@example.com", "2123456701", "5 King St", "None"));
        assertTrue(ex.getMessage().contains("Medicare"));
    }

    @Test
    @DisplayName("TC-C03 booking twice in the same slot is rejected")
    void doubleBookingRejected(@TempDir Path dir) throws Exception {
        Clinic clinic = newClinic(dir);
        PatientController patients = new PatientController(clinic);
        DoctorController doctors = new DoctorController(clinic);
        AppointmentController appointments = new AppointmentController(clinic);

        patients.registerPatient("John Smith", "14/02/1995", "0411222333",
                "john@example.com", "2123456701", "12 Swanston St", "None");
        patients.registerPatient("Mai Tran", "30/09/2001", "0422333444",
                "mai@example.com", "2123456702", "45 Victoria St", "None");
        doctors.registerDoctor("Emily Watson", "03/07/1982", "0398765432",
                "e.watson@clinic.org.au", "General Practice", "85",
                EnumSet.of(DayOfWeek.MONDAY));

        String monday = nextDateFor(DayOfWeek.MONDAY);
        appointments.bookAppointment("P001", "D001", monday, "09:30", "Check-up");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> appointments.bookAppointment("P002", "D001", monday, "09:45", "Cough"));
        assertTrue(ex.getMessage().contains("already has appointment"));
    }

    @Test
    @DisplayName("TC-C04 booking on a day the doctor does not work is rejected")
    void unavailableDayRejected(@TempDir Path dir) throws Exception {
        Clinic clinic = newClinic(dir);
        PatientController patients = new PatientController(clinic);
        DoctorController doctors = new DoctorController(clinic);
        AppointmentController appointments = new AppointmentController(clinic);

        patients.registerPatient("John Smith", "14/02/1995", "0411222333",
                "john@example.com", "2123456701", "12 Swanston St", "None");
        doctors.registerDoctor("Emily Watson", "03/07/1982", "0398765432",
                "e.watson@clinic.org.au", "General Practice", "85",
                EnumSet.of(DayOfWeek.MONDAY));

        String sunday = nextDateFor(DayOfWeek.SUNDAY);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> appointments.bookAppointment("P001", "D001", sunday, "09:30", "Check-up"));
        assertTrue(ex.getMessage().contains("does not work"));
    }

    @Test
    @DisplayName("TC-C05 a patient with appointments cannot be deleted")
    void deleteBlockedByReference(@TempDir Path dir) throws Exception {
        Clinic clinic = newClinic(dir);
        PatientController patients = new PatientController(clinic);
        DoctorController doctors = new DoctorController(clinic);
        AppointmentController appointments = new AppointmentController(clinic);

        patients.registerPatient("John Smith", "14/02/1995", "0411222333",
                "john@example.com", "2123456701", "12 Swanston St", "None");
        doctors.registerDoctor("Emily Watson", "03/07/1982", "0398765432",
                "e.watson@clinic.org.au", "General Practice", "85",
                EnumSet.of(DayOfWeek.MONDAY));
        appointments.bookAppointment("P001", "D001", nextDateFor(DayOfWeek.MONDAY),
                "10:00", "Check-up");

        assertThrows(ValidationException.class, () -> patients.deletePatient("P001"));
    }

    @Test
    @DisplayName("TC-C06 appointments reload with their patient and doctor links intact")
    void appointmentLinksSurviveReload(@TempDir Path dir) throws Exception {
        Clinic clinic = newClinic(dir);
        new PatientController(clinic).registerPatient("John Smith", "14/02/1995",
                "0411222333", "john@example.com", "2123456701", "12 Swanston St", "None");
        new DoctorController(clinic).registerDoctor("Emily Watson", "03/07/1982",
                "0398765432", "e.watson@clinic.org.au", "General Practice", "85",
                EnumSet.of(DayOfWeek.MONDAY));
        new AppointmentController(clinic).bookAppointment("P001", "D001",
                nextDateFor(DayOfWeek.MONDAY), "11:00", "Check-up");

        Clinic reloaded = newClinic(dir);
        reloaded.loadAll();
        assertEquals(1, reloaded.getAppointments().count());
        assertEquals("John Smith",
                reloaded.getAppointments().findById("A001").get().getPatient().getFullName());
        assertEquals("Emily Watson",
                reloaded.getAppointments().findById("A001").get().getDoctor().getFullName());
    }

    @Test
    @DisplayName("TC-C07 a doctor's daily schedule is returned in time order")
    void scheduleIsTimeOrdered(@TempDir Path dir) throws Exception {
        Clinic clinic = newClinic(dir);
        PatientController patients = new PatientController(clinic);
        DoctorController doctors = new DoctorController(clinic);
        AppointmentController appointments = new AppointmentController(clinic);

        patients.registerPatient("John Smith", "14/02/1995", "0411222333",
                "john@example.com", "2123456701", "12 Swanston St", "None");
        patients.registerPatient("Mai Tran", "30/09/2001", "0422333444",
                "mai@example.com", "2123456702", "45 Victoria St", "None");
        doctors.registerDoctor("Emily Watson", "03/07/1982", "0398765432",
                "e.watson@clinic.org.au", "General Practice", "85",
                EnumSet.of(DayOfWeek.MONDAY));

        String monday = nextDateFor(DayOfWeek.MONDAY);
        appointments.bookAppointment("P002", "D001", monday, "14:00", "Cough");
        appointments.bookAppointment("P001", "D001", monday, "09:00", "Check-up");

        Doctor doctor = clinic.getDoctors().findById("D001").get();
        LocalDate day = LocalDate.parse(monday, FMT);
        assertEquals("A002", clinic.generateDoctorSchedule(doctor, day).get(0).getId());
    }

    @Test
    @DisplayName("TC-C08 a corrupt line in a data file is skipped, not fatal")
    void corruptLineIsSkipped(@TempDir Path dir) throws IOException, ValidationException {
        Clinic clinic = newClinic(dir);
        new PatientController(clinic).registerPatient("John Smith", "14/02/1995",
                "0411222333", "john@example.com", "2123456701", "12 Swanston St", "None");
        java.nio.file.Files.writeString(dir.resolve("patients.txt"),
                java.nio.file.Files.readString(dir.resolve("patients.txt")) + "BROKEN|LINE\n");

        Clinic reloaded = newClinic(dir);
        reloaded.loadAll();
        assertEquals(1, reloaded.getPatients().count());
    }
}
