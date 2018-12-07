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
     * Create or update a code, creating all the pre-requestite categories, external standards etc
     * where required
     *
     * @param code - code to update or create
     * @param fromSync - If from the sync job, we will allow creation of certain other resources
     */
    Code createOrUpdateCode(Code code, boolean fromSync);

    /**
     * Get All categories
     * @return List category dtos
     */
    List<CategoryDto> getAllCategories();

    /**
     * Get All codes
     * @return List code dtos
     */
    List<CodeDto> getAllCodes(Institution institution);

    /**
     * Get a code by a given code
     * @param code the code to lookup
     * @return the found code
     */
    Code getCode(String code);


    /**
     * Get a link by the id
     * @param id Id of the code being sent
     * @return rhw found code
     */
    Link getLink(Long id);


    /**
     * Save a link with update fields.
     * @param link the link to save
     * @return the updated link
     */
    Link saveLink(Link link);

    /**
     * Delete a code from the db
     *
     * @param code the code to delete
     */
    void delete(Code code);

    /**
     * Save a code, eithecreating or deleting
     * @param code the code to save
     * @return Save the saved code
     */
    Code save(Code code);


}
