package com.solidstategroup.diagnosisview.api.controller

import com.solidstategroup.diagnosisview.model.User
import com.solidstategroup.diagnosisview.model.codes.Category
import com.solidstategroup.diagnosisview.model.codes.Code
import com.solidstategroup.diagnosisview.model.codes.Link
import com.solidstategroup.diagnosisview.model.codes.enums.Institution
import com.solidstategroup.diagnosisview.service.CodeService
import com.solidstategroup.diagnosisview.service.LinkService

import static java.lang.String.format
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CodeControllerTest extends AbstractMvcSpec {

    static CODE = '/api/code'
    static SINGLE_CODE = '/api/code/%s'
    static CATEGORY = '/api/category'

    def codeService = Mock(CodeService)
    def linkService = Mock(LinkService)

    void setup() {

        controller = new CodeController(userService, codeService, linkService)
        buildMvc()
    }

    def "should create a code"() {

        given: "a new code to create"

        def code = new Code(links: [new Link(), new Link()])

        when: "create code endpoint is called"

        postAt(CODE, code)
                .andExpect(status().isOk())

        then: "a new code is created"

        1 * codeService.save(_ as Code) >> code

        and: "so are it's links"

        2 * linkService.update(_ as Link)
    }

    def "should update a code"() {

        given: "a code requiring an update"

        def code = new Code(links: [new Link(), new Link()])

        when: "update code endpoint is called"

        putAt(CODE, code)
                .andExpect(status().isOk())

        then: "code is updated"

        1 * codeService.save(_ as Code) >> code

        and: "so are it's links"

        2 * linkService.update(_ as Link)
    }

    def "should delete a code"() {

        given: "a code to delete"

        def code = new Code()

        when: "delete code endpoint is called"

        deleteAt(CODE, code)
                .andExpect(status().isOk()
        )

        then: "code is deleted"

        1 * codeService.delete(code)
    }

    def "should return all categories"() {

        when: "categories endpoint is called"

        getAt(CATEGORY)
                .andExpect(status().isOk())

        then: "all categories are returned"

        1 * codeService.getAllCategories() >> [new Category(), new Category()]
    }

    def "should return a single code"() {

        given: "a code"

        def code = 'cancer'

        when: "get code endpoint is called"

        getAt(format(SINGLE_CODE, code))
                .andExpect(status().isOk())

        then: "code is retrieved"

        1 * codeService.get(code) >> new Code()
    }

    def "should get all code based on an institution"() {

        when: "get code endpoint is called"

        getAt(CODE)
                .andExpect(status().isOk())

        then: "correct codes are returned"

        1 * codeService.getAll(Institution.UNIVERSITY_OF_EDINBURGH) >>
                [new Code()]

        and: "user is presented with the appropriate codes"

        1 * userService.getUserByToken(DEFAULT_AUTH_HEADER_VALUE) >>
                new User(institution: "University of Edinburgh")
    }

    def "should get all codes when an institution isn't found"() {

        when: "get code endpoint is called"

        getAt(CODE)
                .andExpect(status().isOk())

        then: "correct codes are returned"

        1 * codeService.getAll(null) >>
                [new Code()]

        and: "user is presented with the appropriate codes"

        1 * userService.getUserByToken(DEFAULT_AUTH_HEADER_VALUE) >>
                new User(institution: null)
    }
}
