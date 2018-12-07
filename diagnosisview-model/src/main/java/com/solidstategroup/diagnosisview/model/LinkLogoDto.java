package com.solidstategroup.diagnosisview.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LinkLogoDto {
    private String id;
    private String startsWith;
    private String image;
    private String imageFormat;
}
