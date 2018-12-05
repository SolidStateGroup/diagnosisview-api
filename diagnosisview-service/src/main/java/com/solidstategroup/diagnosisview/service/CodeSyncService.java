package com.solidstategroup.diagnosisview.service;

/**
 * Interface to interact with dashboard users.
 */
public interface CodeSyncService {

    /**
     * Sync Codes from patientview to diagnosisview
     */
    void syncCodes();

}
