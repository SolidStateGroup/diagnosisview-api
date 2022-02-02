package com.solidstategroup.diagnosisview.jobs;

import com.solidstategroup.diagnosisview.service.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks to check for expired user subscriptions.
 */
@Slf4j
@Component
public class SubscriptionTask {

  private final SubscriptionService subscriptionService;

  public SubscriptionTask(final SubscriptionService subscriptionService) {
    this.subscriptionService = subscriptionService;
  }

  /**
   * Daily check of subscriptions that are ending in the next month.
   *
   * @throws Exception
   */
  @Scheduled(cron = "0 15 17 * * *")
  public void checkSubscriptions() throws Exception {
    log.info("Checking subscriptions....");
    subscriptionService.checkSubscriptions();
  }
}
