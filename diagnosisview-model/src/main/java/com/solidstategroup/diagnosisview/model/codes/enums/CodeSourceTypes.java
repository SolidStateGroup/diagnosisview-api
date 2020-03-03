package com.solidstategroup.diagnosisview.model.codes.enums;


/**
 * Source of Code, NHS_CHOICES if from NHS Choices API or DIAGNOSISVIEW when added via
 * DV web.
 */
public enum CodeSourceTypes {
    DIAGNOSISVIEW("DiagnosisView"),
    NHS_CHOICES("NHS Choices");

    private String name;

    CodeSourceTypes(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.name();
    }
}
