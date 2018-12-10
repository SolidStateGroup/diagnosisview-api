package com.solidstategroup.diagnosisview.repository;

import com.solidstategroup.diagnosisview.model.codes.LinkLogoRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for LinkLogoRule objects.
 */
@Repository
public interface LinkLogoRuleRepository extends JpaRepository<LinkLogoRule, String> {
}
