package com.solidstategroup.diagnosisview.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.solidstategroup.diagnosisview.service.UserService
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import spock.lang.Specification

import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

/**
 * Abstract class to make it easier to get started with Spring MVC testing
 */
class AbstractMvcSpec extends Specification {

    static AUTH_HEADER = "X-Auth-Token"
    static DEFAULT_AUTH_HEADER_VALUE = "1b811191-53bb-4e87-9cac-ea3710ee42b9"

    static ObjectMapper MAPPER = new ObjectMapper()

    def userService = Mock(UserService)

    MockMvc server

    /**
     * Initialize this controller to
     */
    protected controller

    protected controllerAdvice

    /**
     * Uses reflection to add the userService to the {@link BaseController} and
     * builds a Mock MVC based on the controller variable.
     */
    void buildMvc() {

        controllerAdvice = new ApiControllerAdvice()

        server = standaloneSetup(controller)
                .setControllerAdvice(controllerAdvice)
                .build()
    }

    /**
     * Wraps a post request
     */
    ResultActions postAt(String url, Object object) {
        server.perform(post(url)
                .header(AUTH_HEADER, DEFAULT_AUTH_HEADER_VALUE)
                .contentType(APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(object)))
    }

    /**
     * Wraps a put request
     */
    ResultActions putAt(String url, Object object) {
        server.perform(put(url)
                .header(AUTH_HEADER, DEFAULT_AUTH_HEADER_VALUE)
                .contentType(APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(object)))
    }

    /**
     * Wraps a get request
     */
    ResultActions getAt(String url) {
        server.perform(get(url)
                .header(AUTH_HEADER, DEFAULT_AUTH_HEADER_VALUE))
    }

    /**
     * Wraps a delete request
     */
    ResultActions deleteAt(String url) {
        server.perform(delete(url)
                .header(AUTH_HEADER, DEFAULT_AUTH_HEADER_VALUE))
    }

    /**
     * Wraps a delete request
     */
    ResultActions deleteAt(String url, Object object) {
        server.perform(delete(url)
                .header(AUTH_HEADER, DEFAULT_AUTH_HEADER_VALUE)
                .contentType(APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(object)))
    }
}
