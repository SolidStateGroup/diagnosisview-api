package com.solidstategroup.diagnosisview.service.impl;

import com.solidstategroup.diagnosisview.clients.nhschoices.ConditionLinkJson;
import com.solidstategroup.diagnosisview.clients.nhschoices.NhsChoicesApiClient;
import com.solidstategroup.diagnosisview.exceptions.ImportResourceException;
import com.solidstategroup.diagnosisview.exceptions.ResourceNotFoundException;
import com.solidstategroup.diagnosisview.model.NhschoicesCondition;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.codes.Lookup;
import com.solidstategroup.diagnosisview.model.codes.LookupTypes;
import com.solidstategroup.diagnosisview.model.codes.enums.CodeSourceTypes;
import com.solidstategroup.diagnosisview.model.codes.enums.CodeStandardTypes;
import com.solidstategroup.diagnosisview.model.codes.enums.CodeTypes;
import com.solidstategroup.diagnosisview.model.codes.enums.DifficultyLevel;
import com.solidstategroup.diagnosisview.model.enums.LinkTypes;
import com.solidstategroup.diagnosisview.repository.CodeRepository;
import com.solidstategroup.diagnosisview.repository.LookupRepository;
import com.solidstategroup.diagnosisview.repository.NhschoicesConditionRepository;
import com.solidstategroup.diagnosisview.service.NhsChoicesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NHS Choices service, for retrieving data from NHS Choices.
 *
 * @author P Maksymchuk
 * @created on 06/02/2020
 */
@Slf4j
@Service
public class NhsChoicesServiceImpl implements NhsChoicesService {

    private final NhschoicesConditionRepository nhschoicesConditionRepository;
    private final LookupRepository lookupRepository;
    private final CodeRepository codeRepository;

    private String nhsChoicesApiKey;

    public NhsChoicesServiceImpl(@Value("${nhschoices.conditions.api.key}") String nhsChoicesApiKey,
                                 final NhschoicesConditionRepository nhschoicesConditionRepository,
                                 final LookupRepository lookupRepository,
                                 final CodeRepository codeRepository) {
        this.nhsChoicesApiKey = nhsChoicesApiKey;
        this.nhschoicesConditionRepository = nhschoicesConditionRepository;
        this.lookupRepository = lookupRepository;
        this.codeRepository = codeRepository;
    }

    /**
     * Step 1 of update PV Codes from NHS Choices.
     * Reads data from API and stores each condition as NhschoicesCondition.
     * Will create new NhschoicesConditions and delete from PV if no longer found in API.
     *
     * //@throws ImportResourceException
     */
    @Transactional
    @Override
    public void updateConditionsFromJob() {

    }

    /**
     * Step 2 of update PV Codes, synchronises NhschoicesConditions with Codes in DV.
     * If an NhschoicesCondition has been deleted, marks Code as externallyRemoved = true.
     *
     * //@throws ResourceNotFoundException
     */
    @Override
    public void syncConditionsWithCodes() throws ResourceNotFoundException {

        // synchronise conditions previously retrieved from nhs choices, may be consolidated into once function call
        Lookup standardType = lookupRepository.findByTypeAndValue(
                LookupTypes.CODE_STANDARD, CodeStandardTypes.NHS_CHOICES.toString());
        if (standardType == null) {
            throw new ResourceNotFoundException("Could not find PATIENTVIEW code standard type Lookup");
        }
        Lookup codeType = lookupRepository.findByTypeAndValue(LookupTypes.CODE_TYPE, CodeTypes.DIAGNOSIS.toString());
        if (codeType == null) {
            throw new ResourceNotFoundException("Could not find DIAGNOSIS code type Lookup");
        }

        // set creator to importer if not called by a user from endpoint (e.g. run as task)
//        User currentUser = getCurrentUser();
//        if (currentUser == null) {
//            currentUser = userRepository.findByUsernameCaseInsensitive("importer");
//        }

        // get codes and conditions to synchronise, finding all PATIENTVIEW Codes and all NhschoicesCondition
        List<Code> currentCodes = codeRepository.findAllByStandardType(standardType);
        List<NhschoicesCondition> conditions = nhschoicesConditionRepository.findAll();

        // map to store current Codes
        Map<String, Code> currentCodesMap = new HashMap<>();
        for (Code code : currentCodes) {
            currentCodesMap.put(code.getCode(), code);
        }

        Set<Code> codesToSave = new HashSet<>();
        List<String> newOrUpdatedCodes = new ArrayList<>();

        log.info("Synchronising " + conditions.size() + " NhschoicesConditions with " + currentCodes.size()
                + " PATIENTVIEW standard type Codes");

        // iterate through all NHS Choices conditions
        for (NhschoicesCondition condition : conditions) {
            newOrUpdatedCodes.add(condition.getCode());

            // check if Code with same code as NhschoicesCondition exists in PV already
            if (currentCodesMap.keySet().contains(condition.getCode())) {

                Code currentCode = currentCodesMap.get(condition.getCode());
                boolean saveCurrentCode = false;

                // revert removed externally if set
                if (currentCode.isRemovedExternally()) {
                    currentCode.setRemovedExternally(false);
                    saveCurrentCode = true;
                }

                // if no patient friendly name then update with condition name
                if (StringUtils.isEmpty(currentCode.getPatientFriendlyName())) {
                    currentCode.setPatientFriendlyName(condition.getName());
                    saveCurrentCode = true;
                }

                // if no description set, update it with condition description
                if (StringUtils.isEmpty(currentCode.getFullDescription())) {
                    currentCode.setFullDescription(condition.getDescription());
                    saveCurrentCode = true;
                }

                // update description with condition description
                if (!StringUtils.isEmpty(currentCode.getDescription())) {
                    currentCode.setDescription(condition.getName());
                    saveCurrentCode = true;
                }

                // if changed, then save
                if (saveCurrentCode) {
                    currentCode.setLastUpdater(null);
                    currentCode.setLastUpdate(new Date());
                    codesToSave.add(currentCode);
                }
            } else {
                // NhschoicesCondition is new, create and save new Code
                Code code = new Code();
                code.setCreator(null);
                code.setCreated(new Date());
                code.setLastUpdater(null);
                code.setLastUpdate(new Date());
                code.setCode(condition.getCode());
                code.setCodeType(codeType);
                code.setSourceType(CodeSourceTypes.NHS_CHOICES);
                code.setStandardType(standardType);
                code.setDescription(condition.getName());
                code.setFullDescription(condition.getDescription());
                code.setPatientFriendlyName(condition.getName());

                // add Link to new Code if introduction URL is present on NhschoiceCondition
                if (StringUtils.isNotEmpty(condition.getIntroductionUrl())) {
                    Link nhschoicesLink = new Link();

                    // should have them already configured
                    Lookup linkType = lookupRepository.findById(LinkTypes.NHS_CHOICES.id())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("Could not find NHS CHOICES link type Lookup"));

                    nhschoicesLink.setLinkType(linkType);
                    nhschoicesLink.setLink(condition.getIntroductionUrl());
                    nhschoicesLink.setName(linkType.getDescription());
                    nhschoicesLink.setDifficultyLevel(DifficultyLevel.GREEN);
                    nhschoicesLink.setCode(code);
                    nhschoicesLink.setCreator(null);
                    nhschoicesLink.setCreated(code.getCreated());
                    nhschoicesLink.setLastUpdater(null);
                    nhschoicesLink.setLastUpdate(code.getCreated());
                    nhschoicesLink.setDisplayOrder(1);


                    //code.getLinks().add(nhschoicesLink);

                    // add new links, sets correct display order and persist it
                    Link saved = linkService.addExternalLink(medlinePlusLink, entityCode);
                    code.addLink(saved);
                    codeService.save(code);

                    /**
                     * Add or Update Medline Plus link as well if needed
                     */
                    medlinePlusService.setLink(code);
                }

                codesToSave.add(code);
            }
        }

        // handle PATIENTVIEW codes that are no longer in NHS Choices Condition list, mark as removed externally
        for (Code code : currentCodes) {
            if (!newOrUpdatedCodes.contains(code.getCode())) {
                // Code has been removed externally
                code.setRemovedExternally(true);
                code.setLastUpdate(new Date());
                code.setLastUpdater(null);
                codesToSave.add(code);
            }
        }

        if (!codesToSave.isEmpty()) {
            codeRepository.saveAll(codesToSave);
        }

        log.info("Finished synchronising " + conditions.size() + " NhschoicesConditions with " + currentCodes.size()
                + " PATIENTVIEW standard type Codes.");
    }


    private void updateConditionsWorker() throws ImportResourceException {
        log.info("START Update NhschoicesCondition process");
        long start = System.currentTimeMillis();

        // need to use different api key for each service
//        String apiKey = properties.getProperty("nhschoices.conditions.api.key");

        // contact NHSChoices Conditions API to get all the conditions
        // and transform them into local NhschoicesCondition object
        // we should have enough information to build full object
        NhsChoicesApiClient apiClient = NhsChoicesApiClient.newBuilder()
                .setApiKey(nhsChoicesApiKey)
                .build();

        List<ConditionLinkJson> allConditions = apiClient.getAllConditions();
        // can be null if we have communication issue
        if (allConditions == null || allConditions.isEmpty()) {
            throw new ImportResourceException("Error reading alphabetical listing of NHS Choices conditions");
        }

        log.info("Found NhschoicesConditions api: " + allConditions.size());

        // compare to existing using uri, adding if required with correct details
        List<NhschoicesCondition> currentConditions = nhschoicesConditionRepository.findAll();
        List<String> currentConditionCodes = new ArrayList<>();
        Map<String, NhschoicesCondition> currentCodeMap = new HashMap<>();

        for (NhschoicesCondition currentCondition : currentConditions) {
            currentConditionCodes.add(currentCondition.getCode());
            currentCodeMap.put(currentCondition.getCode(), currentCondition);
        }

        List<String> newConditionCodes = new ArrayList<>();
        Date now = new Date();
        for (ConditionLinkJson condition : allConditions) {
            String conditionCode = getConditionCodeFromUri(condition.getApiUrl());
            newConditionCodes.add(conditionCode);
            // build condition public url from api url
            String conditionUrl = buildUrlFromApiUrl(condition.getApiUrl());

            if (!currentCodeMap.keySet().contains(conditionCode)) {
                // found new condition, populate all the details
                // TODO: need to set next id
                NhschoicesCondition newCondition = new NhschoicesCondition();
                newCondition.setCode(conditionCode);
                newCondition.setName(condition.getName());
                newCondition.setDescription(condition.getDescription());
                newCondition.setDescriptionLastUpdateDate(now);

                // check if public url accessible accessible
                Integer status = getUrlStatus(conditionUrl);
                if (status != null && status.equals(200)) {
                    newCondition.setIntroductionUrl(conditionUrl);
                } else {
                    // 404, 403 or otherwise, remove introduction url
                    newCondition.setIntroductionUrl(null);
                }
                newCondition.setIntroductionUrlStatus(status);
                newCondition.setIntroductionUrlLastUpdateDate(now);

                newCondition.setUri(condition.getApiUrl());
                newCondition.setCreator(null);
                newCondition.setCreated(now);
                newCondition.setLastUpdate(newCondition.getCreated());
                newCondition.setLastUpdater(null);

                nhschoicesConditionRepository.save(newCondition);
            } else {
                // existing entry, update dates for introduction url and description
                NhschoicesCondition existingCondition = currentCodeMap.get(conditionCode);
                if (existingCondition != null) {
                    existingCondition.setName(condition.getName());
                    existingCondition.setIntroductionUrl(conditionUrl);
                    existingCondition.setIntroductionUrlLastUpdateDate(now);
                    existingCondition.setIntroductionUrlStatus(200);
                    existingCondition.setDescription(condition.getDescription());
                    existingCondition.setDescriptionLastUpdateDate(now);
                    existingCondition.setUri(condition.getApiUrl());
                    existingCondition.setLastUpdate(now);
                    existingCondition.setLastUpdater(null);
                    nhschoicesConditionRepository.save(existingCondition);
                }
            }

            // sleep for 1 seconds to avoid too many calls to nhs website
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                throw new ImportResourceException("Thread interrupted");
            }
        }

        // delete old NhschoiceCondition, no longer on NHS Choices
        currentConditionCodes.removeAll(newConditionCodes);

        if (!currentConditionCodes.isEmpty()) {
            nhschoicesConditionRepository.deleteByCode(currentConditionCodes);
        }

        long stop = System.currentTimeMillis();
        log.info("TIMING Update NhschoicesCondition " + (stop - start) + " process ");
    }

    private String getConditionCodeFromUri(String uri) {
        return uri.split("/")[uri.split("/").length - 1];
    }

    /**
     * Helper to hit given url and get the status back
     *
     * @param url
     * @return
     */
    private Integer getUrlStatus(String url) {
        try {
            HttpURLConnection huc = (HttpURLConnection) new URL(url).openConnection();
            huc.setRequestMethod("HEAD");  //OR  huc.setRequestMethod ("GET");
            huc.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; "
                    + ".NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 1.2.30703)");
            huc.connect();
            return huc.getResponseCode();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helper to transform condition api url into nhs website url.
     * With introduction of API v2 NHSChoices website is more consistent with the data
     * so api url (https://api.nhs.uk/conditions/{condition}/") for condition should be
     * equivalent to nhs website url (https://www.nhs.uk/conditions/{condition}/
     *
     * @param apiUrl an api url for condition
     * @return a NHS url for condition
     */
    private String buildUrlFromApiUrl(String apiUrl) {
        return apiUrl.replace("api.nhs.uk", "www.nhs.uk");
    }

}
