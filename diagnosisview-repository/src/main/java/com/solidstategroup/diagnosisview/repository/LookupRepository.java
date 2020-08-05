package com.solidstategroup.diagnosisview.repository;

import com.solidstategroup.diagnosisview.model.codes.Lookup;
import com.solidstategroup.diagnosisview.model.codes.enums.LookupTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    Optional<Lookup> findOneByValue(final String value);

    @Query("SELECT loo FROM Lookup loo " +
            " WHERE loo.lookupType.type = :lookupType AND UPPER(loo.value) = UPPER(:lookupValue)")
    Optional<Lookup> findByTypeAndValue(@Param("lookupType") LookupTypes lookupType,
                                        @Param("lookupValue") String lookupValue);

    @Query("SELECT l FROM Lookup l WHERE l.lookupType.type = :lookupType")
    List<Lookup> findByType(@Param("lookupType") LookupTypes lookupType);

    /**
     * Get a list of user statistics for given institution code
     * @param code an Institution lookup type code
     * @return a list of statistics
     */
    @Query(value = "SELECT " +
            "(SELECT COUNT(*) from dv_user WHERE institution = :code) as users," +
            "(SELECT COUNT(*) from dv_user WHERE institution = :code AND active_subscription = TRUE) as subscriptions",
            nativeQuery = true)
    List<Map<String, Object>> getInstitutionStats(@Param("code") String code);
}
