package com.solidstategroup.diagnosisview.repository;

import com.solidstategroup.diagnosisview.model.codes.Lookup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for Lookup objects.
 */
@Repository
public interface LookupRepository extends JpaRepository<Lookup, Long> {
}
