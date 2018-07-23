package com.solidstategroup.diagnosisview.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sample custom field, gives an example of how to retrieve from postgres.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomField {
    private Integer key;
    private String name;
    private String value;
}
