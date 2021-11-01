package com.solidstategroup.diagnosisview.results;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.solidstategroup.diagnosisview.model.CodeDto;
import com.solidstategroup.diagnosisview.model.LinkDto;
import com.solidstategroup.diagnosisview.model.SavedUserCode;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * FavouriteResult pojo to return saved favourite links for user
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FavouriteResult implements Serializable {

  private String code;
  private String friendlyName;
  private Date dateAdded;
  private LinkDto link;

  public static FavouriteResult toResult(SavedUserCode favourite, CodeDto code, LinkDto link) {
    return FavouriteResult.builder()
        .code(code.getCode())
        .friendlyName(code.getFriendlyName())
        .link(link)
        .dateAdded(favourite.getDateAdded())
        .build();
  }
}
