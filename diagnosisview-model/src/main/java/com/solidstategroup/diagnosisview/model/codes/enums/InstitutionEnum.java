package com.solidstategroup.diagnosisview.model.codes.enums;

public enum InstitutionEnum {
    UNIVERSITY_OF_EDINBURGH("University of Edinburgh"),
    NHS_SCOTLAND_KNOWLEDGE_NETWORK("NHS Scotland Knowledge Network"),
    UNIVERSITY_OF_MALAWI("University of Malawi"),
    OTHER("Other"),
    NONE("None");

    private String name;

    InstitutionEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.name();
    }
}
