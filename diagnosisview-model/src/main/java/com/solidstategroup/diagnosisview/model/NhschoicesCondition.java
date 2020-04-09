package com.solidstategroup.diagnosisview.model;

import com.solidstategroup.diagnosisview.model.codes.AuditModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * NhschoicesCondition Entity to store NHS choices conditions.
 */
//@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Entity
@Table(name = "dv_nhschoices_condition")
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

    // NHS Choices dates
    @Column(name = "published_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date publishedDate;

    @Column(name = "modified_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedDate;
}
