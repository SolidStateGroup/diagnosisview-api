package com.solidstategroup.diagnosisview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solidstategroup.diagnosisview.service.CodeSyncService;
import com.solidstategroup.diagnosisview.service.SubscriptionService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Secured API controller, handles main methods.
 */
@RestController
@RequestMapping("/api/admin")
@Log
public class TempAdminApiController {

    private CodeSyncService codeSyncService;
    private SubscriptionService subscriptionService;

    /**
     * Instantiate API controller, includes required services.
     *
     * @param codeSyncService     CodeSync service
     * @param subscriptionService Subscription service
     */
    @Autowired
    public TempAdminApiController(final CodeSyncService codeSyncService,
                                  final SubscriptionService subscriptionService) {
        this.codeSyncService = codeSyncService;
        this.subscriptionService = subscriptionService;
    }


    /**
     * Sync the content from PV
     *
     * @throws Exception
     */
    @RequestMapping(value = "/sync-codes", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void syncContent() {

        codeSyncService.syncCodes();
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
