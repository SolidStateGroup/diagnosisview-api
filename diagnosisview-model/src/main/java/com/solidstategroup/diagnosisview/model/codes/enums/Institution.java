package com.solidstategroup.diagnosisview.model.codes.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Institution {
    UNIVERSITY_OF_EDINBURGH("University of Edinburgh"),
    NHS_SCOTLAND_KNOWLEDGE_NETWORK("NHS Scotland Knowledge Network"),
    UNIVERSITY_OF_MALAWI("University of Malawi"),
    OTHER("Other"),
    NONE("NONE");

    private String name;

    Institution(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.name();
    }
}
