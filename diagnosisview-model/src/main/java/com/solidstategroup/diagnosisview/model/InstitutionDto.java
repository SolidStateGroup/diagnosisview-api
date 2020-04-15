package com.solidstategroup.diagnosisview.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.solidstategroup.diagnosisview.model.codes.enums.DifficultyLevel;
import com.solidstategroup.diagnosisview.model.codes.enums.Institution;
import lombok.Builder;
import lombok.Data;

/**
 * POJO used to map Institution enum
 * for FE
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InstitutionDto {
    private String id;
    private String name;

    public InstitutionDto(Institution institution){
        this.id = institution.getId();
        this.name = institution.getName();
    }
}


