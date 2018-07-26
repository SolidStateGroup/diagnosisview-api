package com.solidstategroup.diagnosisview.repository;

import com.solidstategroup.diagnosisview.model.codes.LookupType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for LookupType objects.
 */
@Repository
public interface LookupTypeRepository extends JpaRepository<LookupType, Long> {
}
