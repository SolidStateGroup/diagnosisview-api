package com.solidstategroup.diagnosisview.repository;

import com.solidstategroup.diagnosisview.model.codes.LogoRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for LogoRule objects.
 */
@Repository
public interface LogoRuleRepository extends JpaRepository<LogoRule, String> {
}
