package com.solidstategroup.diagnosisview.model.codes;

/**
 * Abstract class to wrap Lookup entity.
 * <p>
 * Created by Pavlo Maksymchuk.
 */
public abstract class LookupWrapper {
    protected Lookup lookup;

    public LookupWrapper(Lookup lookup) {
        this.lookup = lookup;
    }

    public Long getId() {
        return lookup.getId();
    }

    public String getCode() {
        return lookup.getValue();
    }

    public String getDescription() {
        return lookup.getDescription();
    }
}
