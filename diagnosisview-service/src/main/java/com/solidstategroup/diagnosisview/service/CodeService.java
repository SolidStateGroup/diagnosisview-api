package com.solidstategroup.diagnosisview.service;

import com.solidstategroup.diagnosisview.model.CategoryDto;
import com.solidstategroup.diagnosisview.model.CodeDto;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.codes.enums.Institution;

import java.util.List;

/**
 * Interface to interact with dashboard users.
 */
public interface CodeService {

    /**
     * Get All categories
     *
     * @return List category dtos
     */
    List<CategoryDto> getAllCategories();

    /**
     * Get All codes
     *
     * @return List code dtos
     */
    List<CodeDto> getAllCodes(Institution institution);

    /**
     * Get a code by a given code
     *
     * @param code the code to lookup
     * @return the full found code
     */
    Code getCode(String code);

    /**
     * Delete a code from the db
     *
     * @param code the code to delete
     */
    void delete(Code code);

    /**
     * Save a code, either creating or deleting
     *
     * @param code the code to save
     * @return Save the saved code
     */
    Code save(Code code);

    /**
     * Create or update a code, creating all the pre-requestite categories, external standards etc
     * where required
     *
     * @param code - code to update or create
     * @param fromSync - If from the sync job, we will allow creation of certain other resources
     */
    Code upsertCode(Code code, boolean fromSync);
}
