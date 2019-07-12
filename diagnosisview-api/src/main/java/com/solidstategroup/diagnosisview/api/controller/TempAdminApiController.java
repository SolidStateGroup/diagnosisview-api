package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.service.BmjBestPractices;
import com.solidstategroup.diagnosisview.service.CodeSyncService;
import com.solidstategroup.diagnosisview.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/admin")
public class TempAdminApiController {

    private final BmjBestPractices bmjBestPractices;
    private final CodeSyncService codeSyncService;
    private final SubscriptionService subscriptionService;

    /**
     * Instantiate API controller, includes required services.
     *
     * @param codeSyncService     CodeSync service
     * @param subscriptionService Subscription service
     */
    @Autowired
    public TempAdminApiController(final BmjBestPractices bmjBestPractices,
                                  final CodeSyncService codeSyncService,
                                  final SubscriptionService subscriptionService) {

        this.bmjBestPractices = bmjBestPractices;
        this.codeSyncService = codeSyncService;
        this.subscriptionService = subscriptionService;
    }

    /**
     * Sync the content from PV
     *
     * @throws Exception
     */
    @GetMapping(value = "/sync-codes")
    public void syncContent() {

        codeSyncService.syncCodes();
    }

    @GetMapping(value = "/sync-bmj")
    public void syncBmjLinks() {

        bmjBestPractices.syncBmjLinks();
    }

    /**
     * Sync the content from PV
     *
     * @throws Exception
     */
    @RequestMapping(value = "/android-test", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void checkAndroid()
            throws Exception {

        subscriptionService.checkSubscriptions();
    }
}
