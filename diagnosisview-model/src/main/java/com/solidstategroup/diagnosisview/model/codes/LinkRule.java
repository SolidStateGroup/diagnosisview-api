package com.solidstategroup.diagnosisview.model.codes;

import com.solidstategroup.diagnosisview.model.codes.enums.Institution;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dv_link_rules")
public class LinkRule {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;

    private String link;
    private String transform;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.REMOVE)
    private Set<LinkRuleMapping> mappings;

    @Enumerated(EnumType.STRING)
    private Institution institution;
}
