package com.solidstategroup.diagnosisview.service;

import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.CodeExternalStandard;

/**
 * MedlinePlus service, for retrieving data from MedlinePlus webservice
 */
public interface MedlinePlusService {
    /**
     * Sets a Link for all the CodeExternalStandard for the Code
     *
     * @param entityCode
     */
    void setLink(Code entityCode);

    /**
     * Sets a MedlinPlus Link for CodeExternalStandard
     *
     * @param entityCode
     * @param codeExternalEntity
     */
    boolean processLink(Code entityCode, CodeExternalStandard codeExternalEntity);
}
