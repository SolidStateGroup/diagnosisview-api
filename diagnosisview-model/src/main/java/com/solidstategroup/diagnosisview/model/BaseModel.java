package com.solidstategroup.diagnosisview.model;


import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;


@MappedSuperclass
public abstract class BaseModel implements Serializable, Comparable {

    protected BaseModel() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(this.getClass().isAssignableFrom(o.getClass()))) {
            return false;
        }

        BaseModel baseModel = (BaseModel) o;
        return id != null && id.equals(baseModel.id);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        } else {
            return -1;
        }
    }

    public int compareTo(Object o) {
        BaseModel baseModel = (BaseModel) o;
        return this.id.compareTo(baseModel.getId());
    }
}
