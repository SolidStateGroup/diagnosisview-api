package com.solidstategroup.diagnosisview.service.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
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
    @Scheduled(cron = "0 0 */2 * * *")
    public void syncCodes() {
        try {

            log.info("Starting Code Sync from PatientView");

            HttpClient httpClient = HttpClientBuilder.create().build();

            //Make the request
            HttpGet request = new HttpGet(PATIENTVIEW_CODE_ENDPOINT);
            request.addHeader(APPLICATION_JSON_HEADER);
            //Make request to auth/login
            request.addHeader(AUTH_HEADER, getLoginToken());

            String responseString =
                    EntityUtils
                            .toString(httpClient
                                    .execute(request)
                                    .getEntity(), "UTF-8");

            Type fooType = new TypeToken<List<Code>>() {
            }.getType();
            String contentString = gson.toJson(gson.fromJson(responseString, Map.class).get("content"),
                    fooType);

            List<Code> codes = gson.fromJson(contentString, fooType);

            codes.forEach(this::updateCode);

            log.info("Finished Code Sync from PatientView");

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 */2 * * *")
    @Override
    public void syncBmjLinks() {

        final UUID correlation = UUID.randomUUID();
        log.info("Correlation id: {}. Starting BMJ link job", correlation);
        Instant start = Instant.now();

        try {

            bmjBestPractices.syncBmjLinks();

        } catch (Exception e) {

            log.error("Correlation id: {}. BMJ link job threw an exception: {}", correlation, e);


        } finally {

            log.info("Correlation id: {}. BMJ link job finished", correlation);
            log.debug("Correlation id: {}. Time taken: {}", correlation, Duration.between(start, Instant.now()));
        }
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
