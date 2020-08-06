package com.solidstategroup.diagnosisview.model.codes;

import java.io.Serializable;
import java.util.Date;

/**
 * Abstract class to wrap Lookup entity.
 * <p>
 * Created by Pavlo Maksymchuk.
 */
public abstract class LookupWrapper implements Serializable {
    private Long id;
    private String code;
    private String description;
    private Date created;
    private Date lastUpdate;

    public LookupWrapper() {
    }

    public LookupWrapper(Lookup lookup) {
        this.id = lookup.getId();
        this.code = lookup.getValue();
        this.description = lookup.getDescription();
        this.created = lookup.getCreated();
        this.lastUpdate = lookup.getLastUpdate();
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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
