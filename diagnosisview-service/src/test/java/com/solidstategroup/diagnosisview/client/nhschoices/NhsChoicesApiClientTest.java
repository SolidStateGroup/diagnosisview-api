package com.solidstategroup.diagnosisview.client.nhschoices;

import com.solidstategroup.diagnosisview.clients.nhschoices.ConditionLinkJson;
import com.solidstategroup.diagnosisview.clients.nhschoices.NhsChoicesApiClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Tests for NhsChoices v2 api client implementation
 *
 * API keys can be found under https://developer.api.nhs.uk
 */
@Slf4j
public class NhsChoicesApiClientTest {

    private static String apiKey = "{your_api_key}";

    @Ignore("Need to add nhs choices api key to run the test")
    @Test
    public void testApiClient_Return_Success_When_Letter_Valid() throws IOException {
        String letter = "A";
        NhsChoicesApiClient apiClient = NhsChoicesApiClient.newBuilder()
                .setApiKey(apiKey)
                .build();

        List<ConditionLinkJson> conditionLinks = apiClient.getConditions(letter);

        Assert.assertNotNull("Should get condition links in response", conditionLinks);
        Assert.assertTrue("Should have condition links in response", conditionLinks.size() > 0);

        for (ConditionLinkJson link : conditionLinks) {
            Assert.assertNotNull("Should get condition name", link.getName());
            Assert.assertNotNull("Should get condition url", link.getApiUrl());
            Assert.assertNotNull("Should get condition description", link.getDescription());
        }
    }

    @Ignore("Need to add nhs choices api key to run the test")
    @Test
    public void testApiClient_Return_All_Conditions_Success() throws IOException {

        NhsChoicesApiClient apiClient = NhsChoicesApiClient.newBuilder()
                .setApiKey(apiKey)
                .build();

        List<ConditionLinkJson> conditionLinks = apiClient.getAllConditions();

        Assert.assertNotNull("Should get condition links in response", conditionLinks);
        Assert.assertTrue("Should have condition links in response", conditionLinks.size() > 0);

        for (ConditionLinkJson link : conditionLinks) {
            Assert.assertNotNull("Should get condition name", link.getName());
            Assert.assertNotNull("Should get condition url", link.getApiUrl());
            Assert.assertNotNull("Should get condition description", link.getDescription());
        }
    }

    @Ignore("Need to add nhs choices api key to run the test")
    @Test
    public void testApiClient_Return_Success_When_Letter_Invalid_No_Conditions() throws IOException {

        // send invalid letter
        String invalidLetter = "6";
        NhsChoicesApiClient apiClient = NhsChoicesApiClient.newBuilder()
                .setApiKey(apiKey)
                .build();

        List<ConditionLinkJson> conditionLinks = apiClient.getConditions(invalidLetter);


        /**
         * Will still should get response from nhs api, but without any conditions
         */
        Assert.assertNull("Should not get any conditions in response", conditionLinks);
    }

    @Ignore("Need to add nhs choices api key to run the test")
    @Test
    public void testApiClient_Return_Null_When_Api_Key_Invalid() throws IOException {

        // send invalid letter
        String letter = "A";
        NhsChoicesApiClient apiClient = NhsChoicesApiClient.newBuilder()
                .setApiKey("invalid_key")
                .build();

        List<ConditionLinkJson> conditionLinks = apiClient.getConditions(letter);
        Assert.assertNull("Should get feed in response", conditionLinks);
    }

    @Test
    public void testAtoZ() {
        int count = 0;
        for (char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {
            count++;
        }
        Assert.assertTrue("Should have 26 letters", count == 26);
    }
}
