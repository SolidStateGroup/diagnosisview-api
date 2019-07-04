package com.solidstategroup.diagnosisview.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.CodeExternalStandard;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    private final CodeService codeService;
    private final String patientviewUser;
    private final String patientviewPassword;
    private final String patientviewApiKey;
    private final String PATIENTVIEW_AUTH_ENDPOINT;
    private final String PATIENTVIEW_CODE_ENDPOINT;

    private static final String BMJ_BP_ENDPOINT_TEMPLATE = "https://bestpractice.bmj.com/infobutton?" +
            "knowledgeResponseType=application/json&" +
            "mainSearchCriteria.v.cs=%s&" +
            "mainSearchCriteria.v.c=%s&" +
            "mainSearchCriteria.v.dn=%s";

    private static final Map<String, String> codeMapping;
    private static final String SNOMED_CT = "SNOMED-CT";
    private static final String ICD_10 = "ICD-10";

    static {
        codeMapping = new HashMap<>();
        codeMapping.put(SNOMED_CT, "2.16.840.1.113883.6.96");
        codeMapping.put(ICD_10, "2.16.840.1.113883.6.90");
    }

    public CodeSyncServiceImpl(CodeService codeService,
            @Value("${PATIENTVIEW_USER:NONE}") String patientviewUser,
            @Value("${PATIENTVIEW_PASSWORD:NONE}") String patientviewPassword,
            @Value("${PATIENTVIEW_APIKEY:NONE}") String patientviewApiKey,
            @Value("${PATIENTVIEW_URL:https://test.patientview.org/api/}") String patientviewUrl) {

        this.codeService = codeService;
        this.patientviewUser = patientviewUser;
        this.patientviewPassword = patientviewPassword;
        this.patientviewApiKey = patientviewApiKey;

        PATIENTVIEW_AUTH_ENDPOINT =
                format(PATIENTVIEW_AUTH_ENDPOINT_TEMPLATE, patientviewUrl);

        PATIENTVIEW_CODE_ENDPOINT =
                format(PATIENTVIEW_CODE_ENDPOINT_TEMPLATE, patientviewUrl);

    }

    private static Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .setLongSerializationPolicy(LongSerializationPolicy.STRING)
            .registerTypeAdapter(Date.class, new DatetimeParser())
            .registerTypeAdapter(ImmutableSortedMap.class, new ImmutableSortedMapDeserializer())
            .registerTypeAdapter(ImmutableList.class, new ImmutableListDeserializer())
            .registerTypeAdapter(ImmutableMap.class, new ImmutableMapDeserializer())
            .create();

    @Override
//   @Scheduled(cron = "0 0 */2 * * *")
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

            Type fooType = new TypeToken<List<Code>>() {}.getType();
            String contentString = gson.toJson(gson.fromJson(responseString, Map.class).get("content"),
                    fooType);

            List<Code> codes = gson.fromJson(contentString, fooType);

            codes.forEach(this::updateCode);

            log.info("Finished Code Sync from PatientView");

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void syncBmjLinks() {

        RestTemplate template = new RestTemplate();

        for (Code code : codeService.getAll()) {

            final Optional<CodeExternalStandard> snomed_ct = getExternalStandard(code, SNOMED_CT);

            if (snomed_ct.isPresent()) {

                String url = buildUrl(SNOMED_CT, snomed_ct.get().getCodeString(), code.getCode());

                try {
                    ResponseEntity<ObjectNode> entity = template.getForEntity(url, ObjectNode.class);

                    if (entity.getStatusCode().is2xxSuccessful()) {

                        JsonNode entry = entity.getBody().findValue("entry");

                        if (entry.isArray()) {

                            for (JsonNode jn : entry) {

                                for (JsonNode link : jn.get("link")) {

                                    String href = link.get("href").asText();
                                    System.out.println(href);
                                }
                            }
                        }

                        continue;
                    }
                } catch (Exception e) {
                    System.out.println("continue...");
                }
            }

            final Optional<CodeExternalStandard> icd_10 = getExternalStandard(code, ICD_10);

            if (!icd_10.isPresent()) {

                continue;
            }

            String url = buildUrl(ICD_10, snomed_ct.get().getCodeString(), code.getCode());

            try {

                ResponseEntity<ObjectNode> entity = template.getForEntity(url, ObjectNode.class);

                if (!entity.getStatusCode().is2xxSuccessful()) {

                    continue;
                }

                JsonNode entry = entity.getBody().findValue("entry");

                if (entry.isArray()) {

                    for (JsonNode jn : entry) {

                        for (JsonNode link: jn.get("link")) {

                            String href = link.get("href").asText();
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("contine ICD-10...  ");
            }
        }
    }

    private String buildUrl(String externalStandard, String code, String codeName) {

        return String.format(BMJ_BP_ENDPOINT_TEMPLATE, codeMapping.getOrDefault(externalStandard, SNOMED_CT), code, codeName);
    }

    private Optional<CodeExternalStandard> getExternalStandard(Code code, String externalStandard) {

        return code.getExternalStandards()
                .stream()
                .filter(es -> es.getExternalStandard().getName().equals(externalStandard))
                .findAny();
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
