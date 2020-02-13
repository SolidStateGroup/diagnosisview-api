package com.solidstategroup.diagnosisview.clients.medlineplus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Generic representation of different fields in MedlinePlus response json.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValueTypeJson {

    @JsonProperty("_value")
    private String value;
    private String type;
}
