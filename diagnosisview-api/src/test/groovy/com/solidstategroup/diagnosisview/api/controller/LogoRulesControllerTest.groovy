package com.solidstategroup.diagnosisview.api.controller

import com.solidstategroup.diagnosisview.model.LogoRuleDto
import com.solidstategroup.diagnosisview.model.User
import com.solidstategroup.diagnosisview.model.codes.LogoRule
import com.solidstategroup.diagnosisview.service.LogoRulesService

import static com.solidstategroup.diagnosisview.model.enums.RoleType.ADMIN
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class LogoRulesControllerTest extends AbstractMvcSpec {

    static LOGO_RULES = "/api/logo/rules"
    static LOGO_RULES_ID = "/api/logo/rules/%s"
    static FETCH_IMAGE = "/api/logo/rules/%s/image"

    def ID = "4fe24b8a-19a3-4af1-8939-2cc0e53b2373"
    def image = "test"
    def imageType = "image/png"
    def startsWith = "www.nhs.uk"

    def logoRulesService = Mock(LogoRulesService)

    void setup() {
        controller = new LogoRulesController(userService, logoRulesService)
        buildMvc()
    }

    def "should add a logo"() {

        given: "a logo to add"

        def logoRule = standardLogoRuleDto()

        when: "endpoint is called"

        postAt(LOGO_RULES, logoRule)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id', is(ID)))
                .andExpect(jsonPath('$.imageUrl', is(sprintf(FETCH_IMAGE, ID))))

        then: "logo is added"

        1 * logoRulesService.add(logoRule) >> standardLogoRule()

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }

    def "should fetch all logos"() {

        given: "logos have been stored in repository"

        1 * logoRulesService.getRules() >> [
                new LogoRule(),
                new LogoRule()
        ]

        when: "endpoint is called"

        getAt(LOGO_RULES)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$', hasSize(2)))

        then: "all logos are returned"

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }

    def "should update a logo"() {

        given: "updates to a logo"

        def logoRuleDto = standardLogoRuleDto()

        when: "endpoint is callled"

        putAt(sprintf(LOGO_RULES_ID, ID), logoRuleDto)
                .andExpect(status().isOk())

        then: "logo is updated"

        1 * logoRulesService.update(ID, logoRuleDto) >> standardLogoRule()

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }

    def "should delete a logo"() {

        when: "endpoint is called"

        deleteAt(sprintf(LOGO_RULES_ID, ID))
                .andExpect(status().isOk())

        then: "logo is deleted"

        1 * logoRulesService.delete(ID)

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }

    def "should fetch a logo rule"() {

        given: "a stored logo"

        1 * logoRulesService.get(ID) >> standardLogoRule()

        when: "endpoint is called logo is retrieved"

        getAt(sprintf(LOGO_RULES_ID, ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id', is(ID)))

        then:

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }

    def "should return stored image"() {

        given:

        def logo = "TEST".bytes


        when:

        getAt(sprintf(FETCH_IMAGE, ID))
                .andExpect(status().isOk())
                .andExpect(content().bytes(logo))

        then:

        1 * logoRulesService.get(ID) >> new LogoRule(linkLogo: logo)
    }

    def "should send 404 when logo image not found"() {

        when: "endpoint is called, 404 is returned"

        getAt(sprintf(FETCH_IMAGE, ID))
                .andExpect(status().isNotFound())

        then: "logo rule is not found"

        1 * logoRulesService.get(ID) >> null
    }

    def standardLogoRule() {
        new LogoRule(id: ID, logoData: image.getBytes("UTF-8"),
                startsWith: startsWith, logoFileType: imageType)
    }

    def standardLogoRuleDto() {
        new LogoRuleDto(image: image, imageFormat: imageType, startsWith: startsWith)
    }
}
