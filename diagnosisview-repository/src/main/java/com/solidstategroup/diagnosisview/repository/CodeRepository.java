package com.solidstategroup.diagnosisview.repository;

import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.codes.Code;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for Code objects.
 */
@Repository
public interface CodeRepository extends JpaRepository<Code, Long> {

    /**
     * Find a code by the given code.
     *
     * @param username String the code to lookup
     * @return the found code
     */
    Code findOneByCode(final String username);
}
