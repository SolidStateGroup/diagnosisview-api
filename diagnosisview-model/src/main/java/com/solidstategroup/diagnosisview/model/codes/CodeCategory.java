package com.solidstategroup.diagnosisview.model.codes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.solidstategroup.diagnosisview.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 17/06/2016
 */
@Entity
@Table(name = "pv_code_category")
public class CodeCategory extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "code_id", nullable = false)
    private Code code;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public CodeCategory() {

    }

    public CodeCategory(Code code, Category category) {
        this.code = code;
        this.category = category;
    }

    @JsonIgnore
    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
