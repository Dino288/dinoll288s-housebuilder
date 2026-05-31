package com.dinoll288.housebuilder.client;

public enum BuildMaterialSlot {
    WALL("Walls"),
    FLOOR("Floor"),
    ROOF("Roof");

    private final String label;

    BuildMaterialSlot(String label) {
        this.label = label;
    }

    public String label() {
        return this.label;
    }
}
