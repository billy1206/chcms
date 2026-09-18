import exception.ValidationException;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import model.Administrator;
import model.Appointment;
import model.Clinic;
import model.Doctor;
import model.Patient;
import view.MainMenuFrame;

/**
 * Application entry point.
 * Responsibilities: build the Model, load saved data, seed demo records the very
 * first time the program runs, then hand control to the View on the Swing EDT.
 */
public class Main {

    private static final String DATA_DIR = "data";

    public static void main(String[] args) {
        Clinic clinic = new Clinic("Community Health Clinic",
                "288 La Trobe Street, Melbourne VIC 3000", "(03) 9000 1234", DATA_DIR);
        try {
            clinic.loadAll();
            if (clinic.getPatients().count() == 0 && clinic.getDoctors().count() == 0) {
                seedDemoData(clinic);
                clinic.saveAll();
            }
        } catch (IOException | ValidationException ex) {
            JOptionPane.showMessageDialog(null,
                    "Could not load saved data: " + ex.getMessage()
                            + "\nThe application will start with an empty database.",
                    "Start-up warning", JOptionPane.WARNING_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to the default look and feel.
            }
            new MainMenuFrame(clinic).setVisible(true);
        });
    }

    /** Sample data so the marker sees a populated system on first launch. */
    private static void seedDemoData(Clinic clinic) throws ValidationException {
        Administrator admin = new Administrator("AD001", "Sarah Nguyen",
                LocalDate.of(1990, 4, 12), "0412345678", "sarah.nguyen@clinic.org.au",
                "admin", "admin123");
        clinic.getAdministrators().add(admin);

        Doctor gp = new Doctor("D001", "Emily Watson", LocalDate.of(1982, 7, 3),
                "0398765432", "e.watson@clinic.org.au", "General Practice", 85.00,
                EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
        Doctor paed = new Doctor("D002", "Raj Patel", LocalDate.of(1978, 11, 20),
                "0399887766", "r.patel@clinic.org.au", "Paediatrics", 120.00,
                EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY));
        clinic.getDoctors().add(gp);
        clinic.getDoctors().add(paed);

        Patient p1 = new Patient("P001", "John Smith", LocalDate.of(1995, 2, 14),
                "0411222333", "john.smith@example.com", "2123456701",
                "12 Swanston Street, Melbourne VIC 3000", "Penicillin");
        Patient p2 = new Patient("P002", "Mai Tran", LocalDate.of(2001, 9, 30),
                "0422333444", "mai.tran@example.com", "2123456702",
                "45 Victoria Street, Richmond VIC 3121", "None");
        clinic.getPatients().add(p1);
        clinic.getPatients().add(p2);

        LocalDateTime slot = nextWeekday(DayOfWeek.MONDAY).atTime(9, 30);
        clinic.getAppointments().add(new Appointment("A001", p1, gp, slot, "Annual health check"));
        clinic.getAppointments().add(new Appointment("A002", p2, gp,
                slot.plusMinutes(30), "Persistent cough"));
    }

    private static LocalDate nextWeekday(DayOfWeek target) {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek() != target) {
            date = date.plusDays(1);
        }
        return date;
    }
}
