package com.solidstategroup.diagnosisview.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryDto {
    private Integer number;
    private String icd10Description;
    private String friendlyDescription;
    private boolean hidden = false;

}
