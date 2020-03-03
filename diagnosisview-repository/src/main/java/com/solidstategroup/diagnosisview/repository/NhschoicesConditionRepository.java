package com.solidstategroup.diagnosisview.repository;

import com.solidstategroup.diagnosisview.model.NhschoicesCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * JPA repository for NhschoicesCondition entity.
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface NhschoicesConditionRepository extends JpaRepository<NhschoicesCondition, Long> {

    @Query("SELECT c FROM NhschoicesCondition c WHERE c.code = :code")
    NhschoicesCondition findOneByCode(@Param("code") String code);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM NhschoicesCondition WHERE code IN :codes")
    void deleteByCode(@Param("codes") List<String> codes);
}
