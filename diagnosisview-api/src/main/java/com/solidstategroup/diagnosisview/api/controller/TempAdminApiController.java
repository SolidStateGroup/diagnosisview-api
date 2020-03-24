package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.service.BmjBestPractices;
import com.solidstategroup.diagnosisview.service.CodeSyncService;
import com.solidstategroup.diagnosisview.service.NhsChoicesService;
import com.solidstategroup.diagnosisview.service.SubscriptionService;
import com.solidstategroup.diagnosisview.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin")
public class TempAdminApiController extends BaseController {

    private final BmjBestPractices bmjBestPractices;
    private final CodeSyncService codeSyncService;
    private final SubscriptionService subscriptionService;
    private final NhsChoicesService nhsChoicesService;

    /**
     * Instantiate API controller, includes required services.
     *
     * @param codeSyncService     CodeSync service
     * @param subscriptionService Subscription service
     */
    @Autowired
    public TempAdminApiController(final UserService userService,
                                  final BmjBestPractices bmjBestPractices,
                                  final CodeSyncService codeSyncService,
                                  final SubscriptionService subscriptionService,
                                  final NhsChoicesService nhsChoicesService) {
        super(userService);
        this.bmjBestPractices = bmjBestPractices;
        this.codeSyncService = codeSyncService;
        this.subscriptionService = subscriptionService;
        this.nhsChoicesService = nhsChoicesService;
    }


    @GetMapping(value = "/sync-codes/{code}")
    public void syncPvCode(@PathVariable("code") final String code, HttpServletRequest request) throws Exception {
        isAdminUser(request);
        codeSyncService.syncCode(code);
    }

    @GetMapping(value = "/sync-bmj")
    public void syncBmjLinks(HttpServletRequest request) throws Exception {
        isAdminUser(request);
        bmjBestPractices.syncBmjLinks();
    }

    @GetMapping(value = "/sync-bmj/{code}")
    public void syncBmjLinksForCode(@PathVariable("code") final String code,
                                    HttpServletRequest request) throws Exception {
        isAdminUser(request);
        bmjBestPractices.syncBmjLinks(code);
    }

    /**
     * Step 1: sync NHS choices conditions
     *
     * @param request
     * @throws Exception
     */
    @GetMapping(value = "/sync/nhs_choices")
    public void syncNhsChoicesConditions(HttpServletRequest request) throws Exception {
        isAdminUser(request);
        System.out.println("ALL GOOD");
        //nhsChoicesService.updateConditionsFromNhsChoices();
    }

    /**
     * Step 2: Sync the nhs choices conditions with codes
     *
     * @throws Exception
     */
    @GetMapping(value = "/sync/codes")
    public void syncCodes(HttpServletRequest request) throws Exception {
        isAdminUser(request);
        nhsChoicesService.syncConditionsWithCodes();
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
