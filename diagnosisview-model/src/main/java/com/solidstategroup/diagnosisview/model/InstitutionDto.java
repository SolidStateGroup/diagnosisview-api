package com.solidstategroup.diagnosisview.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.solidstategroup.diagnosisview.model.codes.Lookup;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * POJO used to map Institution Lookup for FE.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InstitutionDto implements Serializable {
    private String id;
    private String name;

    public InstitutionDto(Lookup lookup){
        this.id = lookup.getValue();
        this.name = lookup.getDescription();
    }
}


