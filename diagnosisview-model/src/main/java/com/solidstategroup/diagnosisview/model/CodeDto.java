package com.solidstategroup.diagnosisview.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodeDto {
    private String friendlyName;
    private Boolean deleted;
    private String code;
    private Set<CategoryDto> categories;
    private Set<LinkDto> links;
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    private boolean hideFromPatients;
    private boolean removedExternally;
    private Date created;
}


