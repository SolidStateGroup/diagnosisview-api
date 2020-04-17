package com.solidstategroup.diagnosisview.service;

import com.solidstategroup.diagnosisview.exceptions.ResourceNotFoundException;
import com.solidstategroup.diagnosisview.model.CategoryDto;
import com.solidstategroup.diagnosisview.model.CodeDto;
import com.solidstategroup.diagnosisview.model.codes.Code;

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
     * Get All codes for given Institution code
     *
     * @param institutionCode a code for Institution
     * @return List code dtos
     */
    List<CodeDto> getAll(String institutionCode) throws ResourceNotFoundException;


    /**
     * Get All active codes, has not been removed externally and not hidden from user. Used for mobile app.
     *
     * @param institutionCode a code for Institution
     * @return List code dtos
     */
    List<CodeDto> getAllActive(String institutionCode) throws ResourceNotFoundException;

    /**
     * Search for Codes by synonyms.
     *
     * @param searchTerm      a synonym value to search for
     * @param institutionCode a code for Institution
     * @return a List code dtos
     */
    List<CodeDto> getCodesBySynonyms(String searchTerm, String institutionCode) throws ResourceNotFoundException;

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
     * Creates a code, creating all the pre-requisite categories, external standards etc where required.
     * <p>
     * Used when creating Code from the DV Web, will prefix code with dv_
     *
     * @param code - code to create
     */
    Code add(Code code) throws Exception;

    /**
     * Update a code, updating/creating all the pre-requisite categories, external standards etc where required.
     * <p>
     * Used when updating Code from the DV Web. Both DV and Other code types can be updated.
     *
     * @param code - code to update
     */
    Code update(Code code) throws Exception;

    /**
     * Updates synonyms for a Code from DV Web, by overriding old ones with new list.
     *
     * @param code - code to update synonyms for
     */
    Code updateCodeSynonyms(Code code) throws Exception;

    /**
     * Create or update a Code from syn job.
     * <p>
     * Should be used with sync only as has sync specific logic. Use upsert(Code) for DV Web code update
     *
     * @param code a code to save
     */
    Code updateCodeFromSync(Code code);

    Code getByInstitution(String code, String institutionCode) throws ResourceNotFoundException;
}
