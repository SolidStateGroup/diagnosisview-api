package com.solidstategroup.diagnosisview.model.codes;

import com.solidstategroup.diagnosisview.model.codes.enums.CodeSourceTypes;
import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 25/06/2014
 */
@Entity
@Table(name = "pv_code")
@Data
public class Code extends AuditModel {

    @Column(name = "code")
    private String code;

    @OneToMany(mappedBy = "code", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CodeCategory> codeCategories = new HashSet<>();

    @OneToOne
    @JoinColumn(name = "type_id")
    private Lookup codeType;

    @Column(name = "display_order")
    private Integer displayOrder;

    // called Name in ui
    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "code", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.REFRESH},
            orphanRemoval = true)
    private Set<CodeExternalStandard> externalStandards = new HashSet<>();

    // from NHS choices initially
    @Column(name = "full_description")
    private String fullDescription;

    @Column(name = "hide_from_patients")
    private boolean hideFromPatients = false;

    @OneToMany(mappedBy = "code", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Link> links = new HashSet<>();

    // used for PATIENTVIEW code standard Codes, from NHS choices initially
    @Column(name = "patient_friendly_name")
    private String patientFriendlyName;

    // used when comparing to NHS Choices
    @Column(name = "removed_externally")
    private boolean removedExternally = false;

    @Column(name = "source_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CodeSourceTypes sourceType = CodeSourceTypes.PATIENTVIEW;

    @OneToOne
    @JoinColumn(name = "standard_type_id")
    private Lookup standardType;

}
