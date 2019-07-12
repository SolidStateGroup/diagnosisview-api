package com.solidstategroup.diagnosisview.model.enums;


/**
 * Type of user.
 */
public enum RoleType {
    ADMIN("Admin"),
    USER("User");

    private String name;
    RoleType(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
