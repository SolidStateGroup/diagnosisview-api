package com.solidstategroup.diagnosisview.model.codes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.codes.enums.CodeSourceTypes;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "pv_code")
public class Code {

    @Id
    @SequenceGenerator(name = "code_seq", sequenceName = "code_seq", initialValue = 2000000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "code_seq")
    private Long id;

    @Column(name = "code")
    private String code;

    @OneToMany(mappedBy = "code", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CodeCategory> codeCategories = new HashSet<>();

    @OneToOne
    @JoinColumn(name = "type_id")
    private Lookup codeType;

    @Column(name = "display_order")
    private Integer displayOrder;

    @OneToMany(mappedBy = "code", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.REFRESH},
            orphanRemoval = true)
    private Set<CodeExternalStandard> externalStandards = new HashSet<>();

    // called Name in ui
    @Column(name = "description", length = 500)
    private String description;

    // from NHS choices initially
    @Column(name = "full_description", length = 500)
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

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private CodeSourceTypes sourceType;

    @OneToOne
    @JoinColumn(name = "standard_type_id")
    private Lookup standardType;

    @CreationTimestamp
    @Column(name = "creation_date")
    private Date created;

    @JsonIgnore
    @CreatedBy
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User creator;

    @UpdateTimestamp
    @Column(name = "last_update_date")
    private Date lastUpdate;

    @JsonIgnore
    @LastModifiedBy
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_updated_by")
    private User lastUpdater;
}
