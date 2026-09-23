package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class Doctor extends Person {

    private String specialisation;
    private double consultationFee;
    private final Set<DayOfWeek> availableDays = EnumSet.noneOf(DayOfWeek.class);

    public Doctor(String id, String fullName, LocalDate dateOfBirth, String phone,
                  String email, String specialisation, double consultationFee,
                  Set<DayOfWeek> availableDays) {
        super(id, fullName, dateOfBirth, phone, email);
        this.specialisation = specialisation;
        this.consultationFee = consultationFee;
        if (availableDays != null) this.availableDays.addAll(availableDays);
    }

    @Override
    public String getRole() { return "Doctor"; }

    /** Business rule owned by Doctor itself, not by the GUI (High Cohesion). */
    public boolean isAvailableOn(DayOfWeek day) {
        return availableDays.contains(day);
    }

    public String getSpecialisation() { return specialisation; }
    public void setSpecialisation(String specialisation) { this.specialisation = specialisation; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }

    /** Defensive copy so external code cannot mutate internal state. */
    public Set<DayOfWeek> getAvailableDays() {
        Set<DayOfWeek> copy = EnumSet.noneOf(DayOfWeek.class);
        copy.addAll(availableDays);
        return copy;
    }

    public void setAvailableDays(Set<DayOfWeek> days) {
        availableDays.clear();
        if (days != null) availableDays.addAll(days);
    }

    public List<String> getAvailableDayNames() {
        List<String> names = new ArrayList<>();
        for (DayOfWeek d : availableDays) {
            names.add(d.toString().charAt(0) + d.toString().substring(1, 3).toLowerCase());
        }
        return names;
    }
}
