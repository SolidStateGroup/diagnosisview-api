package com.solidstategroup.diagnosisview.repository;

import com.solidstategroup.diagnosisview.model.codes.CodeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for Code Category objects.
 */
@Repository
public interface CodeCategoryRepository extends JpaRepository<CodeCategory, Long> {
}
