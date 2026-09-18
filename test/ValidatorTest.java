import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import exception.ValidationException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.Validator;

/**
 * Test class 1 of 3 - util.Validator
 * Objective: prove every input rule accepts valid data and rejects invalid data,
 * so no malformed record can ever reach the Model layer.
 */
class ValidatorTest {

    @Test
    @DisplayName("TC-V01 valid Australian mobile number is accepted and trimmed")
    void acceptsValidPhone() throws ValidationException {
        assertEquals("0412345678", Validator.requirePhone(" 0412 345 678 "));
    }

    @Test
    @DisplayName("TC-V02 phone with the wrong length is rejected")
    void rejectsShortPhone() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.requirePhone("04123"));
        assertTrue(ex.getMessage().contains("Australian"));
    }

    @Test
    @DisplayName("TC-V03 empty required text is rejected with the field name")
    void rejectsEmptyText() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.requireText("Address", "   "));
        assertEquals("Address", ex.getField());
    }

    @Test
    @DisplayName("TC-V04 email without an @ symbol is rejected")
    void rejectsBadEmail() {
        assertThrows(ValidationException.class, () -> Validator.requireEmail("john.example.com"));
    }

    @Test
    @DisplayName("TC-V05 Medicare number must be exactly ten digits")
    void validatesMedicare() throws ValidationException {
        assertEquals("2123456701", Validator.requireMedicare("2123456701"));
        assertThrows(ValidationException.class, () -> Validator.requireMedicare("21234567"));
        assertThrows(ValidationException.class, () -> Validator.requireMedicare("21234567AB"));
    }

    @Test
    @DisplayName("TC-V06 date of birth must be in the past and correctly formatted")
    void validatesDateOfBirth() throws ValidationException {
        assertEquals(LocalDate.of(1995, 2, 14),
                Validator.requirePastDate("Date of birth", "14/02/1995"));
        assertThrows(ValidationException.class,
                () -> Validator.requirePastDate("Date of birth", "1995-02-14"));
        assertThrows(ValidationException.class,
                () -> Validator.requirePastDate("Date of birth", "14/02/2099"));
    }

    @Test
    @DisplayName("TC-V07 a negative fee is rejected, zero is allowed")
    void validatesAmount() throws ValidationException {
        assertEquals(0.0, Validator.requirePositiveAmount("Cost", "0"));
        assertEquals(85.5, Validator.requirePositiveAmount("Cost", "85.50"));
        assertThrows(ValidationException.class,
                () -> Validator.requirePositiveAmount("Cost", "-10"));
        assertThrows(ValidationException.class,
                () -> Validator.requirePositiveAmount("Cost", "eighty"));
    }

    @Test
    @DisplayName("TC-V08 an appointment in the past is rejected")
    void rejectsPastAppointment() {
        assertThrows(ValidationException.class,
                () -> Validator.requireFutureDateTime("01/01/2020", "09:00"));
    }
}
