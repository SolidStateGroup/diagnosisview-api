package com.solidstategroup.diagnosisview.payloads;


import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload used to validate chargebee subscription once user subscribed via hosted page.
 *
 * <p>Created by Pavlo Maksymchuk.
 */
@Getter
@Setter
@NoArgsConstructor
public class ChargebeeSubscribePayload {

  @NotBlank(message = "can't be empty")
  private String pageId;
}
