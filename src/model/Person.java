package model;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * Abstract superclass shared by Patient, Doctor and Administrator.
 * All fields are private (encapsulation); access is via getters/setters only.
 */
public abstract class Person implements Identifiable {

    private String id;
    private String fullName;
    private LocalDate dateOfBirth;
    private String phone;
    private String email;

    protected Person(String id, String fullName, LocalDate dateOfBirth, String phone, String email) {
        this.id = id;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.email = email;
    }

    /** Polymorphic hook: every concrete subclass names its own role. */
    public abstract String getRole();

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAge() {
        if (dateOfBirth == null) return 0;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        return Objects.equals(id, ((Person) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return getRole() + " " + id + " - " + fullName; }
}
