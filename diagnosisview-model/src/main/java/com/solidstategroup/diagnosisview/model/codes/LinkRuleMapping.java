package com.solidstategroup.diagnosisview.model.codes;

import com.solidstategroup.diagnosisview.model.codes.enums.CriteriaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "dv_link_rule_mapping")
public class LinkRuleMapping {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "mapping_id")
    private LinkRule rule;

    @ManyToOne
    @JoinColumn(name = "link_id")
    private Link link;

    private String replacementLink;

    @Enumerated(EnumType.STRING)
    private CriteriaType criteriaType;

    private String criteria;
}
