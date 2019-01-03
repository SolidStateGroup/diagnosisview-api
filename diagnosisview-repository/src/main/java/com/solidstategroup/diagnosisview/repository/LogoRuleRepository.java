package com.solidstategroup.diagnosisview.repository;

import com.solidstategroup.diagnosisview.model.codes.LogoRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for LogoRule objects.
 */
@Repository
public interface LogoRuleRepository extends JpaRepository<LogoRule, String> {

    @Modifying
    @Query(value = "UPDATE Link l set l.logoRule = NULL WHERE l.logoRule = ?1")
    void clearLogoRule(LogoRule logoRule);

    @Modifying
    @Query(value = "UPDATE Link l SET l.logoRule = :logoRule WHERE EXISTS " +
            "(SELECT ol FROM Link ol WHERE ol.link LIKE CONCAT('%', :#{#logoRule.startsWith}, '%'))")
    void addLogoRule(@Param("logoRule") LogoRule logoRule);
}
