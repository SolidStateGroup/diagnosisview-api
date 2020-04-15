package com.solidstategroup.diagnosisview.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.solidstategroup.diagnosisview.model.codes.enums.DifficultyLevel;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * POJO used to map DifficultyLevel enum
 * for FE
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DifficultyLevelDto {
    private String id;
    private String name;

    public DifficultyLevelDto(DifficultyLevel difficultyLevel){
        this.id = difficultyLevel.getId();
        this.name = difficultyLevel.getName();
    }
}


