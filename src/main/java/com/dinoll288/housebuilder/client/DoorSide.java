package com.dinoll288.housebuilder.client;

public enum DoorSide {
    NORTH("North"),
    SOUTH("South"),
    WEST("West"),
    EAST("East");

    private final String label;

    DoorSide(String label) {
        this.label = label;
    }

    public String label() {
        return this.label;
    }

    public DoorSide next() {
        DoorSide[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public boolean usesWidthSpan() {
        return this == NORTH || this == SOUTH;
    }
}
