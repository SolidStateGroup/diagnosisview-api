package com.solidstategroup.diagnosisview.model;

import com.solidstategroup.diagnosisview.model.codes.enums.Institution;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkMappingDto {

    private String id;

    @NotNull
    private String link;

    @NotNull
    private String transformation;

    @NotNull
    private Institution institution;
}

