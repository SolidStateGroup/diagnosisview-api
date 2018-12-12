package com.solidstategroup.diagnosisview.api.controller

import com.solidstategroup.diagnosisview.model.LinkRuleDto
import com.solidstategroup.diagnosisview.model.User
import com.solidstategroup.diagnosisview.model.codes.LinkRule
import com.solidstategroup.diagnosisview.service.LinkRulesService

import static com.solidstategroup.diagnosisview.model.enums.RoleType.ADMIN
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class LinksRulesControllerTest extends AbstractMvcSpec {

    static LINKS_TRANSFORMATION = "/api/link/rules"

    def linksService = Mock(LinkRulesService)

    void setup() {
        controller = new LinksRulesController(linksService)
        buildMvc()
    }

    def "should accept institutional link transformation"() {
        given: "a valid link request"

        def linkTransformation =
                new LinkRuleDto(
                        link: "test",
                        transformation: "transformation",
                        institution: "UNIVERSITY_OF_EDINBURGH")

        when: "endpoint is called"

        postAt(LINKS_TRANSFORMATION, linkTransformation)
                .andExpect(status().isOk())

        then: "service layer is called with correct object"

        1 * linksService.add(linkTransformation) >> new LinkRule()
        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }

    def "should reject invalid link request"() {
        given: "an invalid link"

        def linkTransformation = new LinkRuleDto()

        when: "endpoint is called"

        postAt(LINKS_TRANSFORMATION, linkTransformation)
                .andExpect(status().isBadRequest())

        then: "service layer is not called"

        0 * linksService.add(linkTransformation)
    }
}
