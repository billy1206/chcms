package model;

import java.time.LocalDate;

public class Patient extends Person {

    private String medicareNumber;
    private String address;
    private String allergies;

    public Patient(String id, String fullName, LocalDate dateOfBirth, String phone,
                   String email, String medicareNumber, String address, String allergies) {
        super(id, fullName, dateOfBirth, phone, email);
        this.medicareNumber = medicareNumber;
        this.address = address;
        this.allergies = (allergies == null || allergies.isBlank()) ? "None" : allergies;
    }

    @Override
    public String getRole() { return "Patient"; }

    public String getMedicareNumber() { return medicareNumber; }
    public void setMedicareNumber(String medicareNumber) { this.medicareNumber = medicareNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }
}
