package com.solidstategroup.diagnosisview.jobs;

import com.solidstategroup.diagnosisview.service.LinksSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Scheduled tasks to retrieve data from BMJ and MedlinePlus external services
 * and sync them into Code links based on external standards codes.
 */
@Slf4j
@Component
public class LinksSyncTask {

    private final LinksSyncService linksSyncService;

    @Autowired
    public LinksSyncTask(final LinksSyncService linksSyncService) {
        this.linksSyncService = linksSyncService;
    }

    @Scheduled(cron = "${cron.job.sync.bmj.links}")
    public void syncAndUpdateCodes() {
        long start = System.currentTimeMillis();
        final UUID correlation = UUID.randomUUID();

        log.info("Starting Links Sync task. Correlation id: {} ", correlation);

        try {
            linksSyncService.syncLinks();
        } catch (Exception e) {
            log.error("Correlation id: {}. Links sync job threw an exception: {}", correlation, e);
        }

        long stop = System.currentTimeMillis();
        log.info("TIMING Links Sync took {}", (stop - start));
    }
}
