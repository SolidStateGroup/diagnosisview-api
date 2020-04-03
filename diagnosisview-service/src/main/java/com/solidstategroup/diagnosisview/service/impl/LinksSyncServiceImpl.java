package com.solidstategroup.diagnosisview.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.CodeExternalStandard;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.codes.Lookup;
import com.solidstategroup.diagnosisview.model.codes.enums.DifficultyLevel;
import com.solidstategroup.diagnosisview.repository.LookupRepository;
import com.solidstategroup.diagnosisview.service.CodeService;
import com.solidstategroup.diagnosisview.service.LinkService;
import com.solidstategroup.diagnosisview.service.LinksSyncService;
import com.solidstategroup.diagnosisview.service.MedlinePlusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service to handle sync for different types of links from external systems.
 *
 * Currently supporting BMJ and MedlinePlus links. NHS Choice links are created
 * during NhsChoices code sync.
 */
@Slf4j
@Service
public class LinksSyncServiceImpl implements LinksSyncService {

    private static final String BMJ_BP_ENDPOINT_TEMPLATE = "https://bestpractice.bmj.com/infobutton?" +
            "knowledgeResponseType=application/json&" +
            "mainSearchCriteria.v.cs=%s&" +
            "mainSearchCriteria.v.c=%s&" +
            "mainSearchCriteria.v.dn=%s";
    private static final Map<String, String> codeMapping;
    private static final String SNOMED_CT = "SNOMED-CT";
    private static final String ICD_10 = "ICD-10";
    private static final String linkName = "BMJ Best Practice";
    private static RestTemplate template = new RestTemplate();
    private static Integer DEFAULT_BMJ_ORDER = 11;

    static {
        codeMapping = new HashMap<>();
        codeMapping.put(SNOMED_CT, "2.16.840.1.113883.6.96");
        codeMapping.put(ICD_10, "2.16.840.1.113883.6.90");
    }

    private final CodeService codeService;
    private final LinkService linkService;
    private final MedlinePlusService medlinePlusService;

    private final Lookup BMJ;

    public LinksSyncServiceImpl(final CodeService codeService,
                                final LinkService linkService,
                                final LookupRepository lookupRepository,
                                final MedlinePlusService medlinePlusService) {

        this.codeService = codeService;
        this.linkService = linkService;
        this.medlinePlusService = medlinePlusService;

        BMJ = lookupRepository.findOneByValue("BMJ");
    }

    /**
     * {@inheritDoc}
     */
    @CacheEvict(value = {"getAllCodes", "getAllCategories"}, allEntries = true)
    @Override
    public void syncLinks() {

        long start = System.currentTimeMillis();
        List<Code> codes = codeService.getAll();

        log.info("Links Sync Processing {} codes.", codes.size());

        codes.forEach(code -> {

            final Optional<CodeExternalStandard> snomed = getExternalStandard(code, SNOMED_CT);

            boolean foundBmjSnomed = false;
            boolean foundMedlineSnomed = false;

            if (snomed.isPresent()) {

                foundBmjSnomed = processBMJLink(buildUrl(SNOMED_CT, snomed.get().getCodeString(), code.getCode()),
                        code,
                        SNOMED_CT);

                foundMedlineSnomed = medlinePlusService.processLink(code, snomed.get());
            }

            // no links found for SNOMED code, try ICD_10
            if (!foundBmjSnomed) {
                getExternalStandard(code, ICD_10)
                        .ifPresent(icd10 ->
                                processBMJLink(buildUrl(ICD_10, icd10.getCodeString(), code.getCode()), code, ICD_10)
                        );
            }

            if (!foundMedlineSnomed) {
                getExternalStandard(code, ICD_10)
                        .ifPresent(icd10 -> medlinePlusService.processLink(code, icd10));
            }
        });

        long stop = System.currentTimeMillis();
        log.info("Links Sync DONE Processing codes, timing {}.", (stop - start));
    }

    @CacheEvict(value = {"getAllCodes", "getAllCategories"}, allEntries = true)
    @Override
    public void syncLinks(String codeStr) {

        Code code = codeService.get(codeStr);

        log.info("Processing {} code.", code.getCode());

        final Optional<CodeExternalStandard> snomed = getExternalStandard(code, SNOMED_CT);

        boolean foundSnomed = false;
        boolean foundMedlineSnomed = false;

        if (snomed.isPresent()) {
            // process BMJ link
            foundSnomed = processBMJLink(
                    buildUrl(SNOMED_CT, snomed.get().getCodeString(), code.getCode()), code, SNOMED_CT);

            foundMedlineSnomed = medlinePlusService.processLink(code, snomed.get());
        }

        if (!foundSnomed) {
            getExternalStandard(code, ICD_10)
                    .ifPresent(icd10 -> processBMJLink(
                            buildUrl(ICD_10, icd10.getCodeString(), code.getCode()), code, ICD_10)
                    );
        }

        if (!foundMedlineSnomed) {
            getExternalStandard(code, ICD_10)
                    .ifPresent(icd10 -> medlinePlusService.processLink(code, icd10));
        }
    }

    /**
     * Builds a BMJ link. Uses the external code standard mapping to build the link with the correct codes.
     */
    private String buildUrl(String externalStandard, String code, String codeName) {

        return String.format(BMJ_BP_ENDPOINT_TEMPLATE, codeMapping.getOrDefault(externalStandard, SNOMED_CT), code, codeName);
    }

    /**
     * Calls the url and processes the result from the BMJ.
     *
     * NOTE: the BMJ sends an http 500 response with HTML when the code is not found. {@link RestTemplate} is not
     * setup to handle this so will throw an exception. All we are doing here is to capturing that exception and moving
     * on.
     */
    private boolean processBMJLink(String url, Code code, String standard) {

        final UUID correlation = UUID.randomUUID();
        Instant start = Instant.now();

        ResponseEntity<ObjectNode> entity = null;
        try {

            entity = template.getForEntity(url, ObjectNode.class);

            if (!entity.getStatusCode().is2xxSuccessful()) {

                return false;
            }

            JsonNode entry = entity.getBody().findValue("entry");

            if (entry != null && entry.isArray()) {

                for (JsonNode jn : entry) {

                    String id = jn.get("id").get("_value").asText();
                    SimpleDateFormat utcDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    Date dateUpdated = utcDateFormat.parse(jn.get("updated").get("_value").asText());

                    for (JsonNode link : jn.get("link")) {

                        String href = link.get("href").asText();
                        String title = link.get("title").asText();
                        Set<Link> links = code.getLinks();

                        Optional<Link> bmj = links.stream()
                                .filter(l -> l.getLinkType().getValue().equals("BMJ"))
                                .filter(l -> l.getExternalId().equals(id))
                                .findAny();

                        if (bmj.isPresent()) {

                            // we have links

                            if (bmj.get().getLastUpdate().before(dateUpdated)) {

                                // we have an updated link...
                                Link linkToUpdate = new Link();
                                linkToUpdate.setName(linkName);
                                linkToUpdate.setLink(href);
                                linkToUpdate.setExternalId(id);
                                linkService.updateExternalLinks(linkToUpdate);

                                log.info("BMJ Correlation id: {}. Links updated for ext id {}", correlation, id);
                                log.debug("BMJ Correlation id: {}. Time taken: {}", correlation,
                                        Duration.between(start, Instant.now()));
                                return true;
                            }

                        } else {

                            // we have a new link...
                            Link newLink = new Link();
                            newLink.setName(linkName);
                            newLink.setLink(href);
                            newLink.setExternalId(id);
                            newLink.setLinkType(BMJ);
                            newLink.setDisplayOrder(DEFAULT_BMJ_ORDER);
                            newLink.setDifficultyLevel(DifficultyLevel.AMBER);

                            Link saved = linkService.addExternalLink(newLink, code);
                            code.addLink(saved);
                            codeService.save(code);

                            log.info("BMJ Correlation id: {}. New link saved {}", correlation, newLink.getId());
                            log.debug("BMJ Correlation id: {}. Time taken: {}", correlation, Duration.between(start, Instant.now()));

                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception in Bmj.processLink()", e);
            log.debug("Correlation id: {}. Could not get links for {} using standard {} ", correlation, code.getCode(), standard);
            log.error("Exception in Bmj.processLink() Correlation id: {}. Url: {}, Response status code: {}",
                    correlation, url, entity != null ? entity.getStatusCode() : "none returned");
        }

        log.debug("Correlation id: {}. Time taken: {}", correlation, Duration.between(start, Instant.now()));

        return false;
    }

    /**
     * Pulls an external standard from a {@link Code}
     */
    private Optional<CodeExternalStandard> getExternalStandard(Code code, String externalStandard) {
        return code.getExternalStandards()
                .stream()
                .filter(es -> es.getExternalStandard().getName().equals(externalStandard))
                .findAny();
    }
}
