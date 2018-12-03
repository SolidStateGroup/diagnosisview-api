package com.solidstategroup.diagnosisview.model.codes.enums;

public enum Institution {
    UNIVERSITY_OF_EDINBURGH("UNIVERSITY_OF_EDINBURGH");

    private String name;

    Institution(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
