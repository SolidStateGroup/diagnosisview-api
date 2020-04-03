package com.solidstategroup.diagnosisview.service;

/**
 * Interface to interact with the syncing Links from external sources.
 */
public interface LinksSyncService {

    /**
     * Calls the BMJ Best Practice API and MedlinePlus API to pull links urls
     * for external website.
     *
     * Uses codes already stored in diagnosis view
     * to make calls to the BMJ and MedlinePlus. Method will call first using SNOMED-CT
     * code standard and then ICD-10 if SNOMED-CT does not return a result.
     */
    void syncLinks();

    /**
     * Calls the BMJ Best Practice API and MedlinePlus API to pull links urls
     * for external website for given Code string.
     *
     * Uses codes already stored in diagnosis view
     * to make calls to the BMJ and MedlinePlus.
     *
     * Method will call first using SNOMED-CT
     * code standard and then ICD-10 if SNOMED-CT does not return a result.
     */
    void syncLinks(String code);
}
