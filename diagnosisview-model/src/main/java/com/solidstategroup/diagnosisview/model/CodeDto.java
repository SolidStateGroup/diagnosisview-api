package com.solidstategroup.diagnosisview.model;

import lombok.Data;

@Data
public class CodeDto {
    private String friendlyName;
    private Boolean deleted;
    private String code;
}
