package com.solidstategroup.diagnosisview.model.codes.enums;


/**
 * Difficulty level of a link.
 */
public enum DifficultyLevel {
    DO_NOT_OVERRIDE("Do_Not_Override"),
    GREEN("Green"),
    AMBER("Amber"),
    RED("Red");

    private String name;
    DifficultyLevel(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
