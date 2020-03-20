package com.solidstategroup.diagnosisview.jobs;

import com.solidstategroup.diagnosisview.service.NhsChoicesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks to retrieve data from NHS Choices API, currently just
 * conditions which are synchronised with Codes.
 *
 * This will also update Codes in DV.
 */
@Slf4j
@Component
public class NhsChoicesTask {

    private final NhsChoicesService nhsChoicesService;

    @Autowired
    public NhsChoicesTask(NhsChoicesService nhsChoicesService) {
        this.nhsChoicesService = nhsChoicesService;
    }

    //@Scheduled(cron = "0 */3 * * * ?") // every 3 minutes
    @Scheduled(cron = "${cron.job.sync.nhschoices}")
    public void syncAndUpdateCodes() {
        long start = System.currentTimeMillis();

        log.info("Starting sync NHS Choices condition and update Codes task");

        try {
            // update NhschoicesConditions from NHS Choices API
            nhsChoicesService.updateConditionsFromNhsChoices();

            // updated Codes with NhschoicesConditions
            nhsChoicesService.syncConditionsWithCodes();

        } catch (Exception e) {
            log.error("Error updating from NHS Choices: " + e.getMessage(), e);
        }
        long stop = System.currentTimeMillis();
        log.info("TIMING Update NHS Choices and Codes update took {}", (stop - start));
    }
}
