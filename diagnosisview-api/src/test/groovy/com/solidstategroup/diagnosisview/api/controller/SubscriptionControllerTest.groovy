package com.solidstategroup.diagnosisview.api.controller

import com.solidstategroup.diagnosisview.model.User

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

class SubscriptionControllerTest extends AbstractMvcSpec {

    static VALIDATE_ANDROID = '/api/user/validate/android'

    def user = new User(username: "test")

    void setup() {

        controller = new SubscriptionController(userService, subscriptionService)
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
}
