import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import model.Appointment;
import model.AppointmentStatus;
import model.Doctor;
import model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test class 2 of 3 - model.Appointment
 * Objective: prove the 30 minute slot arithmetic and the double-booking rule
 * behave correctly at the boundaries (touching slots must NOT clash).
 */
class AppointmentTest {

    private Patient patientA;
    private Patient patientB;
    private Doctor doctorOne;
    private Doctor doctorTwo;
    private LocalDateTime base;

    @BeforeEach
    void setUp() {
        patientA = new Patient("P001", "John Smith", LocalDate.of(1995, 2, 14),
                "0411222333", "john@example.com", "2123456701", "12 Swanston St", "None");
        patientB = new Patient("P002", "Mai Tran", LocalDate.of(2001, 9, 30),
                "0422333444", "mai@example.com", "2123456702", "45 Victoria St", "None");
        doctorOne = new Doctor("D001", "Emily Watson", LocalDate.of(1982, 7, 3),
                "0398765432", "e.watson@clinic.org.au", "General Practice", 85.0,
                EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY));
        doctorTwo = new Doctor("D002", "Raj Patel", LocalDate.of(1978, 11, 20),
                "0399887766", "r.patel@clinic.org.au", "Paediatrics", 120.0,
                EnumSet.of(DayOfWeek.MONDAY));
        base = LocalDateTime.of(2030, 1, 7, 9, 0);   // a Monday
    }

    @Test
    @DisplayName("TC-A01 a new appointment starts with status SCHEDULED")
    void newAppointmentIsScheduled() {
        Appointment a = new Appointment("A001", patientA, doctorOne, base, "Check-up");
        assertEquals(AppointmentStatus.SCHEDULED, a.getStatus());
    }

    @Test
    @DisplayName("TC-A02 an appointment ends thirty minutes after it starts")
    void endTimeIsThirtyMinutesLater() {
        Appointment a = new Appointment("A001", patientA, doctorOne, base, "Check-up");
        assertEquals(base.plusMinutes(30), a.getEndTime());
    }

    @Test
    @DisplayName("TC-A03 overlapping appointments for the same doctor clash")
    void overlappingSlotsClash() {
        Appointment first = new Appointment("A001", patientA, doctorOne, base, "Check-up");
        Appointment second = new Appointment("A002", patientB, doctorOne,
                base.plusMinutes(15), "Cough");
        assertTrue(second.clashesWith(first));
    }

    @Test
    @DisplayName("TC-A04 back-to-back slots do NOT clash (boundary case)")
    void adjacentSlotsDoNotClash() {
        Appointment first = new Appointment("A001", patientA, doctorOne, base, "Check-up");
        Appointment second = new Appointment("A002", patientB, doctorOne,
                base.plusMinutes(30), "Cough");
        assertFalse(second.clashesWith(first));
    }

    @Test
    @DisplayName("TC-A05 the same time with a different doctor does not clash")
    void differentDoctorsDoNotClash() {
        Appointment first = new Appointment("A001", patientA, doctorOne, base, "Check-up");
        Appointment second = new Appointment("A002", patientB, doctorTwo, base, "Cough");
        assertFalse(second.clashesWith(first));
    }

    @Test
    @DisplayName("TC-A06 a cancelled appointment frees its slot")
    void cancelledAppointmentDoesNotBlockSlot() {
        Appointment first = new Appointment("A001", patientA, doctorOne, base, "Check-up");
        first.cancel();
        Appointment second = new Appointment("A002", patientB, doctorOne, base, "Cough");
        assertEquals(AppointmentStatus.CANCELLED, first.getStatus());
        assertFalse(second.clashesWith(first));
    }

    @Test
    @DisplayName("TC-A07 the display format is dd/MM/yyyy HH:mm")
    void formatsDateTimeForDisplay() {
        Appointment a = new Appointment("A001", patientA, doctorOne, base, "Check-up");
        assertEquals("07/01/2030 09:00", a.getFormattedDateTime());
    }

    @Test
    @DisplayName("TC-A08 the doctor availability flag matches the working days set")
    void doctorAvailabilityIsChecked() {
        assertTrue(doctorOne.isAvailableOn(DayOfWeek.MONDAY));
        assertFalse(doctorOne.isAvailableOn(DayOfWeek.SUNDAY));
    }
}
