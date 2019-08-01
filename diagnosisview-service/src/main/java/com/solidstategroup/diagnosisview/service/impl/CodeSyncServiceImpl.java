package com.solidstategroup.diagnosisview.service.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.solidstategroup.diagnosisview.model.RestPage;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.service.BmjBestPractices;
import com.solidstategroup.diagnosisview.service.CodeService;
import com.solidstategroup.diagnosisview.service.CodeSyncService;
import com.solidstategroup.diagnosisview.service.DatetimeParser;
import com.tyler.gson.immutable.ImmutableListDeserializer;
import com.tyler.gson.immutable.ImmutableMapDeserializer;
import com.tyler.gson.immutable.ImmutableSortedMapDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

/**
 * {@inheritDoc}
 */
@Slf4j
@Service
public class CodeSyncServiceImpl implements CodeSyncService {

    private static final String PATIENTVIEW_AUTH_ENDPOINT_TEMPLATE = "%sauth/login";
    private static final String PATIENTVIEW_CODE_ENDPOINT_TEMPLATE =
            "%scode?filterText=&page=0&size=200000&sortDirection=ASC&sortField=code&standardTypes=134";
    private static final String AUTH_HEADER = "X-Auth-Token";
    private static final Header APPLICATION_JSON_HEADER =
            new BasicHeader("content-type", APPLICATION_JSON_VALUE);
    private static Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .setLongSerializationPolicy(LongSerializationPolicy.STRING)
            .registerTypeAdapter(Date.class, new DatetimeParser())
            .registerTypeAdapter(ImmutableSortedMap.class, new ImmutableSortedMapDeserializer())
            .registerTypeAdapter(ImmutableList.class, new ImmutableListDeserializer())
            .registerTypeAdapter(ImmutableMap.class, new ImmutableMapDeserializer())
            .create();

    private final BmjBestPractices bmjBestPractices;
    private final CodeService codeService;
    private final String patientviewUser;
    private final String patientviewPassword;
    private final String patientviewApiKey;
    private final String PATIENTVIEW_AUTH_ENDPOINT;
    private final String PATIENTVIEW_CODE_ENDPOINT;

    public CodeSyncServiceImpl(BmjBestPractices bmjBestPractices,
                               CodeService codeService,
                               @Value("${PATIENTVIEW_USER:NONE}") String patientviewUser,
                               @Value("${PATIENTVIEW_PASSWORD:NONE}") String patientviewPassword,
                               @Value("${PATIENTVIEW_APIKEY:NONE}") String patientviewApiKey,
                               @Value("${PATIENTVIEW_URL:https://test.patientview.org/api/}") String patientviewUrl) {

        this.bmjBestPractices = bmjBestPractices;
        this.codeService = codeService;
        this.patientviewUser = patientviewUser;
        this.patientviewPassword = patientviewPassword;
        this.patientviewApiKey = patientviewApiKey;

        PATIENTVIEW_AUTH_ENDPOINT =
                format(PATIENTVIEW_AUTH_ENDPOINT_TEMPLATE, patientviewUrl);

        PATIENTVIEW_CODE_ENDPOINT =
                format(PATIENTVIEW_CODE_ENDPOINT_TEMPLATE, patientviewUrl);
    }

    @Override
    //@Scheduled(cron = "0 0 */2 * * *") // every 2 hours
//    @Scheduled(cron = "0 */5 * * * ?") // every 2 min
    public void syncCodes() {
        try {

            log.info("Starting Code Sync from PatientView");
            long start = System.currentTimeMillis();

//            HttpClient httpClient = HttpClientBuilder.create().build();
//
//            //Make the request
//            HttpGet request = new HttpGet(PATIENTVIEW_CODE_ENDPOINT);
//            request.addHeader(APPLICATION_JSON_HEADER);
//            //Make request to auth/login
//            request.addHeader(AUTH_HEADER, getLoginToken());
//
//            HttpResponse response = httpClient.execute(request);
//            HttpEntity entity = response.getEntity();
//            String responseString = EntityUtils.toString(entity, "UTF-8");
//
//            Type fooType = new TypeToken<List<Code>>() {
//            }.getType();
//            String contentString = gson.toJson(gson.fromJson(responseString, Map.class).get("content"),
//                    fooType);
//
//            List<Code> codes = gson.fromJson(contentString, fooType);

            RestTemplate restTemplate = new RestTemplate();


            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.set(AUTH_HEADER, getLoginToken());
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

            //&page=0&size=200000
            UriComponents uriBuilder = UriComponentsBuilder.fromHttpUrl(PATIENTVIEW_CODE_ENDPOINT)
                    .queryParam("pageSize", "2000")
                    .queryParam("page", "0")
                    .build();

            //uriBuilder.getQueryParams().replace("page", String.valueOf(2));

            ParameterizedTypeReference<RestPage<Code>> responseType =
                    new ParameterizedTypeReference<RestPage<Code>>() {
                    };

            ResponseEntity<RestPage<Code>> response =
                    restTemplate.exchange(PATIENTVIEW_CODE_ENDPOINT, HttpMethod.GET, entity, responseType);


            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Got Codes from PatientView, timing {}", (System.currentTimeMillis() - start));
                List<Code> codes = response.getBody().getContent();
                codes.forEach(this::updateCode);
            } else {
                log.error("Could not connect to PV, code {}", response.getStatusCode());
            }


            long stop = System.currentTimeMillis();
            log.info("Finished Code Sync from PatientView, timing {}", (stop - start));

        } catch (Exception e) {
            log.error("Failed to sync PV codes", e);
        }
    }


    //    @Scheduled(cron = "0 0 */2 * * *")
//    @Scheduled(cron = "0 */5 * * * ?") // every 5 min
    @Override
    public void syncBmjLinks() {

        final UUID correlation = UUID.randomUUID();
        log.info("Correlation id: {}. Starting BMJ link job", correlation);
        Instant start = Instant.now();
        long start2 = System.currentTimeMillis();

        try {

            bmjBestPractices.syncBmjLinks();

        } catch (Exception e) {
            log.error("Correlation id: {}. BMJ link job threw an exception: {}", correlation, e);
        } finally {
            log.info("Correlation id: {}. BMJ link job finished, timing: {}", correlation, Duration.between(start, Instant.now()));
        }

        long stop = System.currentTimeMillis();
        log.info("Finished BMJ link job, timing {}", (stop - start2));
    }

    @Transactional
    protected void updateCode(Code code) {

        try {

            codeService.upsert(code, true);

        } catch (Exception e) {

            log.info("Insert failed for code: " + code.getCode() + " with error: " + e.getMessage());
        }
    }

    /**
     * Get the token returned by the api.
     *
     * @return login token
     */
    private String getLoginToken() throws IOException {

        //Make request to auth/login
        HttpClient httpClient = HttpClientBuilder.create().build();

        Map<String, String> body = new HashMap<>();
        body.put("username", patientviewUser);
        body.put("password", patientviewPassword);
        body.put("apiKey", patientviewApiKey);

        HttpPost request = new HttpPost(PATIENTVIEW_AUTH_ENDPOINT);
        request.addHeader(APPLICATION_JSON_HEADER);
        request.setEntity(new StringEntity(gson.toJson(body)));

        HttpEntity entity = httpClient.execute(request).getEntity();

        String responseString = EntityUtils.toString(entity, "UTF-8");

        return gson.fromJson(responseString, Map.class).get("token").toString();
    }
}
