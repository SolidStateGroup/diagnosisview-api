package com.solidstategroup.diagnosisview.model;

import com.solidstategroup.diagnosisview.model.codes.enums.CriteriaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkRuleDto {

    private String id;

    @NotNull
    private String link;

    @NotNull
    private String transformation;

    @NotNull
    private CriteriaType criteriaType;

    @NotNull
    private String criteria;
}

