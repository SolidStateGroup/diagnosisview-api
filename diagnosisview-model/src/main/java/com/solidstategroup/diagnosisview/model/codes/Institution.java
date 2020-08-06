package com.solidstategroup.diagnosisview.model.codes;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Institution model represents a Lookup of a type (LookupType) Institution.
 * <p>
 * Created by Pavlo Maksymchuk.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Institution extends LookupWrapper {

    private boolean hidden = false;
    private Map<String, Object> stats;

    public Institution() {
        super();
    }

    public Institution(Lookup lookup) {
        super(lookup);

        // extra data is stored as map in jsonb field
        // against lookup table
        if (lookup.getData() != null) {
            hidden = (boolean) lookup.getData().get("hidden");
        }
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public void setStats(Map<String, Object> stats) {
        this.stats = stats;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }
}
