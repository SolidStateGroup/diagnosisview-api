package com.solidstategroup.diagnosisview.model.enums;


/**
 * Type of subscription.
 */
public enum SubscriptionType {
    ANDROID("Android"),
    IOS("iOS"),
    CHARGEBEE("Chargebee");

    private String name;
    SubscriptionType(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
