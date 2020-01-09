package com.solidstategroup.diagnosisview.service;

import com.solidstategroup.diagnosisview.model.CategoryDto;
import com.solidstategroup.diagnosisview.model.CodeDto;
import com.solidstategroup.diagnosisview.model.codes.Code;
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
     * Search for Codes by synonyms.
     *
     * @param searchTerm  a synonym value to search for
     * @param institution
     * @return a List code dtos
     */
    List<CodeDto> getCodesBySynonyms(String searchTerm, Institution institution);

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
     * where required
     *
     * @param code - code to update or create
     */
    Code upsert(Code code) throws Exception;

    /**
     * Create or update a Code
     *
     * @param code a code to save
     */
    Code updateCode(Code code);

    Code getByInstitution(String code, Institution institution);
}
