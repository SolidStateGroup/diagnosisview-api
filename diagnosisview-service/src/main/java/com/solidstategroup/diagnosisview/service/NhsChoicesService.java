package com.solidstategroup.diagnosisview.service;

import com.solidstategroup.diagnosisview.exceptions.ImportResourceException;
import com.solidstategroup.diagnosisview.exceptions.ResourceNotFoundException;

/**
 * NHS Choices service, for retrieving data from NHS Choices.
 *
 * @author P Maksymchuk
 * @created on 06/02/2020
 */
public interface NhsChoicesService {

    /**
     * Step 1 of update DV Condition from NHS Choices.
     * Reads data from API and stores each condition as NhschoicesCondition.
     * Will create new NhschoicesConditions and delete from PV if no longer found in API.
     *
     * @throws ImportResourceException
     */
    void updateConditionsFromNhsChoices() throws ImportResourceException;

    /**
     * Step 2 of update PV Codes, synchronises NhschoicesConditions with Codes.
     * If an NhschoicesCondition has been deleted, marks Code as externallyRemoved = true.
     *
     * @throws ResourceNotFoundException
     */
    void syncConditionsWithCodes() throws ResourceNotFoundException;

}
