package com.solidstategroup.diagnosisview.clients.medlineplus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * Representation of Entry as part of the MedlinePlus response json
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntryJson {
    private ValueTypeJson title;
    private LinkJson[] link;
    private ValueTypeJson id;
    private ValueTypeJson updated;
    private ValueTypeJson summary;
}
