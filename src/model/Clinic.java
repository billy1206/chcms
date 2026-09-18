package model;

import exception.ValidationException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import persistence.CsvMapper;
import persistence.FileRepository;
import persistence.Repository;
import util.SearchUtil;
import util.SortUtil;

/**
 * Aggregate root of the Model layer. It owns the four repositories and the
 * clinic-wide business rules (double-booking, doctor availability, schedules).
 * The View never talks to this class directly - only Controllers do (MVC).
 */
public class Clinic {

    private String name;
    private String address;
    private String phone;

    private final Repository<Patient> patients;
    private final Repository<Doctor> doctors;
    private final Repository<Appointment> appointments;
    private final Repository<Treatment> treatments;
    private final Repository<Administrator> administrators;

    public Clinic(String name, String address, String phone, String dataDirectory) {
        this.name = name;
        this.address = address;
        this.phone = phone;

        Path dir = Paths.get(dataDirectory);
        this.patients = new FileRepository<>(dir.resolve("patients.txt"),
                CsvMapper::patientToLine, CsvMapper::patientFromLine);
        this.doctors = new FileRepository<>(dir.resolve("doctors.txt"),
                CsvMapper::doctorToLine, CsvMapper::doctorFromLine);
        this.administrators = new FileRepository<>(dir.resolve("administrators.txt"),
                CsvMapper::adminToLine, CsvMapper::adminFromLine);
        this.appointments = new FileRepository<>(dir.resolve("appointments.txt"),
                CsvMapper::appointmentToLine,
                line -> CsvMapper.appointmentFromLine(line, this.patients, this.doctors));
        this.treatments = new FileRepository<>(dir.resolve("treatments.txt"),
                CsvMapper::treatmentToLine,
                line -> CsvMapper.treatmentFromLine(line, this.appointments));
    }

    // ------------------------------------------------------------ persistence
    /** Load order matters: people first, then the records that reference them. */
    public void loadAll() throws IOException {
        patients.load();
        doctors.load();
        administrators.load();
        appointments.load();
        treatments.load();
    }

    public void saveAll() throws IOException {
        patients.save();
        doctors.save();
        administrators.save();
        appointments.save();
        treatments.save();
    }

    // -------------------------------------------------------- business rules
    /**
     * Books an appointment after checking the two clinic rules:
     * the doctor must work that weekday, and the slot must be free.
     */
    public void bookAppointment(Appointment appointment) throws ValidationException {
        Doctor doctor = appointment.getDoctor();
        if (!doctor.isAvailableOn(appointment.getDateTime().getDayOfWeek())) {
            throw new ValidationException("Appointment date",
                    "Dr " + doctor.getFullName() + " does not work on "
                            + appointment.getDateTime().getDayOfWeek() + ".");
        }
        for (Appointment existing : appointments.findAll()) {
            if (appointment.clashesWith(existing)
                    && existing.getStatus() != AppointmentStatus.CANCELLED) {
                throw new ValidationException("Appointment time",
                        "Dr " + doctor.getFullName() + " already has appointment "
                                + existing.getId() + " at " + existing.getFormattedDateTime() + ".");
            }
        }
        appointments.add(appointment);
    }

    /** Task 8 in action: filter with linear search, then order with quick sort. */
    public List<Appointment> generateDoctorSchedule(Doctor doctor, LocalDate date) {
        List<Appointment> found = SearchUtil.linearSearch(appointments.findAll(),
                a -> a.getDoctor().getId().equals(doctor.getId())
                        && a.getDateTime().toLocalDate().equals(date)
                        && a.getStatus() != AppointmentStatus.CANCELLED);
        SortUtil.quickSort(found, Comparator.comparing(Appointment::getDateTime));
        return found;
    }

    public double getTotalRevenue() {
        double total = 0;
        for (Treatment t : treatments.findAll()) {
            total += t.getTotalCharge();
        }
        return total;
    }

    public long countByStatus(AppointmentStatus status) {
        return appointments.findAll().stream().filter(a -> a.getStatus() == status).count();
    }

    // ---------------------------------------------------------------- getters
    public Repository<Patient> getPatients() { return patients; }
    public Repository<Doctor> getDoctors() { return doctors; }
    public Repository<Appointment> getAppointments() { return appointments; }
    public Repository<Treatment> getTreatments() { return treatments; }
    public Repository<Administrator> getAdministrators() { return administrators; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
