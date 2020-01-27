package com.solidstategroup.diagnosisview.service.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.solidstategroup.diagnosisview.model.RestPage;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.Link;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

/**
 * {@inheritDoc}
 */
@Slf4j
@Service
public class CodeSyncServiceImpl implements CodeSyncService {

    private static final int PAGE_SIZE = 30;
    private static final String PATIENTVIEW_AUTH_ENDPOINT_TEMPLATE = "%sauth/login";
    private static final String PATIENTVIEW_CODE_ENDPOINT_TEMPLATE =
            "%scode?filterText=&sortDirection=ASC&sortField=code&standardTypes=134";//&page=47&size=20
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
    private final String PATIENTVIEW_CODES_ENDPOINT;
    private final String PATIENTVIEW_CODE_DETAILS_ENDPOINT;
    private final Executor taskExecutor;

    public CodeSyncServiceImpl(BmjBestPractices bmjBestPractices,
                               final CodeService codeService,
                               @Qualifier("asyncExecutor") final Executor taskExecutor,
                               @Value("${PATIENTVIEW_USER:NONE}") String patientviewUser,
                               @Value("${PATIENTVIEW_PASSWORD:NONE}") String patientviewPassword,
                               @Value("${PATIENTVIEW_APIKEY:NONE}") String patientviewApiKey,
                               @Value("${PATIENTVIEW_URL:https://test.patientview.org/api/}") String patientviewUrl) {

        this.bmjBestPractices = bmjBestPractices;
        this.codeService = codeService;
        this.taskExecutor = taskExecutor;
        this.patientviewUser = patientviewUser;
        this.patientviewPassword = patientviewPassword;
        this.patientviewApiKey = patientviewApiKey;

        PATIENTVIEW_AUTH_ENDPOINT = format(PATIENTVIEW_AUTH_ENDPOINT_TEMPLATE, patientviewUrl);
        PATIENTVIEW_CODES_ENDPOINT = format(PATIENTVIEW_CODE_ENDPOINT_TEMPLATE, patientviewUrl);
        PATIENTVIEW_CODE_DETAILS_ENDPOINT = format("%scode/", patientviewUrl);
    }

    @CacheEvict(value = {"getAllCodes", "getAllCategories"}, allEntries = true)
    @Transactional
    public void syncCode(String code) {

        long start = System.currentTimeMillis();
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.set(AUTH_HEADER, getLoginToken());
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

            Code foundCode = codeService.get(code);

            ResponseEntity<Code> response = restTemplate
                    .exchange(PATIENTVIEW_CODE_DETAILS_ENDPOINT + foundCode.getId(), HttpMethod.GET, entity, Code.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                codeService.updateCodeFromSync(response.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to sync PV codes", e);
        }

        long stop = System.currentTimeMillis();
        log.info("Finished Code Sync from PatientView, timing {}", (stop - start));
    }


    @Override
    @Scheduled(cron = "${cron.job.sync.code}")
    public void syncCodes() {
        try {

            log.info("Starting Code Sync from PatientView");
            long start = System.currentTimeMillis();

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.set(AUTH_HEADER, getLoginToken());
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

            int currentPage = 0;
            ResponseEntity<RestPage<Code>> response = null;

            do {

                // call codes end point with small size per page, otherwise will run out of memory
                String codesUrl = UriComponentsBuilder.fromHttpUrl(PATIENTVIEW_CODES_ENDPOINT)
                        .queryParam("size", PAGE_SIZE)
                        .queryParam("page", currentPage)
                        .build()
                        .toUriString();

                ParameterizedTypeReference<RestPage<Code>> responseType =
                        new ParameterizedTypeReference<RestPage<Code>>() {
                        };

                response = restTemplate.exchange(codesUrl, HttpMethod.GET, entity, responseType);

                if (response.getStatusCode() == HttpStatus.OK) {
                    List<Code> codes = response.getBody().getContent();
                    batchProcess(codes, currentPage, entity);
                    currentPage++;
                } else {
                    log.error("Could not connect to PV, code {}", response.getStatusCode());
                    break;
                }
            } while (response != null && response.getBody().hasNext());

            long stop = System.currentTimeMillis();
            log.info("Finished Code Sync from PatientView, timing {}", (stop - start));

        } catch (Exception e) {
            log.error("Failed to sync PV codes", e);
        }
    }

    @Scheduled(cron = "${cron.job.sync.bmj.links}")
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
     * Update a list of Codes.
     * We are using CompletableFuture to process each code async.
     *
     * @param codesToProcess a list of codes to process
     * @param page           a page number that getting processes
     * @param entity         a HttpEntity containing required headers to be used to re fetch code details if needed
     */
    @CacheEvict(value = {"getAllCodes", "getAllCategories"}, allEntries = true)
    public void batchProcess(List<Code> codesToProcess, int page, org.springframework.http.HttpEntity<String> entity) {
        log.info("Starting code batchProcess {}, page {}", codesToProcess.size(), page);
        long start = System.currentTimeMillis();

        codes(codesToProcess)
                .thenCompose(codes -> {
                    List<CompletionStage<Code>> updatedCodes = codes.stream()
                            .map(code -> checkLinksAndUpdate(code, entity)
                                    .thenApply(r -> {
                                        log.debug("-> completed code {}", code.getCode());
                                        // check for missing link then do a call again
                                        return code;
                                    })
                            ).collect(Collectors.toList());
                    CompletableFuture<Void> done = CompletableFuture
                            .allOf(updatedCodes.toArray(new CompletableFuture[updatedCodes.size()]));
                    return done.thenApply(v -> updatedCodes.stream()
                            .map(CompletionStage::toCompletableFuture)
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList()));
                })
                .toCompletableFuture().join();

        long stop = System.currentTimeMillis();
        log.info("Finished code batchProcess, page {} timing {}", page, (stop - start));
    }

    /**
     * Update code details.
     * We have ddd issue where some links are getting stripped ﻿https://www.nhs.uk when returned
     * with a Code. Re fetch Code details to fix it.
     *
     * @param code a Code to update
     * @return a CompletionStage<Code>
     */
    @Transactional
    protected CompletionStage<Code> checkLinksAndUpdate(Code code, org.springframework.http.HttpEntity<String> entity) {

        return CompletableFuture.supplyAsync(() -> {

            RestTemplate restTemplate = new RestTemplate();
            boolean needRefetch = false;
            // need to check if all the links formatted correctly
            for (Link l : code.getLinks()) {
                if (!StringUtils.isEmpty(l.getLink()) && !l.getLink().startsWith("http")) {
                    log.info("Code {} missing http for link Link {} {} ", code.getCode(), l.getId(), l.getLink());
                    needRefetch = true;
                    break;
                }
            }

            if (needRefetch) {
                ResponseEntity<Code> response = restTemplate
                        .exchange(PATIENTVIEW_CODE_DETAILS_ENDPOINT + code.getId(), HttpMethod.GET, entity, Code.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    return codeService.updateCodeFromSync(response.getBody());
                }
            }

            return codeService.updateCodeFromSync(code);
        }, taskExecutor).exceptionally(th -> null);
    }

    /**
     * Wraps a list of Codes in CompletionStage for async processing
     *
     * @param codes a list of Code objects
     * @return CompletionStage<List<Code>>
     */
    private CompletionStage<List<Code>> codes(List<Code> codes) {
        return CompletableFuture.supplyAsync(() -> codes);
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
