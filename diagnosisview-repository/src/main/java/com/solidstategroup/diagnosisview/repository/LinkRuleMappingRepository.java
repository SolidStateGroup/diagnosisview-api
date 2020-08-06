package com.solidstategroup.diagnosisview.repository;

import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.codes.LinkRule;
import com.solidstategroup.diagnosisview.model.codes.LinkRuleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkRuleMappingRepository extends JpaRepository<LinkRuleMapping, String> {

    @Query("SELECT r FROM LinkRuleMapping r " +
            " WHERE r.rule = :rule AND r.link = :link")
    Optional<LinkRuleMapping> findByRuleAndLink(@Param("rule") LinkRule rule, @Param("link") Link link);
}
