package com.solidstategroup.diagnosisview.repository;

import com.solidstategroup.diagnosisview.model.codes.LinkRuleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRuleMappingRepository extends JpaRepository<LinkRuleMapping, String> {
}
