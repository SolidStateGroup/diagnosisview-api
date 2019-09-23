package com.solidstategroup.diagnosisview.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * JSON payment details that comes from the IAP service used
 */

@Data
@AllArgsConstructor
public class GoogleReceipt implements Serializable {
    String packageName;
    String productId;
    String token;
}