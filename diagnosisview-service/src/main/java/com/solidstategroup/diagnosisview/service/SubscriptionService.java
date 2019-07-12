package com.solidstategroup.diagnosisview.service;

/**
 * Subscription service to check renewable subscriptions
 */
public interface SubscriptionService {

    /**
     * Daily check of subscriptions that are ending in the next month
     */
    void checkSubscriptions() throws Exception;

}
