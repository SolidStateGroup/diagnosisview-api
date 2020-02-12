package com.solidstategroup.diagnosisview.service.impl;


import com.solidstategroup.diagnosisview.clients.medlineplus.MedlineplusApiClient;
import com.solidstategroup.diagnosisview.clients.medlineplus.MedlineplusResponseJson;
import com.solidstategroup.diagnosisview.exceptions.ResourceNotFoundException;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.CodeExternalStandard;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.codes.Lookup;
import com.solidstategroup.diagnosisview.model.enums.LinkTypes;
import com.solidstategroup.diagnosisview.repository.CodeExternalStandardRepository;
import com.solidstategroup.diagnosisview.repository.CodeRepository;
import com.solidstategroup.diagnosisview.repository.ExternalStandardRepository;
import com.solidstategroup.diagnosisview.repository.LookupRepository;
import com.solidstategroup.diagnosisview.service.LinkService;
import com.solidstategroup.diagnosisview.service.MedlinePlusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * MedlinePlusService implementation
 */
@Slf4j
@Service
public class MedlinePlusServiceImpl implements MedlinePlusService {

    private final CodeExternalStandardRepository codeExternalStandardRepository;
    private final LookupRepository lookupRepository;
    private final CodeRepository codeRepository;
    private final ExternalStandardRepository externalStandardRepository;
    private final LinkService linkService;


    public MedlinePlusServiceImpl(final CodeExternalStandardRepository codeExternalStandardRepository,
                                  final LookupRepository lookupRepository,
                                  final CodeRepository codeRepository,
                                  final ExternalStandardRepository externalStandardRepository,
                                  final LinkService linkService) {
        this.codeExternalStandardRepository = codeExternalStandardRepository;
        this.lookupRepository = lookupRepository;
        this.codeRepository = codeRepository;
        this.externalStandardRepository = externalStandardRepository;
        this.linkService = linkService;
    }


    @Override
    @Transactional
    public void setLink(Code entityCode) {
        try {

            if (entityCode == null) {
                log.error("Missing Code, cannot add Medline Plus link");
                return;
            }

            Set<CodeExternalStandard> codeExternalStandards = new HashSet<>();
            if (!CollectionUtils.isEmpty(entityCode.getExternalStandards())) {
                codeExternalStandards = new HashSet<>(entityCode.getExternalStandards());
            }
            // for each code external standard add or update link
            for (CodeExternalStandard codeExternalStandard : codeExternalStandards) {
                codeExternalStandard.setCode(entityCode);

                setCodeExternalStandardLink(entityCode, codeExternalStandard);
            }
        } catch (Exception e) {
            log.error("Failed to add MediaPlus link to Code", e);
        }
    }

    @Override
    @Transactional
    public void setCodeExternalStandardLink(Code entityCode, CodeExternalStandard codeExternalEntity) {
        try {

            if (codeExternalEntity == null || entityCode == null) {
                log.error("Missing CodeExternalStandard or Code, cannot add Medline Plus link");
                return;
            }

            Date now = new Date();
            Link existingLink = null;

            // check Link exists already with Medline Plus type
            for (Link link : entityCode.getLinks()) {
                if (link.getLinkType() != null && LinkTypes.MEDLINE_PLUS.id() == link.getLinkType().getId()) {
                    existingLink = link;
                }
            }

            /**
             * Need to check what system to use to query the link ICD-10 or SNOMED-CT.
             * Will bring the same link url though, but still nice to have support
             *
             * Defaults to ICD-10
             */
            MedlineplusApiClient.CodeSystem codeSystem = MedlineplusApiClient.CodeSystem.ICD_10_CM;
            if (MedlineplusApiClient.CodeSystem.SNOMED_CT.nameCode().equals(
                    codeExternalEntity.getExternalStandard().getName())) {
                codeSystem = MedlineplusApiClient.CodeSystem.SNOMED_CT;
            }

            MedlineplusApiClient apiClient = MedlineplusApiClient
                    .newBuilder()
                    .setCodeSystem(codeSystem)
                    .build();
            MedlineplusResponseJson json = apiClient.getLink(codeExternalEntity.getCodeString());

            String linkUrl = null;

            // Deep down in json, need to check all the bits before getting url
            if (json.getFeed() != null
                    && json.getFeed().getEntry() != null
                    && json.getFeed().getEntry().length > 0
                    && json.getFeed().getEntry()[0].getLink().length > 0) {

                linkUrl = json.getFeed().getEntry()[0].getLink()[0].getHref();

                // should have them already configured
                if (existingLink == null) {
                    Lookup linkType = lookupRepository.findById(LinkTypes.MEDLINE_PLUS.id())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("Could not find MEDLINE_PLUS link type Lookup"));

                    // no medline plus link exist create one Link
                    Link medlinePlusLink = new Link();

                    medlinePlusLink.setLinkType(linkType);
                    medlinePlusLink.setLink(linkUrl);
                    medlinePlusLink.setName(linkType.getDescription());
                    medlinePlusLink.setCode(entityCode);
                    medlinePlusLink.setCreator(null);
                    medlinePlusLink.setCreated(now);
                    medlinePlusLink.setLastUpdater(null);
                    medlinePlusLink.setLastUpdate(medlinePlusLink.getCreated());

                    if (entityCode.getLinks().isEmpty()) {
                        medlinePlusLink.setDisplayOrder(1);
                    } else {
                        medlinePlusLink.setDisplayOrder(2);
                    }

                    entityCode.getLinks().add(medlinePlusLink);
                    entityCode.setLastUpdater(null);
                } else {
                    // update existing MedlineLink link
                    existingLink.setLink(linkUrl);
                    existingLink.setLastUpdater(null);
                    existingLink.setLastUpdate(now);
                }
                log.info("Done medline plus link for code {}", codeExternalEntity.getCodeString());
            } else {
                log.error("Could not find medline plus url for {}", codeExternalEntity.getCodeString());
            }

            entityCode.setLastUpdate(now);
            codeRepository.save(entityCode);

            //linkService.reorderLinks(entityCode.getCode());

        } catch (Exception e) {
            log.error("Failed to add MediaPlus link to Code", e);
        }
    }

}