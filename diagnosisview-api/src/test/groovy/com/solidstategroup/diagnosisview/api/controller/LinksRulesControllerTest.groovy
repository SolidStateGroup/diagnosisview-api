package com.solidstategroup.diagnosisview.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.solidstategroup.diagnosisview.model.LinkRuleDto
import com.solidstategroup.diagnosisview.model.User
import com.solidstategroup.diagnosisview.model.codes.LinkRule
import com.solidstategroup.diagnosisview.service.LinkRulesService
import com.solidstategroup.diagnosisview.service.UserService
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static com.solidstategroup.diagnosisview.model.enums.RoleType.ADMIN
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

class LinksRulesControllerTest extends Specification {

    static LINKS_TRANSFORMATION = "/api/link/rules"
    static AUTH_HEADER = "X-Auth-Token"
    static AUTH_HEADER_VALUE = "1b811191-53bb-4e87-9cac-ea3710ee42b9"
    static ObjectMapper MAPPER = new ObjectMapper()

    def linksService = Mock(LinkRulesService)
    def userService = Mock(UserService)

    MockMvc server

    void setup() {
        def linksController = new LinksRulesController(linksService)

        ReflectionTestUtils.setField(linksController, "userService", userService)

        server = standaloneSetup(linksController)
                .build()
    }

    def "should accept institutional link transformation"() {
        given: "a valid link request"

        def linkTransformation =
                new LinkRuleDto(
                        link: "test",
                        transformation: "transformation",
                        institution: "UNIVERSITY_OF_EDINBURGH")

        when: "endpoint is called"

        server.perform(post(LINKS_TRANSFORMATION)
                .header(AUTH_HEADER, AUTH_HEADER_VALUE)
                .contentType(APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(linkTransformation)))
                .andExpect(status().isOk())

        then: "service layer is called with correct object"

        1 * linksService.addRule(linkTransformation) >> new LinkRule()
        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }

    def "should reject invalid link request"() {
        given: "an invalid link"

        def linkTransformation = new LinkRuleDto()

        when: "endpoint is called"

        server.perform(post(LINKS_TRANSFORMATION)
                .contentType(APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(linkTransformation)))
                .andExpect(status().isBadRequest())

        then: "service layer is not called"

        0 * linksService.addRule(linkTransformation)
    }
}
