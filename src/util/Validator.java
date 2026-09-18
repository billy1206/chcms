package util;

import exception.ValidationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Single Responsibility: this class only validates and parses raw text.
 * Every rule lives here once, so the same rule is reused by every controller.
 */
public final class Validator {

    private static final Pattern NAME = Pattern.compile("^[A-Za-z][A-Za-z .'-]{1,49}$");
    private static final Pattern PHONE = Pattern.compile("^(0[2-478]\\d{8}|\\+61[2-478]\\d{8})$");
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]{2,}$");
    private static final Pattern MEDICARE = Pattern.compile("^\\d{10}$");

    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private Validator() { }

    public static String requireText(String field, String value) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(field, field + " cannot be empty.");
        }
        return value.trim();
    }

    public static String requireName(String field, String value) throws ValidationException {
        String v = requireText(field, value);
        if (!NAME.matcher(v).matches()) {
            throw new ValidationException(field, field + " must be 2-50 letters (spaces, ' and - allowed).");
        }
        return v;
    }

    public static String requirePhone(String value) throws ValidationException {
        String v = requireText("Phone", value).replace(" ", "");
        if (!PHONE.matcher(v).matches()) {
            throw new ValidationException("Phone", "Phone must be a valid Australian number, e.g. 0412345678.");
        }
        return v;
    }

    public static String requireEmail(String value) throws ValidationException {
        String v = requireText("Email", value);
        if (!EMAIL.matcher(v).matches()) {
            throw new ValidationException("Email", "Email must look like name@example.com.");
        }
        return v;
    }

    public static String requireMedicare(String value) throws ValidationException {
        String v = requireText("Medicare number", value).replace(" ", "");
        if (!MEDICARE.matcher(v).matches()) {
            throw new ValidationException("Medicare number", "Medicare number must be exactly 10 digits.");
        }
        return v;
    }

    public static double requirePositiveAmount(String field, String value) throws ValidationException {
        String v = requireText(field, value);
        double amount;
        try {
            amount = Double.parseDouble(v);
        } catch (NumberFormatException ex) {
            throw new ValidationException(field, field + " must be a number.");
        }
        if (amount < 0) {
            throw new ValidationException(field, field + " cannot be negative.");
        }
        return amount;
    }

    public static LocalDate requireDate(String field, String value) throws ValidationException {
        String v = requireText(field, value);
        try {
            return LocalDate.parse(v, DATE_FMT);
        } catch (DateTimeParseException ex) {
            throw new ValidationException(field, field + " must use the format dd/MM/yyyy.");
        }
    }

    public static LocalDate requirePastDate(String field, String value) throws ValidationException {
        LocalDate date = requireDate(field, value);
        if (!date.isBefore(LocalDate.now())) {
            throw new ValidationException(field, field + " must be in the past.");
        }
        if (date.isBefore(LocalDate.now().minusYears(130))) {
            throw new ValidationException(field, field + " is not realistic.");
        }
        return date;
    }

    public static LocalDateTime requireFutureDateTime(String dateValue, String timeValue)
            throws ValidationException {
        LocalDate date = requireDate("Appointment date", dateValue);
        String t = requireText("Appointment time", timeValue);
        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.of(date, java.time.LocalTime.parse(t, TIME_FMT));
        } catch (DateTimeParseException ex) {
            throw new ValidationException("Appointment time", "Time must use the 24-hour format HH:mm.");
        }
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Appointment date", "Appointments cannot be booked in the past.");
        }
        return dateTime;
    }
}
