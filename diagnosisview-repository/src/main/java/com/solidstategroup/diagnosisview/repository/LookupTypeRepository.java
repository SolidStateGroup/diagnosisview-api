package com.solidstategroup.diagnosisview.repository;

import com.solidstategroup.diagnosisview.model.codes.LookupType;
import com.solidstategroup.diagnosisview.model.codes.enums.LookupTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for LookupType objects.
 */
@Repository
public interface LookupTypeRepository extends JpaRepository<LookupType, Long> {

    /**
     * Find a lookup type by the given value
     *
     * @param type - the type of the lookup type
     * @return the found Lookup
     */
    Optional<LookupType> findOneByType(LookupTypes type);
}
