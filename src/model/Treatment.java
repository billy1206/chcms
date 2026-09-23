package model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Composition: a Treatment cannot exist without the Appointment it belongs to.
 * Multiplicity: Appointment 1 --- 0..* Treatment.
 */
public class Treatment implements Identifiable {

    private String id;
    private Appointment appointment;
    private String description;
    private String medication;
    private double cost;
    private LocalDate treatmentDate;

    public Treatment(String id, Appointment appointment, String description,
                     String medication, double cost, LocalDate treatmentDate) {
        this.id = id;
        this.appointment = appointment;
        this.description = description;
        this.medication = (medication == null || medication.isBlank()) ? "None" : medication;
        this.cost = cost;
        this.treatmentDate = treatmentDate;
    }

    /** Total charged to the patient = consultation fee + treatment cost. */
    public double getTotalCharge() {
        return cost + appointment.getDoctor().getConsultationFee();
    }

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMedication() { return medication; }
    public void setMedication(String medication) { this.medication = medication; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    public LocalDate getTreatmentDate() { return treatmentDate; }
    public void setTreatmentDate(LocalDate treatmentDate) { this.treatmentDate = treatmentDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Treatment)) return false;
        return Objects.equals(id, ((Treatment) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return id + " | " + description + " | $" + cost; }
}
