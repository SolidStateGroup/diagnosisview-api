package com.solidstategroup.diagnosisview.clients.nhschoices;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Json representation of Condition link as part of the NhsChoicesResponseJson
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConditionLinkJson {

    private String name;
    @JsonProperty("url")
    private String apiUrl;
    private String description;
}
