package org.example;

public enum TicketStatus {
    AKTIV("Aktiv"),
    PAUSIERT("Pausiert"),
    GEKUENDIGT("Gekündigt"),
    ABGELAUFEN("Abgelaufen");

    private final String displayName;

    TicketStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}