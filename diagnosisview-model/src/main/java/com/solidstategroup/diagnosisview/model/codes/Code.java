package com.solidstategroup.diagnosisview.model.codes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.solidstategroup.diagnosisview.model.Synonym;
import com.solidstategroup.diagnosisview.model.Tag;
import com.solidstategroup.diagnosisview.model.codes.enums.CodeSourceTypes;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

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

@Data
@Entity
@Table(name = "pv_code")
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonStringType.class),
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // need this to avoid Serialization issue
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

    @Type(type = "jsonb")
    @Column(name = "synonyms", columnDefinition = "jsonb")
    @Builder.Default
    private Set<Synonym> synonyms = new HashSet<>();

    @Type(type = "jsonb")
    @Column(name = "tags", columnDefinition = "jsonb")
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    /**
     * Adds a link to the current code.
     */
    public void addLink(Link link) {

        links.add(link);
        link.setCode(this);
    }
}
