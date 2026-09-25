package org.example;

import java.time.LocalDate;
import java.time.YearMonth;

public class Main {
    public static void main(String[] args) {
        Passenger passenger = new Passenger(
                "Alex",
                "Müller",
                LocalDate.of(1998, 4, 12),
                "DE987654321"
        );

        YearMonth targetMonth = YearMonth.now();
        DeutschlandTicket ticket = DeutschlandTicket.issueVrnTicket(passenger, targetMonth);

        System.out.println("==========================================");
        System.out.println("   DIGITALES VRN DEUTSCHLANDTICKET");
        System.out.println("==========================================");
        System.out.println("Ticket-ID:        " + ticket.ticketId());
        System.out.println("Inhaber:          " + ticket.passenger().getFullName());
        System.out.println("Geburtsdatum:     " + ticket.passenger().dateOfBirth());
        System.out.println("Gültigkeitsmonat: " + ticket.validMonth());
        System.out.println("Preis:            " + ticket.price() + " €");
        System.out.println("Status:           " + ticket.status().getDisplayName());
        System.out.println("Aussteller:       " + ticket.issuer());

        System.out.println("\n--- Fahrscheinprüfung ---");
        System.out.println("Heute gültig?                " + (ticket.isValidForDate(LocalDate.now()) ? "JA" : "NEIN"));
        System.out.println("Nutzung von S-Bahn erlaubt?  " + (ticket.canRide(TransitType.S_BAHN) ? "JA" : "NEIN"));
        System.out.println("Nutzung von ICE erlaubt?     " + (ticket.canRide(TransitType.EXPRESS_TRAIN) ? "JA" : "NEIN"));

        System.out.println("\n--- QR-Code Daten ---");
        System.out.println(ticket.toQrPayload());
        System.out.println("==========================================");
    }
}