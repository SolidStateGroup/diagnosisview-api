package com.solidstategroup.diagnosisview.model;

import com.solidstategroup.diagnosisview.model.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * JSON payment details that comes from the IAP service used
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetails implements Serializable {
    private String response;
    private GoogleReceipt googleReceipt;
    private PaymentType paymentType;
}
