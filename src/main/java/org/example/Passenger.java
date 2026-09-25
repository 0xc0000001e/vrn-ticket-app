package org.example;

import java.time.LocalDate;

public record Passenger(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String documentNumber
) {
    public String getFullName() {
        return firstName + " " + lastName;
    }
}