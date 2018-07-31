package com.solidstategroup.diagnosisview.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Sample custom field, gives an example of how to retrieve from postgres.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetails {
    private String response;

}
