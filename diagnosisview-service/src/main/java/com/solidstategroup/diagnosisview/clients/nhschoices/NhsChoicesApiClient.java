package com.solidstategroup.diagnosisview.clients.nhschoices;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Api Client implementation for NhsChoices API v2.
 *
 * This client covers 2 NhsChoices service subscriptions: Conditions and Organisation data.
 *
 * Each of them requires different ste of api key, which can be found under Developers
 * console (https://developer.api.nhs.uk/developer)
 */
@Slf4j
public final class NhsChoicesApiClient {

    private static final String AUTH_HEADER = "Subscription-Key";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final String BASE_URL = "https://api.nhs.uk/";
    protected static final String CONDITIONS_URI = "conditions/";

    private String apiUrl;
    private String apiKey;

    // Filters the conditions by A-Z
    private static final String PARAM_CONDITION_CATEGORY = "category";
    // Includes synonyms conditions
    private static final String PARAM_SYNONYMS = "synonyms";

    private NhsChoicesApiClient() {
    }

    public static NhsChoicesApiClient.Builder newBuilder() {
        return new NhsChoicesApiClient.Builder();
    }

    /**
     * Retrieves NHSChoices conditions for give letter
     *
     * @param letter an A-Z letter to filter conditions
     * @return a list of condition, or null if nothing found or cannot contact api
     */
    public List<ConditionLinkJson> getConditions(String letter) {
        if (StringUtils.isBlank(letter)) {
            throw new IllegalArgumentException("Please provide letter");
        }

        // request parameters
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair(PARAM_CONDITION_CATEGORY, letter));
        parameters.add(new BasicNameValuePair(PARAM_SYNONYMS, "false"));

        try {
            NhsChoicesResponseJson responseJson = doGet(parameters, CONDITIONS_URI);
            if (responseJson != null) {
                return responseJson.getConditionLinks();
            }
        } catch (Exception e) {
            log.error("Exception in NhsChoicesApiClient ", e);
        }
        return null;
    }

    /**
     * Finds all NHSChoices conditions
     *
     * @return a list of all condition, or null if nothing found or cannot contact api
     */
    public List<ConditionLinkJson> getAllConditions() {

        List<ConditionLinkJson> allConditions = new ArrayList<>();

        // request parameters
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair(PARAM_SYNONYMS, "false"));

        // run from A-Z to get all conditions
        for (char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {
            try {
                // add code parameter
                parameters.add(new BasicNameValuePair(PARAM_CONDITION_CATEGORY, String.valueOf(alphabet)));
                NhsChoicesResponseJson responseJson = doGet(parameters, CONDITIONS_URI);
                if (responseJson != null) {
                    allConditions.addAll(responseJson.getConditionLinks());
                } else {
                    log.warn("NhsChoicesResponseJson is null for  " + alphabet);
                }
            } catch (Exception e) {
                log.error("Exception in NhsChoicesApiClient.getAllConditions() ", e);
            }

            // test system need to throttle as allowed 10 calls/minute up to a maximum of 1000 calls per month
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                log.error("InterruptedException in NhsChoicesApiClient ", ie);
            }
        }

        return allConditions;
    }

    private NhsChoicesResponseJson doGet(List<NameValuePair> parameters, String uri) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set(AUTH_HEADER, apiKey);
        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

        // Build the server url together with the parameters you wish to pass
        URIBuilder urlBuilder = new URIBuilder(buildFullUrl(uri));
        if (parameters != null) {
            urlBuilder.addParameters(parameters);
        }

        ResponseEntity<NhsChoicesResponseJson> response = restTemplate
                .exchange(urlBuilder.build(), HttpMethod.GET, entity, NhsChoicesResponseJson.class);

        // only parse on 200 response
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            return null;
        }
    }

    /**
     * Helper method to build full url baseUrl + endpoint uri
     */
    private String buildFullUrl(String uri) {
        return apiUrl + uri;
    }

    public static final class Builder {
        private NhsChoicesApiClient result;

        private Builder() {
            result = new NhsChoicesApiClient();
        }

        public Builder setApiKey(String apiKey) {
            if (null != apiKey) {
                result.apiKey = apiKey;
            }
            return this;
        }

        public NhsChoicesApiClient build() {
            result.apiUrl = BASE_URL;
            return result;
        }
    }
}
