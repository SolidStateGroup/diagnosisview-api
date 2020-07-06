package com.solidstategroup.diagnosisview.model.codes;

import java.io.Serializable;

/**
 * Abstract class to wrap Lookup entity.
 * <p>
 * Created by Pavlo Maksymchuk.
 */
public abstract class LookupWrapper implements Serializable {
    private Long id;
    private String code;
    private String description;


    public LookupWrapper() {
    }

    public LookupWrapper(Lookup lookup) {
        this.id = lookup.getId();
        this.code = lookup.getValue();
        this.description = lookup.getDescription();
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
