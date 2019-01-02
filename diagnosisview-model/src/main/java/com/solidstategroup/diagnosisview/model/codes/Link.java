package com.solidstategroup.diagnosisview.model.codes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.solidstategroup.diagnosisview.model.codes.enums.DifficultyLevel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 25/06/2014
 */
@Entity
@Table(name = "pv_link")
public class Link extends AuditModel {

    @OneToOne
    @JoinColumn(name = "type_id")
    private Lookup linkType;

    @ManyToOne
    @JoinColumn(name = "code_id")
    private Code code;

    @Column(name = "difficulty_level")
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @Column(name = "link")
    private String link;

    @Column(name = "name")
    private String name;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "free_link", nullable = false)
    private Boolean freeLink;

    @Column(name = "transformations_only")
    private Boolean transformationsOnly;

    @OneToMany(mappedBy = "link")
    private Set<LinkRuleMapping> mappingLinks;

    @ManyToOne
    @JoinColumn(name = "link_logo_id")
    private LogoRule logoRule;

    public Lookup getLinkType() {
        return linkType;
    }

    public void setLinkType(Lookup linkType) {
        this.linkType = linkType;
    }

    @JsonIgnore
    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public DifficultyLevel getDifficultyLevel() {
        if (difficultyLevel == null) {
            return DifficultyLevel.GREEN;
        }
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public boolean hasDifficultyLevelSet() {
        return this.difficultyLevel != null;
    }

    public boolean hasFreeLinkSet() {
        return this.freeLink != null;
    }

    public Boolean getFreeLink() {
        if (freeLink == null) {
            return false;
        }
        return freeLink;
    }

    public void setFreeLink(Boolean freeLink) {
        this.freeLink = freeLink;
    }

    public boolean hasTransformationOnly() { return this.transformationsOnly != null; }

    public boolean useTransformationsOnly() {
        return transformationsOnly;
    }

    public void setTransformationsOnly(boolean transformationsOnly) {
        this.transformationsOnly = transformationsOnly;
    }

    public Set<LinkRuleMapping> getMappingLinks() {
        return mappingLinks;
    }

    public void setMappingLinks(Set<LinkRuleMapping> mappingLinks) {
        this.mappingLinks = mappingLinks;
    }

    public LogoRule getLogoRule() {
        return logoRule;
    }

    public void setLogoRule(LogoRule logoRule) {
        this.logoRule = logoRule;
    }
}
