package org.example;

import java.time.LocalDate;

public class Passenger {
    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final String documentNumber;

    public Passenger(String firstName, String lastName, LocalDate dateOfBirth, String documentNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.documentNumber = documentNumber;
    }

    public String firstName() { return firstName; }
    public String lastName() { return lastName; }
    public LocalDate dateOfBirth() { return dateOfBirth; }
    public String documentNumber() { return documentNumber; }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
