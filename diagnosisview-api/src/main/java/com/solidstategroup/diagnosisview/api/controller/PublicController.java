package com.solidstategroup.diagnosisview.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
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
}
