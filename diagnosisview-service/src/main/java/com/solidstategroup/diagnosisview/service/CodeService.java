package com.solidstategroup.diagnosisview.service;

import com.solidstategroup.diagnosisview.model.CategoryDto;
import com.solidstategroup.diagnosisview.model.CodeDto;
import com.solidstategroup.diagnosisview.model.codes.Code;

import java.util.List;

/**
 * Interface to interact with dashboard users.
 */
public interface CodeService {

    /**
     * Get All categories
     * @return List category dtos
     */
    List<CategoryDto> getAllCategories();

    /**
     * Get All codes
     * @return List code dtos
     */
    List<CodeDto> getAllCodes();

    /**
     * Get a code by a given code
     * @param code the code to lookup
     * @return rhw found code
     */
    Code getCode(String code);

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
