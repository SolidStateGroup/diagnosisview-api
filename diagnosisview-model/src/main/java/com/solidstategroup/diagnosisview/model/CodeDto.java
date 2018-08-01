package com.solidstategroup.diagnosisview.model;

import com.solidstategroup.diagnosisview.model.codes.Link;
import lombok.Data;

import java.util.Set;

@Data
public class CodeDto {
    private String friendlyName;
    private Boolean deleted;
    private String code;
    private Set<LinkDto> links;
}


