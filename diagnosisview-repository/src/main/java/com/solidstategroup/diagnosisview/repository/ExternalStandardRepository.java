package com.solidstategroup.diagnosisview.repository;

import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.codes.ExternalStandard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for External Standard objects.
 */
@Repository
public interface ExternalStandardRepository extends JpaRepository<ExternalStandard, Long> {
}
