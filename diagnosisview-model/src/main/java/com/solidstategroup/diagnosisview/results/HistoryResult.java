package com.solidstategroup.diagnosisview.results;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.solidstategroup.diagnosisview.model.CodeDto;
import com.solidstategroup.diagnosisview.model.SavedUserCode;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * HistoryResult pojo to return saved history codes for user
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoryResult implements Serializable {

  private String code;
  private String friendlyName;
  private Date dateAdded;

  public static HistoryResult toResult(SavedUserCode history, CodeDto code) {
    return HistoryResult.builder()
        .code(code.getCode())
        .friendlyName(code.getFriendlyName())
        .dateAdded(history.getDateAdded())
        .build();
  }
}
