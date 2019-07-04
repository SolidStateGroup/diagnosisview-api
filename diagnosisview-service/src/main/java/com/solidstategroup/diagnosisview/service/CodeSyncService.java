package com.solidstategroup.diagnosisview.service;

import java.io.IOException;

/**
 * Interface to interact with dashboard users.
 */
public interface CodeSyncService {

    /**
     * Sync Codes from patientview to diagnosisview
     */
    void syncCodes();

    /**
     * Calls the BMJ Best Practice API to pull links urls
     * for BMJ website. Uses codes already stored in diagnosis view
     * to make calls to the BMJ. Method will call first using SNOMED-CT
     * code standard and then ICD-10 if SNOMED-CT does not return a result.
     */
    void syncBmjLinks() throws IOException;
}
