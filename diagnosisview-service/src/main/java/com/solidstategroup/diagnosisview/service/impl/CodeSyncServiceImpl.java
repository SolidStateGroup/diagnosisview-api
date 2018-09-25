package com.solidstategroup.diagnosisview.service.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.CodeCategory;
import com.solidstategroup.diagnosisview.model.codes.CodeExternalStandard;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.codes.Lookup;
import com.solidstategroup.diagnosisview.repository.CategoryRepository;
import com.solidstategroup.diagnosisview.repository.CodeCategoryRepository;
import com.solidstategroup.diagnosisview.repository.CodeExternalStandardRepository;
import com.solidstategroup.diagnosisview.repository.CodeRepository;
import com.solidstategroup.diagnosisview.repository.ExternalStandardRepository;
import com.solidstategroup.diagnosisview.repository.LinkRepository;
import com.solidstategroup.diagnosisview.repository.LookupRepository;
import com.solidstategroup.diagnosisview.repository.LookupTypeRepository;
import com.solidstategroup.diagnosisview.service.CodeSyncService;
import com.solidstategroup.diagnosisview.service.DatetimeParser;
import com.tyler.gson.immutable.ImmutableListDeserializer;
import com.tyler.gson.immutable.ImmutableMapDeserializer;
import com.tyler.gson.immutable.ImmutableSortedMapDeserializer;
import lombok.extern.java.Log;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * {@inheritDoc}.
 */
@Log
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
    private CategoryRepository categoryRepository;

    @Autowired
    private CodeExternalStandardRepository codeExternalStandardRepository;

    @Autowired
    private CodeRepository codeRepository;

    @Autowired
    private CodeCategoryRepository codeCategoryRepository;

    @Autowired
    private ExternalStandardRepository externalStandardRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private LookupRepository lookupRepository;

    @Autowired
    private LookupTypeRepository lookupTypeRepository;

    private Lookup niceLinksLookup;
    private Lookup userLink;

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

            //Get the NICE lookup if it exists
            populatDVLookups();

            codes.stream().forEach(code -> updateCode(code));
            log.info("Finished Code Sync from PatientView");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @org.springframework.transaction.annotation.Transactional
    protected void updateCode(Code code) {
        Code existingCode = codeRepository.findOne(code.getId());

        //If there is a code, or it has been updated, update
        if (existingCode == null || existingCode.getLinks().size() != code.getLinks().size() ||
                existingCode.getExternalStandards().size() != code.getExternalStandards().size() ||
                existingCode.getCodeCategories().size() != code.getCodeCategories().size() ||
                existingCode.getLastUpdate().before(code.getLastUpdate())) {

            lookupTypeRepository.save(code.getStandardType().getLookupType());
            lookupRepository.save(code.getStandardType());

            lookupTypeRepository.save(code.getCodeType().getLookupType());
            lookupRepository.save(code.getCodeType());

            for (CodeCategory codeCategory : code.getCodeCategories()) {
                categoryRepository.save(codeCategory.getCategory());
            }

            //Check if code category exists
            for (CodeExternalStandard externalStandard : code.getExternalStandards()) {
                externalStandardRepository.save(externalStandard.getExternalStandard());
            }

            //Check if code category exists
            for (Link link : code.getLinks()) {
                //If the lookupValue is a DV only value, then dont save as it will overlap
                //In future this may need to be a check against all DV only lookup values
                if (link.getLinkType().getId().equals(niceLinksLookup.getId())) {
                    link.setLinkType(userLink);
                } else {
                    lookupTypeRepository.save(link.getLinkType().getLookupType());
                    lookupRepository.save(link.getLinkType());
                }
            }

            Set<Link> links = code.getLinks();
            Set<CodeCategory> codeCategories = code.getCodeCategories();
            Set<CodeExternalStandard> externalStandards = code.getExternalStandards();


            //Remove code related fields, this stops exceptions being thrown
            code.setLinks(new HashSet<>());
            code.setCodeCategories(new HashSet<>());
            code.setExternalStandards(new HashSet<>());

            codeRepository.save(code);

            //Add in the code categories
            for (CodeCategory codeCategory : codeCategories) {
                codeCategory.setCode(code);
                codeCategoryRepository.save(codeCategory);
            }

            //Add in code related external standards
            for (CodeExternalStandard externalStandard : externalStandards) {
                externalStandard.setCode(code);
                codeExternalStandardRepository.save(externalStandard);
            }

            //Add in code specific links
            for (Link link : links) {
                Link existingLink = linkRepository.findOne(link.getId());

                //If the link is a nice link, we should categorise it as such
                //In the future this maybe extended into its own function
                if (link.getLink().contains("nice.org.uk")) {
                    link.setLinkType(niceLinksLookup);
                }
                //Ensure that difficulty is not overwritten
                if (existingLink != null) {
                    link.setDifficultyLevel(existingLink.getDifficultyLevel());
                    link.setFreeLink(existingLink.getFreeLink());
                }

                link.setCode(code);
                linkRepository.save(link);
            }

            code.setLinks(links);
            code.setCodeCategories(codeCategories);
            code.setExternalStandards(externalStandards);

            codeRepository.save(code);
        }
    }

    /**
     * Populates the DiagnosisView specific lookup values
     */
    private void populatDVLookups() {
        niceLinksLookup = lookupRepository.findOneByValue("NICE_CKS");
        userLink = lookupRepository.findOneByValue("CUSTOM");
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
