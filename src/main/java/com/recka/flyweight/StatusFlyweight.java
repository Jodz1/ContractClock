package com.recka.flyweight;

public final class StatusFlyweight {
    private final String status;
    private final String label;
    private final String colorHex;
    StatusFlyweight(String status, String label, String colorHex) {
        this.status = status;
        this.label = label;
        this.colorHex = colorHex;
    }
    public String status() { return status; }
    public String label() { return label; }
    public String colorHex() { return colorHex; }
}
