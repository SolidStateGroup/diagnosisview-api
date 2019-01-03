package com.solidstategroup.diagnosisview.repository;

import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.codes.LogoRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * JPA repository for Link objects.
 */
@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {

    Set<Link> findLinksByLinkContaining(String message);
}
