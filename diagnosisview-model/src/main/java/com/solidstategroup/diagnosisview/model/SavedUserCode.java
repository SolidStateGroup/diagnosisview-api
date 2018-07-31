package com.solidstategroup.diagnosisview.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Custom field used by user object to persist favourites and search history
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SavedUserCode {

    @ApiModelProperty(example = "coma")
    private String code;
    @ApiModelProperty(example = "MEDLINE_PLUS")
    private String type;
    private Date dateAdded;
}
