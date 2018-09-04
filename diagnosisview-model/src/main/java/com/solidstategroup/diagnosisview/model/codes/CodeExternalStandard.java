package com.solidstategroup.diagnosisview.model.codes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.solidstategroup.diagnosisview.model.BaseModel;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Link table between Code and ExternalStandard (M:M)
 * Created by jamesr@solidstategroup.com
 * Created on 18/06/2014
 */
@Entity
@Table(name = "pv_code_external_standard")
public class CodeExternalStandard extends BaseModel {

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_id", nullable = false)
    private Code code;

    @Column(name = "code")
    private String codeString;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "external_standard_id", nullable = false)
    private ExternalStandard externalStandard;

    public CodeExternalStandard() { }

    public CodeExternalStandard(Code code, ExternalStandard externalStandard) {
        this.code = code;
        this.externalStandard = externalStandard;
    }

    public CodeExternalStandard(Code code, ExternalStandard externalStandard, String codeString) {
        this.code = code;
        this.codeString = codeString;
        this.externalStandard = externalStandard;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public String getCodeString() {
        return codeString;
    }

    public void setCodeString(String codeString) {
        this.codeString = codeString;
    }

    public ExternalStandard getExternalStandard() {
        return externalStandard;
    }

    public void setExternalStandard(ExternalStandard externalStandard) {
        this.externalStandard = externalStandard;
    }
}
