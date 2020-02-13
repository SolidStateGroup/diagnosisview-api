package com.solidstategroup.diagnosisview.repository;

import com.solidstategroup.diagnosisview.model.codes.Lookup;
import com.solidstategroup.diagnosisview.model.codes.LookupTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for Lookup objects.
 */
@Repository
public interface LookupRepository extends JpaRepository<Lookup, Long> {

    /**
     * Find a lookup value by the given value
     *
     * @param value - the lookup value
     * @return the found Lookup
     */
    Lookup findOneByValue(final String value);

    @Query("SELECT loo FROM Lookup loo " +
            " WHERE loo.lookupType.type = :lookupType AND loo.value = :lookupValue")
    Lookup findByTypeAndValue(@Param("lookupType") LookupTypes lookupType,
                              @Param("lookupValue") String lookupValue);

}
