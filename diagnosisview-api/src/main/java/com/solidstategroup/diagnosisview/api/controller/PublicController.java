package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.model.codes.enums.DifficultyLevel;
import com.solidstategroup.diagnosisview.model.codes.enums.Institution;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* Public controller, not secured with SAML/ADFS etc.
*/
@RestController
@RequestMapping("/public")
public class PublicController {

    /**
    * Get status of API, used as health check by monitoring applications.
    * @return Map of response
    */
    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public Map<String, String> status() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        return response;
    }

    /**
     * Retrieves different configuration settings for the application.
     *
     * Used by FE.
     *
     * @return Map of configuration settings
     */
    @GetMapping(value = "/settings", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<?>> settings() {
        return new HashMap<String, List<?>>() {{
            put("institutions", Arrays.asList(Institution.UNIVERSITY_OF_EDINBURGH,
                    Institution.NHS_SCOTLAND_KNOWLEDGE_NETWORK,
                    Institution.UNIVERSITY_OF_MALAWI,
                    Institution.OTHER,
                    Institution.NONE));
            put("difficultyLevels", Arrays.asList(DifficultyLevel.DO_NOT_OVERRIDE,
                    DifficultyLevel.GREEN,
                    DifficultyLevel.AMBER,
                    DifficultyLevel.RED));
        }};
    }
}
