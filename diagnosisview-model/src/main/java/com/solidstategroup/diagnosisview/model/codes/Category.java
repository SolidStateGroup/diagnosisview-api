package com.solidstategroup.diagnosisview.model.codes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.solidstategroup.diagnosisview.model.BaseModel;
import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 17/06/2016
 */
@Entity
@Table(name = "pv_category")
@Data
public class Category extends BaseModel {

    @Column(name = "number", nullable = false)
    private Integer number;

    @Column(name = "icd10_description", nullable = false)
    private String icd10Description;

    @Column(name = "friendly_description", nullable = false)
    private String friendlyDescription;

    @Column(name = "hidden", nullable = false)
    private boolean hidden = false;

    @JsonIgnore
    @OneToMany(mappedBy = "category", cascade = {CascadeType.ALL})
    private Set<CodeCategory> codeCategories = new HashSet<>();

    public Category() {}

    public Category(Integer number, String icd10Description, String friendlyDescription) {
        this.number = number;
        this.icd10Description = icd10Description;
        this.friendlyDescription = friendlyDescription;
    }

    public Category(Integer number, String icd10Description, String friendlyDescription, boolean hidden) {
        this.number = number;
        this.icd10Description = icd10Description;
        this.friendlyDescription = friendlyDescription;
        this.hidden = hidden;
    }
}
