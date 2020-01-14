package com.solidstategroup.diagnosisview.service;

/**
 * Methods to interact with the BMJ Best Practices.
 */
public interface BmjBestPractices {

    /**
     * Calls the BMJ Best Practice API to pull links urls
     * for BMJ website. Uses codes already stored in diagnosis view
     * to make calls to the BMJ. Method will call first using SNOMED-CT
     * code standard and then ICD-10 if SNOMED-CT does not return a result.
     */
    void syncBmjLinks();

    /**
     * Calls the BMJ Best Practice API to pull links urls
     * for BMJ website for given Code string.
     *
     * Uses codes already stored in diagnosis view
     * to make calls to the BMJ. Method will call first using SNOMED-CT
     * code standard and then ICD-10 if SNOMED-CT does not return a result.
     */
    void syncBmjLinks(String code);
}
