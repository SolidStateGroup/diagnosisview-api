package com.solidstategroup.diagnosisview.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

/**
 * A client for 3rd party service to search for Diagnosis by synonyms.
 *
 * Created by Pavlo Maksymchuk.
 */
@Slf4j
@Service
public class SynonymsService {


    private static final String SYNONYMS_URL = "https://clinicaltables.nlm.nih.gov/api/conditions/v3/search?" +
            "terms=%s" +
            "&maxList=20" +
            "&ef=icd10cm_codes,primary_name" +
            "&sf=primary_name,consumer_name,key_id,word_synonyms,synonyms";
    private static RestTemplate template = new RestTemplate();

    public SynonymsService() {
    }

    /**
     * Search for diagnosis/synonyms for given search parameter
     *
     * @param searchTerm a term to search for
     * @return a list of external standards icd10 codes
     */
    public Set<String> searchSynonyms(String searchTerm) {

        log.info("Starting synonyms search");
        long start = System.currentTimeMillis();

        Set<String> codes = new HashSet<>();
        ResponseEntity<ArrayNode> entity;

        try {
            String url = buildUrl(searchTerm);
            entity = template.getForEntity(url, ArrayNode.class);

            if (!entity.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to search synonyms, terms {}", entity.getStatusCode());
                return codes;
            }

            JsonNode entry = entity.getBody().findValue("icd10cm_codes");
            if (entry != null && entry.isArray()) {
                for (JsonNode node : entry) {
                    String fullCode = node.asText();
                    if (!StringUtils.isEmpty(fullCode)) {
                        codes.add(fullCode);
                    }
                }
            } else {
                log.info("No codes returned in search synonyms {}", searchTerm);
            }

        } catch (Exception e) {
            log.error("Exception in synonyms search ", e);
        }

        long stop = System.currentTimeMillis();
        log.info("Finished Synonyms search, timing {}", (stop - start));
        return codes;
    }

    private String buildUrl(String term) {
        return String.format(SYNONYMS_URL, term);
    }

}
