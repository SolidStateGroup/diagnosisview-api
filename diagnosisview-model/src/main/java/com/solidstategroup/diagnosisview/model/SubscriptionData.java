package com.solidstategroup.diagnosisview.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JSON to store subscription type related data.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionData implements Serializable {

  private String subscriptionId;
}
