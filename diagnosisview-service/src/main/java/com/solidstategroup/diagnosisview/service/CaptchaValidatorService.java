package com.solidstategroup.diagnosisview.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Pavlo Maksymchuk.
 */
@Slf4j
@Component
public class CaptchaValidatorService {

  private RestTemplate restTemplate;
  private String siteKey;
  private String siteSecret;

  public CaptchaValidatorService(@Value("${google.recaptcha.key.site}") String siteKey,
      @Value("${google.recaptcha.key.secret}") String siteSecret) {
    this.restTemplate = new RestTemplate();
    this.siteKey = siteKey;
    this.siteSecret = siteSecret;
    log.info("Created");
  }

  public boolean isValid(String captchaResponse) {

    // Ensure user hasn't submitted a blank captcha response
    if (StringUtils.isBlank(captchaResponse)) {
      return false;
    }

    // Prepare the form data for sending to google
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>(2);
    formData.add("response", captchaResponse);
    formData.add("secret", siteSecret);

    try {
      // Post the data to google
      GoogleResponseData responseData = restTemplate.postForObject(
          "https://www.google.com/recaptcha/api/siteverify",
          formData, GoogleResponseData.class);

      if (responseData.success) { // Verified by google
        log.debug("Captcha validation succeeded.");
        return true;
      }

      log.info("Captcha validation failed.");
      return false;

    } catch (Exception e) {
      log.error(ExceptionUtils.getStackTrace(e));
      return false;
    }
  }

  /**
   * A class to receive the response from Google
   */
  private static class GoogleResponseData {

    private boolean success;

    @JsonProperty("error-codes")
    private Collection<String> errorCodes;

    public boolean isSuccess() {
      return success;
    }

    public void setSuccess(boolean success) {
      this.success = success;
    }

    public Collection<String> getErrorCodes() {
      return errorCodes;
    }

    public void setErrorCodes(Collection<String> errorCodes) {
      this.errorCodes = errorCodes;
    }
  }

}
