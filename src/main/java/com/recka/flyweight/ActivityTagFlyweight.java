package com.recka.flyweight;

public final class ActivityTagFlyweight {
    private final String name;
    private final String colorHex;
    ActivityTagFlyweight(String name, String colorHex) {
        this.name = name;
        this.colorHex = colorHex;
    }
    public String name() { return name; }
    public String colorHex() { return colorHex; }
}
