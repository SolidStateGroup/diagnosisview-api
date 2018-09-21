package com.solidstategroup.diagnosisview.model;

import lombok.Data;

import java.util.Set;

@Data
public class CodeDto {
    private String friendlyName;
    private Boolean deleted;
    private String code;
    private Set<CategoryDto> categories;
    private Set<LinkDto> links;
}


