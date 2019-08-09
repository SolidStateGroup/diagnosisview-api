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
import com.solidstategroup.diagnosisview.service.CodeSyncService;
import com.solidstategroup.diagnosisview.service.DatetimeParser;
import com.solidstategroup.diagnosisview.task.CodeProcessor;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
    private final CodeProcessor codeProcessor;
    private final String patientviewUser;
    private final String patientviewPassword;
    private final String patientviewApiKey;
    private final String PATIENTVIEW_AUTH_ENDPOINT;
    private final String PATIENTVIEW_CODE_ENDPOINT;

    public CodeSyncServiceImpl(BmjBestPractices bmjBestPractices,
                               final CodeProcessor codeProcessor,
                               @Value("${PATIENTVIEW_USER:NONE}") String patientviewUser,
                               @Value("${PATIENTVIEW_PASSWORD:NONE}") String patientviewPassword,
                               @Value("${PATIENTVIEW_APIKEY:NONE}") String patientviewApiKey,
                               @Value("${PATIENTVIEW_URL:https://test.patientview.org/api/}") String patientviewUrl) {

        this.bmjBestPractices = bmjBestPractices;
        this.codeProcessor = codeProcessor;
        this.patientviewUser = patientviewUser;
        this.patientviewPassword = patientviewPassword;
        this.patientviewApiKey = patientviewApiKey;

        PATIENTVIEW_AUTH_ENDPOINT =
                format(PATIENTVIEW_AUTH_ENDPOINT_TEMPLATE, patientviewUrl);

        PATIENTVIEW_CODE_ENDPOINT =
                format(PATIENTVIEW_CODE_ENDPOINT_TEMPLATE, patientviewUrl);
    }

    @Override
    @Scheduled(cron = "0 0 23 * * ?") // every day at 23:00
    public void syncCodes() {
        try {

            log.info("Starting Code Sync from PatientView");
            long start = System.currentTimeMillis();

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
                List<Code> codes = response.getBody().getContent();
                log.info("Got Codes {} from PatientView, timing {}", codes.size(), (System.currentTimeMillis() - start));

                final int batchSize = 10;
                final AtomicInteger counter = new AtomicInteger();

                // chunk the list of codes and process in batches
                final Collection<List<Code>> result = codes.stream()
                        .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / batchSize))
                        .values();

                // process in batches async to speed up code update
                int index = 0;
                for (List<Code> list : result) {
                    codeProcessor.processBatch(list, index++);
                }

            } else {
                log.error("Could not connect to PV, code {}", response.getStatusCode());
            }

            long stop = System.currentTimeMillis();
            log.info("Finished Code Sync from PatientView, timing {}", (stop - start));

        } catch (Exception e) {
            log.error("Failed to sync PV codes", e);
        }
    }


    @Scheduled(cron = "0 0 22 * * ?") // every day at 22:00
    @Override
    public void syncBmjLinks() {

        final UUID correlation = UUID.randomUUID();
        log.info("Correlation id: {}. Starting BMJ link job", correlation);
        long start = System.currentTimeMillis();
        try {

            bmjBestPractices.syncBmjLinks();

        } catch (Exception e) {
            log.error("Correlation id: {}. BMJ link job threw an exception: {}", correlation, e);
        }

        long stop = System.currentTimeMillis();
        log.info("Finished BMJ link job, timing {}", (stop - start));
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
