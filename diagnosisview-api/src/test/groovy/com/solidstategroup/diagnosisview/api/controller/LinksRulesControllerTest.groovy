package com.solidstategroup.diagnosisview.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.solidstategroup.diagnosisview.exceptions.BadRequestException
import com.solidstategroup.diagnosisview.model.LinkRuleDto
import com.solidstategroup.diagnosisview.model.User
import com.solidstategroup.diagnosisview.model.codes.LinkRule
import com.solidstategroup.diagnosisview.model.codes.enums.CriteriaType
import com.solidstategroup.diagnosisview.model.codes.enums.Institution
import com.solidstategroup.diagnosisview.service.LinkRuleService
import com.solidstategroup.diagnosisview.service.UserService
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static com.solidstategroup.diagnosisview.model.enums.RoleType.ADMIN
import static org.hamcrest.collection.IsCollectionWithSize.hasSize
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

class LinksRulesControllerTest extends Specification {

    static LINK_RULES = "/api/link/rules"
    static AUTH_HEADER = "X-Auth-Token"
    static AUTH_HEADER_VALUE = "1b811191-53bb-4e87-9cac-ea3710ee42b9"
    static ObjectMapper MAPPER = new ObjectMapper()

    def linkRulesService = Mock(LinkRuleService)
    def userService = Mock(UserService)

    MockMvc server

    void setup() {
        def linksController = new LinksRulesController(linkRulesService)

        ReflectionTestUtils.setField(linksController, "userService", userService)

        server = standaloneSetup(linksController)
                .setControllerAdvice(new ApiControllerAdvice())
                .build()
    }

    def "should accept institutional link transformation"() {
        given: "a valid rule request"

        def rule =
                new LinkRuleDto(
                        link: "test",
                        transformation: "transformation",
                        criteriaType: CriteriaType.INSTITUTION,
                        criteria: Institution.UNIVERSITY_OF_EDINBURGH.toString())

        when: "endpoint is called"

        server.perform(post(LINK_RULES)
                .header(AUTH_HEADER, AUTH_HEADER_VALUE)
                .contentType(APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(rule)))
                .andExpect(status().isOk())

        then: "service layer is called with correct object"

        1 * linkRulesService.addRule(rule) >> new LinkRule()

        and: "admin role check is performed"

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }

    def "should reject invalid rule request"() {
        given: "an invalid rule"

        def linkTransformation = new LinkRuleDto()

        when: "endpoint is called"

        server.perform(post(LINK_RULES)
                .contentType(APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(linkTransformation)))
                .andExpect(status().isBadRequest())

        then: "service layer is not called"

        0 * linkRulesService.addRule(linkTransformation)
    }

    def "should fetch all rules"() {
        when:

        server.perform(get(LINK_RULES)
                .header(AUTH_HEADER, AUTH_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$', hasSize(2)))

        then: "rules are retrieved"

        1 * linkRulesService.getLinkRules() >> [new LinkRule(), new LinkRule()]

        and: "admin role check is performed"

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }

    def "should fetch a specific rule"() {
        given:

        def id = "a6924b77-cc96-4bbc-89ed-9e02477fee34"

        when:

        server.perform(get(LINK_RULES + "/" + id)
                .header(AUTH_HEADER, AUTH_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id').value(id))

        then: "rule is retrieved"

        1 * linkRulesService.getLinkRule(id) >> new LinkRule(id: id)

        and: "admin role check is performed"

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }

    def "should throw 404 when specific rule not found"() {
        given:

        def id = "a6924b77-cc96-4bbc-89ed-9e02477fee34"

        1 * linkRulesService.getLinkRule(id) >> { throw new BadRequestException() }

        when: "rule is retrieved"

        server.perform(get(LINK_RULES + "/" + id)
                .header(AUTH_HEADER, AUTH_HEADER_VALUE))
                .andExpect(status().isNotFound())

        then: "admin role check is performed"

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }

    def "should delete a specific rule"() {
        given: "id of rule to delete"

        def id = "a6924b77-cc96-4bbc-89ed-9e02477fee34"

        when: "delete endpoint is called"

        server.perform(delete(LINK_RULES + "/" + id)
                .header(AUTH_HEADER, AUTH_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(status().isOk())

        then: "link is deleted"

        1 * linkRulesService.deleteLinkRule(id)

        and: "admin role check is performed"

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }
}
