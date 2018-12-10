package com.solidstategroup.diagnosisview.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkLogoDto {
    private String id;
    private String startsWith;
    private String image;
    private String imageFormat;
    private String imageUrl;
}
