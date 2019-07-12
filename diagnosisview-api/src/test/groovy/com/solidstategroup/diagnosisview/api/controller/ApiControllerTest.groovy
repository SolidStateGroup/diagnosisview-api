package com.solidstategroup.diagnosisview.api.controller

import com.solidstategroup.diagnosisview.model.LoginRequest
import com.solidstategroup.diagnosisview.model.User

import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ApiControllerTest extends AbstractMvcSpec {

    static USER_LOGIN = '/api/login'
    static USER_PRINCIPAL = '/api/account'
    static REGISTER = '/api/register'

    def username = "testuser"
    def password = "password1"

    void setup() {
        controller = new ApiController(userService)
        buildMvc()
    }

    def "should log a user in"() {

        given: "a login request"

        def login = new LoginRequest(username: username, password: password)

        when: "login endpoint is called"

        postAt(USER_LOGIN, login)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.token', is(DEFAULT_AUTH_HEADER_VALUE)))

        then: "token is returned"

        1 * userService.login(username , password) >>  new User(token: DEFAULT_AUTH_HEADER_VALUE, username: username)
    }

    def "should return a logged in users account info"() {

        when: "user principal endpoint is called"

        getAt(USER_PRINCIPAL)
            .andExpect(status().isOk())
            .andExpect(jsonPath('$.username', is(username)))

        then: "user details are returned"

        1 * userService.getUserByToken(_ as String) >> new User(username: username)
    }

    def "should register a normal user"() {

        given: "a registration request"

        def registration = new User(username: username)

        when: "registration endpoint is called"

        postAt(REGISTER, registration)
            .andExpect(status().isOk())
            .andExpect(jsonPath('$.username', is(username)))

        then: "user is registered"

        1 * userService.createOrUpdateUser(_ as User, false) >> registration
    }
}
