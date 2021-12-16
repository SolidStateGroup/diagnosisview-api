package com.solidstategroup.diagnosisview.model.enums;


/**
 * Type of payment.
 */
public enum PaymentType {
    ANDROID("Android"),
    IOS("iOS"),
    CHARGEBEE("Chargebee");

    private final String name;
    PaymentType(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
