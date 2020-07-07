package com.solidstategroup.diagnosisview.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Custom field used by user object to persist favourites and search history
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavedUserCode implements Serializable {

    @ApiModelProperty(example = "123L")
    private Long linkId;
    @ApiModelProperty(example = "coma")
    private String code;
    @ApiModelProperty(example = "MEDLINE_PLUS")
    private String type;
    private Date dateAdded;
}
