package com.solidstategroup.diagnosisview.service;

import org.springframework.scheduling.annotation.Scheduled;

/**
 * Subscription service to check renewable subscriptions
 */
public interface SubscriptionService {

    /**
     * Daily check of subscriptions that are ending in the next month
     */
    @Scheduled(cron = "0 15 * * *")
    void checkSubscriptions() throws Exception;

}
