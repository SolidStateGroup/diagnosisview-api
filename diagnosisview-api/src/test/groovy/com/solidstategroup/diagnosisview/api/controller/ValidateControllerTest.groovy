package com.solidstategroup.diagnosisview.api.controller

import com.solidstategroup.diagnosisview.model.User
import spock.lang.Ignore

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ValidateControllerTest extends AbstractMvcSpec {

    static VALIDATE_ANDROID = '/api/user/validate/android'
    static VALIDATE_IOS = '/api/user/validate/ios'

    def user = new User(username: "test")

    void setup() {

        controller = new ValidateController(userService)
        buildMvc()
    }

    def "should verify android purchase"() {

        given: "a purchase string"

        def purchase = "test String"

        when: "verify android endpoint is called"

        server.perform(post(VALIDATE_ANDROID)
                .header(AUTH_HEADER, DEFAULT_AUTH_HEADER_VALUE)
                .content(purchase))

        then: "android token is verified"

        1 * userService.verifyAndroidToken(_ as User, purchase) >> user

        and: "user auth is checked"

        1 * userService.getUserByToken(DEFAULT_AUTH_HEADER_VALUE) >> user
    }

    @Ignore
    def "should verify ios purchase"() {

        given: "an ios purchase"

        def receipt = "receipt"
        def test = "[{\"transactionReceipt\": \"receipt\"}]"

        when: "verify ios endpoint is called"

        server.perform(post(VALIDATE_IOS)
                .header(AUTH_HEADER, DEFAULT_AUTH_HEADER_VALUE)
                .content(test))
                .andExpect(status().isOk())

        then: "ios token is verified"

        1 * userService.verifyAppleReceiptData(_ as User, receipt) >> user
    }
}
