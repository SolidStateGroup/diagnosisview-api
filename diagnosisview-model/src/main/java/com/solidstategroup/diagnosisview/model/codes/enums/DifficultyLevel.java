package com.solidstategroup.diagnosisview.model.codes.enums;


/**
 * Source of Code, PATIENTVIEW by default, NHS_CHOICES if from NHS Choices API.
 * Created by jamesr@solidstategroup.com
 * Created on 08/06/2016
 */
public enum DifficultyLevel {
    GREEN("Green"),
    AMBER("Amber"),
    RED("Red");

    private String name;
    DifficultyLevel(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
