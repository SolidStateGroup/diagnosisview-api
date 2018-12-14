package com.solidstategroup.diagnosisview.service.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.service.CodeService;
import com.solidstategroup.diagnosisview.service.CodeSyncService;
import com.solidstategroup.diagnosisview.service.DatetimeParser;
import com.tyler.gson.immutable.ImmutableListDeserializer;
import com.tyler.gson.immutable.ImmutableMapDeserializer;
import com.tyler.gson.immutable.ImmutableSortedMapDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@inheritDoc}.
 */
@Slf4j
@Service
public class CodeSyncServiceImpl implements CodeSyncService {

    @Value("${PATIENTVIEW_USER:NONE}")
    private String patientviewUser;

    @Value("${PATIENTVIEW_PASSWORD:NONE}")
    private String patientviewPassword;

    @Value("${PATIENTVIEW_APIKEY:NONE}")
    private String patientviewApiKey;

    @Value("${PATIENTVIEW_URL:https://test.patientview.org/api/}")
    private String patientviewUrl;

    private static Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .setLongSerializationPolicy(LongSerializationPolicy.STRING)
            .registerTypeAdapter(Date.class, new DatetimeParser())
            .registerTypeAdapter(ImmutableSortedMap.class, new ImmutableSortedMapDeserializer())
            .registerTypeAdapter(ImmutableList.class, new ImmutableListDeserializer())
            .registerTypeAdapter(ImmutableMap.class, new ImmutableMapDeserializer())
            .create();

    @Autowired
    private CodeService codeService;

    @Override
    @Scheduled(cron = "0 0 */2 * * *")
    public void syncCodes() {
        try {
            log.info("Starting Code Sync from PatientView");
            //Make request to auth/login
            HttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead

            //Make the request
            HttpGet request = new HttpGet(patientviewUrl +
                    "code?filterText=&page=0&size=200000&sortDirection=ASC&sortField=code&standardTypes=134");
            request.addHeader("content-type", "application/json");
            request.addHeader("X-Auth-Token", getLoginToken());

            HttpResponse response = httpClient.execute(request);

            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");

            Type fooType = new TypeToken<List<Code>>() {
            }.getType();
            String contentString = gson.toJson(gson.fromJson(responseString, Map.class).get("content"),
                    fooType);

            List<Code> codes = gson.fromJson(contentString, fooType);


            codes.stream().forEach(code -> updateCode(code));
            log.info("Finished Code Sync from PatientView");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @org.springframework.transaction.annotation.Transactional
    protected void updateCode(Code code) {

        codeService.upsert(code, true);
    }

    /**
     * Get the token returned by the api.
     *
     * @return login token
     */
    private String getLoginToken() throws IOException {

        //Make request to auth/login
        HttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead

        Map<String, String> postbody = new HashMap<>();
        postbody.put("username", patientviewUser);
        postbody.put("password", patientviewPassword);
        postbody.put("apiKey", patientviewApiKey);


        HttpPost request = new HttpPost(patientviewUrl + "auth/login");
        StringEntity params = new StringEntity(gson.toJson(postbody));
        request.addHeader("content-type", "application/json");
        request.setEntity(params);
        HttpResponse response = httpClient.execute(request);

        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        System.out.println(responseString);

        return gson.fromJson(responseString, Map.class).get("token").toString();
    }
}
