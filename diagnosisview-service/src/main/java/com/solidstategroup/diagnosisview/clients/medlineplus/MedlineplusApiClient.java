package com.solidstategroup.diagnosisview.clients.medlineplus;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Api Client implementation for MedlinePlus Connect web services
 * <p>
 * See https://medlineplus.gov/connect/service.html for more information
 */
public final class MedlineplusApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(MedlineplusApiClient.class);

    private CodeSystem codeSystem;
    private String apiUrl;

    private static final String BASE_URL = "https://connect.medlineplus.gov/service";

    private static final String CODE_SYSTEM_PARAM = "mainSearchCriteria.v.cs";
    private static final String RESPONSE_TYPE_PARAM = "knowledgeResponseType";
    private static final String CODE_PARAM = "mainSearchCriteria.v.c";


    private MedlineplusApiClient() {
    }

    public static MedlineplusApiClient.Builder newBuilder() {
        return new MedlineplusApiClient.Builder();
    }

    /**
     * Identifies the problem code system that will be used in request
     * ICD-10-CM  mainSearchCriteria.v.cs=2.16.840.1.113883.6.90
     * ICD-9-CM   mainSearchCriteria.v.cs=2.16.840.1.113883.6.103
     * SNOMED CT  mainSearchCriteria.v.cs=2.16.840.1.113883.6.96
     */
    public enum CodeSystem {
        ICD_10_CM {
            @Override
            public String code() {
                return "2.16.840.1.113883.6.90";
            }

            @Override
            public String nameCode() {
                return "ICD-10";
            }
        },
        ICD_9_CM {
            @Override
            public String code() {
                return "2.16.840.1.113883.6.103";
            }

            @Override
            public String nameCode() {
                return "ICD-9";
            }
        },
        SNOMED_CT {
            @Override
            public String code() {
                return "2.16.840.1.113883.6.96";
            }

            @Override
            public String nameCode() {
                return "SNOMED-CT";
            }
        };

        public abstract String code();

        public abstract String nameCode();
    }

    /**
     * Retrieves link for given code from MedlinePlus web service
     *
     * @param code
     */
    public MedlineplusResponseJson getLink(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Missing problem code");
        }

        try {
            return doGet(code);
        } catch (Exception e) {
            LOG.error("Exception in MedlineplusApiClient ", e);
        }
        return null;

    }

    private MedlineplusResponseJson doGet(String code) throws IOException, URISyntaxException {

        // add code parameter
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair(CODE_PARAM, code));

        // Build the server url together with the parameters you wish to pass
        URIBuilder urlBuilder = new URIBuilder(buildFullUrl());
        urlBuilder.addParameters(parameters);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        //headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

        ResponseEntity<MedlineplusResponseJson> response = restTemplate
                .exchange(urlBuilder.build(), HttpMethod.GET, entity, MedlineplusResponseJson.class);

        // only parse on 200 response
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            return null;
        }
    }

    /**
     * Helper method to build full url baseUrl + endpoint uri + params
     */
    private String buildFullUrl() {
        return apiUrl + "?" + CODE_SYSTEM_PARAM + "=" + codeSystem.code()
                + "&" + RESPONSE_TYPE_PARAM + "=" + MediaType.APPLICATION_JSON;
    }

    public static final class Builder {
        private MedlineplusApiClient result;

        private Builder() {
            result = new MedlineplusApiClient();
        }

        public Builder setCodeSystem(CodeSystem codeSystem) {
            if (null != codeSystem) {
                result.codeSystem = codeSystem;
            }
            return this;
        }

        public MedlineplusApiClient build() {

            // default to ICD-10-CM code system if nothing provided
            if (result.codeSystem == null) {
                result.codeSystem = CodeSystem.SNOMED_CT;
            }
            result.apiUrl = BASE_URL;
            return result;
        }
    }
}
