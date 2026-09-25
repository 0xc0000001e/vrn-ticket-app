package org.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public record DeutschlandTicket(
        String ticketId,
        Passenger passenger,
        YearMonth validMonth,
        BigDecimal price,
        String issuer,
        TicketStatus status,
        Set<TransitType> allowedTransit
) {
    public static final BigDecimal PRICE_EUR = new BigDecimal("58.00");

    public static DeutschlandTicket issueVrnTicket(Passenger passenger, YearMonth month) {
        Set<TransitType> allowed = EnumSet.of(
                TransitType.BUS,
                TransitType.TRAM,
                TransitType.S_BAHN,
                TransitType.REGIONAL_TRAIN
        );

        String id = "VRN-DT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new DeutschlandTicket(
                id,
                passenger,
                month,
                PRICE_EUR,
                "VRN (Verkehrsverbund Rhein-Neckar)",
                TicketStatus.AKTIV,
                allowed
        );
    }

    public boolean isValidForDate(LocalDate date) {
        return status == TicketStatus.AKTIV && YearMonth.from(date).equals(validMonth);
    }

    public boolean canRide(TransitType transitType) {
        return allowedTransit.contains(transitType);
    }

    public String toQrPayload() {
        return String.format(
                "VDV-KA|ISSUER:%s|ID:%s|HOLDER:%s|DOB:%s|VALID:%s|PRICE:%.2f EUR",
                issuer, ticketId, passenger.getFullName(), passenger.dateOfBirth(), validMonth, price
        );
    }
}