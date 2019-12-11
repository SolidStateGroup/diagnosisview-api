package com.solidstategroup.diagnosisview.model.codes;

import com.solidstategroup.diagnosisview.model.codes.enums.CodeSourceTypes;
import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "pv_code")
public class Code extends AuditModel {

    @Id
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

    @OneToMany(mappedBy = "code", fetch = FetchType.EAGER,
            cascade = {CascadeType.PERSIST, CascadeType.REFRESH}, orphanRemoval = true)
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

    /**
     * Adds a link to the current code.
     */
    public void addLink(Link link) {

        links.add(link);
        link.setCode(this);
    }
}
