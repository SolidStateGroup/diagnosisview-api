package com.solidstategroup.diagnosisview.service;

import com.solidstategroup.diagnosisview.model.CategoryDto;
import com.solidstategroup.diagnosisview.model.CodeDto;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.enums.Institution;

import java.util.List;

/**
 * Interface to interact with codes.
 */
public interface CodeService {

    /**
     * Get All categories
     *
     * @return List category dtos
     */
    List<CategoryDto> getAllCategories();

    /**
     * Fetches all save codes.
     *
     * @return List {@link Code}
     */
    List<Code> getAll();

    /**
     * Get All codes
     *
     * @return List code dtos
     */
    List<CodeDto> getAll(Institution institution);

    /**
     * Get a code by a given code
     *
     * @param code the code to lookup
     * @return the full found code
     */
    Code get(String code);

    /**
     * Delete a code from the db
     *
     * @param code the code to delete
     */
    void delete(Code code);

    /**
     * Save a code, either creating or deleting
     *
     * @param code the code to update
     * @return Save the saved code
     */
    Code save(Code code);

    /**
     * Create or update a code, creating all the pre-requestite categories, external standards etc
     * where required.
     *
     * Used when creating or updating Code from the DV Web.
     *
     * @param code - code to update or create
     */
    Code upsert(Code code) throws Exception;

    /**
     * Create or update a Code from syn job.
     *
     * Should be used with sync only as has sync specific logic.
     * Use upsert(Code) for DV Web code update
     *
     * @param code a code to save
     */
    Code updateCodeFromSync(Code code);

    Code getByInstitution(String code, Institution institution);
}
