package com.solidstategroup.diagnosisview.repository;

import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * JPA repository for Link objects.
 */
@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {

    Set<Link> findLinksByLinkContaining(String message);

    void deleteByCode(Code code);
}
