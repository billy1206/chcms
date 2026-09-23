package model;

import java.time.LocalDate;

public class Administrator extends Person {

    private String username;
    private String password;

    public Administrator(String id, String fullName, LocalDate dateOfBirth, String phone,
                         String email, String username, String password) {
        super(id, fullName, dateOfBirth, phone, email);
        this.username = username;
        this.password = password;
    }

    @Override
    public String getRole() { return "Administrator"; }

    public boolean authenticate(String user, String pass) {
        return username.equals(user) && password.equals(pass);
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
