package com.solidstategroup.diagnosisview.model;

import com.solidstategroup.diagnosisview.model.codes.Lookup;
import com.solidstategroup.diagnosisview.model.codes.enums.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LinkDto {
    private Lookup linkType;
    private DifficultyLevel difficultyLevel;
    private String link;
}
