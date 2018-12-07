package com.solidstategroup.diagnosisview.repository;

import com.solidstategroup.diagnosisview.model.codes.LinkLogoRule;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for LinkLogoRule objects.
 */
@Repository
public interface LinkLogoRuleRepository extends CrudRepository<LinkLogoRule, String> {
}
