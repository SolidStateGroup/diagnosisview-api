package com.solidstategroup.diagnosisview.model;

import com.solidstategroup.diagnosisview.model.codes.AuditModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * NhschoicesCondition Entity to store NHS choices conditions.
 */
//@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Entity
@Table(name = "pv_nhschoices_condition")
public class NhschoicesCondition extends AuditModel {

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "description_last_update_date")
    private Date descriptionLastUpdateDate;

    @Column(name = "introduction_url")
    private String introductionUrl;

    @Column(name = "introduction_url_status")
    private Integer introductionUrlStatus;

    @Column(name = "introduction_url_last_update_date")
    private Date introductionUrlLastUpdateDate;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "uri", nullable = false)
    private String uri;

//    public NhschoicesCondition() {}
//
//    public NhschoicesCondition(String name, String uri) {
//        this.name = name;
//        this.uri = uri;
//    }
//
//    public NhschoicesCondition(String code, String name, String uri) {
//        this.code = code;
//        this.name = name;
//        this.uri = uri;
//    }
}
