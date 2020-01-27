package com.solidstategroup.diagnosisview.service;

import java.io.IOException;

/**
 * Interface to interact with dashboard users.
 */
public interface CodeSyncService {

    /**
     * Sync single code from PV
     *
     * @param code a code to sync
     * @throws IOException
     */
    void syncCode(String code) throws IOException;
    /**
     * Sync Codes from patientview to diagnosisview
     */
    void syncCodes();

    /**
     * Sync Links from BMJ Best Practice.
     */
    void syncBmjLinks();
}
