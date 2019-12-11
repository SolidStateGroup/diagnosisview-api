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
        "WHERE UPPER(c.code) LIKE UPPER(:code) ")
    List<Code> findByCode(@Param("code") String code);

    boolean existsByCode(String code);
}
