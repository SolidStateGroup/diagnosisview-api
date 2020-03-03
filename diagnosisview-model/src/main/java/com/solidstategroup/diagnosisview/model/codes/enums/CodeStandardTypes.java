package com.solidstategroup.diagnosisview.model.codes.enums;

/**
 * Used in testing etc where specific name of code standard type is used (e.g. NHS_CHOICES, EDTA). Stored in Lookups
 * in database.
 */
public enum CodeStandardTypes {
    EDTA("EDTA"),
    ICD("ICD"),
    NHS_CHOICES("NHS Choices"),
    READ("READ"),
    SNOMED("SNOMED");

    private String name;

    CodeStandardTypes(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.name();
    }
}
