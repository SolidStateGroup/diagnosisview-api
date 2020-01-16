package com.solidstategroup.diagnosisview.repository;

import com.solidstategroup.diagnosisview.model.codes.Code;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for Code objects.
 */
@Repository
public interface CodeRepository extends JpaRepository<Code, Long> {

    /**
     * Find a code by the given code.
     *
     * @param code String the code to lookup
     * @return the found code
     */
    Code findOneByCode(final String code);

    @Query("SELECT c FROM Code c " +
            " WHERE UPPER(c.code) LIKE UPPER(:code) ")
    List<Code> findByCode(@Param("code") String code);

    @Query("SELECT c FROM Code c " +
            " JOIN c.externalStandards es " +
            " WHERE removed_externally = false AND hide_from_patients = false AND es.codeString LIKE :code")
    List<Code> findByExternalStandards(@Param("code") String code);

    @Query(value = "SELECT * FROM pv_code, " +
            " jsonb_array_elements(synonyms) " +
            " WHERE removed_externally = false AND hide_from_patients = false " +
            " AND UPPER(value->>'name') LIKE UPPER(:synonym)",
            nativeQuery = true)
    List<Code> findBySynonym(@Param("synonym") String synonym);

    boolean existsByCode(String code);

    /**
     * Find all the Code that has not been removed externally and not hidden from patient.
     *
     * @return a list of Code objects
     */
    @Query("SELECT c FROM Code c  " +
            " WHERE c.removedExternally = false AND c.hideFromPatients = false ")
    List<Code> findAllActive();
}
