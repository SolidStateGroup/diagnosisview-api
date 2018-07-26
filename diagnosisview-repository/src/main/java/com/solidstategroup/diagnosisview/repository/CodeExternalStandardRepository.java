package com.solidstategroup.diagnosisview.repository;

import com.solidstategroup.diagnosisview.model.codes.CodeExternalStandard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for Code External Standard objects.
 */
@Repository
public interface CodeExternalStandardRepository extends JpaRepository<CodeExternalStandard, Long> {
}
