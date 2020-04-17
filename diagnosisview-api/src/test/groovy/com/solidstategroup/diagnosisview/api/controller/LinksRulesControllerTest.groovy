package com.solidstategroup.diagnosisview.api.controller

import com.solidstategroup.diagnosisview.exceptions.BadRequestException
import com.solidstategroup.diagnosisview.model.LinkRuleDto
import com.solidstategroup.diagnosisview.model.User
import com.solidstategroup.diagnosisview.model.codes.LinkRule
import com.solidstategroup.diagnosisview.model.codes.enums.CriteriaType
import com.solidstategroup.diagnosisview.model.codes.enums.InstitutionEnum
import com.solidstategroup.diagnosisview.service.LinkRuleService

import static com.solidstategroup.diagnosisview.model.enums.RoleType.ADMIN
import static org.hamcrest.collection.IsCollectionWithSize.hasSize
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class LinksRulesControllerTest extends AbstractMvcSpec {

    static LINK_RULES = "/api/link/rules"

    def linkRulesService = Mock(LinkRuleService)

    void setup() {
        controller = new LinksRulesController(userService, linkRulesService)
        buildMvc()
    }

    def "should accept institutional link transformation"() {
        given: "a valid rule request"

        def rule =
                new LinkRuleDto(
                        link: "test",
                        transformation: "transformation",
                        criteriaType: CriteriaType.INSTITUTION,
                        criteria: InstitutionEnum.UNIVERSITY_OF_EDINBURGH.toString())

        when: "endpoint is called"

        postAt(LINK_RULES, rule)
                .andExpect(status().isOk())

        then: "service layer is called with correct object"

        1 * linkRulesService.add(rule) >> new LinkRule()

        and: "admin role check is performed"

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }

    def "should reject invalid rule request"() {

        given: "an invalid rule"

        def linkTransformation = new LinkRuleDto()

        when: "endpoint is called"

        postAt(LINK_RULES, linkTransformation)
                .andExpect(status().isBadRequest())

        then: "service layer is not called"

        0 * linkRulesService.add(linkTransformation)
    }

    def "should fetch all rules"() {
        when:

        getAt(LINK_RULES)
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

        getAt(LINK_RULES + "/" + id)
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

        getAt(LINK_RULES + "/" + id)
                .andExpect(status().isNotFound())

        then: "admin role check is performed"

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }

    def "should delete a specific rule"() {
        given: "id of rule to delete"

        def id = "a6924b77-cc96-4bbc-89ed-9e02477fee34"

        when: "delete endpoint is called"

        deleteAt(LINK_RULES + "/" + id)
                .andExpect(status().isOk())

        then: "link is deleted"

        1 * linkRulesService.deleteLinkRule(id)

        and: "admin role check is performed"

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }
}
