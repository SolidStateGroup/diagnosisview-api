package com.solidstategroup.diagnosisview.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * Stores Synonym as jsonb value for the Code
 */
@Data
@AllArgsConstructor
public class Synonym implements Serializable {
    private String name;
}
