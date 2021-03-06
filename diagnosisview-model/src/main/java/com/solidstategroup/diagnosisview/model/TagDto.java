package com.solidstategroup.diagnosisview.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.solidstategroup.diagnosisview.model.codes.Lookup;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * POJO used to map Tag Lookup for FE.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TagDto implements Serializable {
    private String code;
    private String description;

    public TagDto(Lookup lookup){
        this.code = lookup.getValue();
        this.description = lookup.getDescription();
    }
}


