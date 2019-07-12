package com.solidstategroup.diagnosisview.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * JSON payment details that comes from the IAP service used
 */

@Data
@AllArgsConstructor
public class GoogleReceipt {
    String packageName;
    String productId;
    String token;
}