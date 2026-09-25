package org.example;

public enum TransitType {
    BUS("Bus"),
    TRAM("Straßenbahn"),
    S_BAHN("S-Bahn"),
    REGIONAL_TRAIN("Regionalzug (RB/RE)"),
    EXPRESS_TRAIN("Fernverkehr (ICE/IC/EC)");

    private final String description;

    TransitType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
