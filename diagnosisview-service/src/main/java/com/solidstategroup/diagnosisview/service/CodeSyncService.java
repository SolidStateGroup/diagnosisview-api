package com.solidstategroup.diagnosisview.service;

import org.springframework.scheduling.annotation.Scheduled;

/**
 * Interface to interact with dashboard users.
 */
public interface CodeSyncService {

    /**
     * Sync Codes from patientview to diagnosisview
     */
    @Scheduled(cron = "0 9 * * *")
    void syncCodes();

}
