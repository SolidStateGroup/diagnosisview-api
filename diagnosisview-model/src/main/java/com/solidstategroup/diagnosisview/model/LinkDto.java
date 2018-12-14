package com.solidstategroup.diagnosisview.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.solidstategroup.diagnosisview.model.codes.Lookup;
import com.solidstategroup.diagnosisview.model.codes.enums.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkDto {
    private Long id;
    private Lookup linkType;
    private DifficultyLevel difficultyLevel;
    private String link;
    private Boolean displayLink;
    private String name;
    private Boolean freeLink;
    private Boolean transformationsOnly;
}
