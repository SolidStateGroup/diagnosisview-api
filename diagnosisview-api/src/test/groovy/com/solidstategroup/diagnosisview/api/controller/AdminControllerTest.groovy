package com.solidstategroup.diagnosisview.api.controller

import com.solidstategroup.diagnosisview.model.LoginRequest
import com.solidstategroup.diagnosisview.model.User
import com.solidstategroup.diagnosisview.model.codes.Code
import com.solidstategroup.diagnosisview.model.codes.ExternalStandard
import com.solidstategroup.diagnosisview.model.codes.Link
import com.solidstategroup.diagnosisview.repository.ExternalStandardRepository
import com.solidstategroup.diagnosisview.service.CodeService
import com.solidstategroup.diagnosisview.service.LinkService

import static com.solidstategroup.diagnosisview.model.enums.RoleType.ADMIN
import static com.solidstategroup.diagnosisview.model.enums.RoleType.USER
import static java.lang.String.format
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AdminControllerTest extends AbstractMvcSpec {

    static ADMIN_LOGIN = '/api/admin/login'
    static ADMIN_USERS = '/api/admin/users'
    static ADMIN_USER = '/api/admin/user'
    static ADMIN_USER_ID = '/api/admin/user/%s'
    static ADMIN_CODE = '/api/admin/code'
    static ADMIN_CODE_LINK = '/api/admin/code/link'
    static ADMIN_EXTERNAL_STANDARDS = "/api/admin/code/external-standards"

    def codeService = Mock(CodeService)
    def linkService = Mock(LinkService)
    def externalStandardRepository = Mock(ExternalStandardRepository)

    def username = "testusername"
    def password = "password1"

    void setup() {
        controller =
                new AdminController(userService, codeService, linkService, externalStandardRepository)
        buildMvc()
    }

    def "should return login details"() {

        given: "a login request"

        def login = new LoginRequest(username, password)

        when: "login endpoint is called"

        postAt(ADMIN_LOGIN, login)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.token', is(DEFAULT_AUTH_HEADER_VALUE)))

        then: "user is logged in"

        userService.login(username, password) >>
                new User(token: DEFAULT_AUTH_HEADER_VALUE, roleType: ADMIN, username: username)
    }

    def "should throw exception then user is not admin"() {

        given:

        def login = new LoginRequest(username, password)

        when:

        postAt(ADMIN_LOGIN, login)
            .andExpect(status().isForbidden())

        then:

        1 * userService.login(username, password) >>
                new User(token: DEFAULT_AUTH_HEADER_VALUE, roleType: USER, username: username)
    }

    def "should throw exception then user is not found"() {

        given:

        def login = new LoginRequest(username, password)

        when:

        postAt(ADMIN_LOGIN, login)
                .andExpect(status().isForbidden())

        then:

        1 * userService.login(username, password) >> null

    }

    def "should return all users"() {

        when: "get users endpoint is called"

        getAt(ADMIN_USERS)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$', hasSize(2)))

        then: "all users are returned"

        1 * userService.getAllUsers() >> [new User(username: "one"), new User(username: "two")]

        and: "admin role check is performed"

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }

    def "should delete a user"() {

        given:

        def user = new User(username: username)

        when:

        deleteAt(ADMIN_USER, user)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.username', is(username)))

        then:

        1 * userService.deleteUser(user) >> user

        and: "admin role check is performed"

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }

    def "should create a user"() {

        given: "a new admin user registration"

        def user = new User(username: username)

        when: "register admin endpoint is called"

        postAt(ADMIN_USER, user)
                .andExpect(status().isOk())

        then: "admin is created"

        1 * userService.createOrUpdateUser(user, true) >> user

        and: "admin role check is performed"

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }

    def "should create a new code"() {

        given: "a new code"

        def code = new Code()

        when: "create code endpoint is called"

        postAt(ADMIN_CODE, code)
                .andExpect(status().isOk())

        then: "code is created"

        1 * codeService.upsert(code, false) >> code

        and: "admin role check is performed"

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }

    def "should update a code"() {

        given: "a code to update"

        def code = new Code()

        when: "update code endpoint is called"

        putAt(ADMIN_CODE, code)
                .andExpect(status().isOk())

        then: "code is updated"

        1 * codeService.upsert(code, false) >> code

        and: "admin role check is performed"

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }

    def "should update a link"() {

        given: "a link to update"

        def link = new Link(id: 1)

        when: "update link endpoint is called"

        putAt(ADMIN_CODE_LINK, link)
                .andExpect(status().isOk())

        then: ""

        1 * linkService.update(link) >> link

        and: "admin role check is performed"

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }

    def "should fetch external standards"() {

        when: "call to external standards endpoint is made"

        getAt(ADMIN_EXTERNAL_STANDARDS)
                .andExpect(status().isOk())
                .andExpect jsonPath('$', hasSize(2))

        then: "external standards are returned"

        1 * externalStandardRepository.findAll() >>
                [new ExternalStandard(), new ExternalStandard()]

        and: "admin role check is performed"

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }

    def "should update a user"() {

        given: "a user to update"

        def userId = "1"
        def user = new User(username: username)

        when: "call to update user is made"

        putAt(format(ADMIN_USER_ID, userId), user)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.username', is(username)))

        then: "user is updated"

        1 * userService.createOrUpdateUser(_ as User, true) >> user

        and: "admin role check is performed"

        1 * userService.getUserByToken(_ as String) >> new User(roleType: ADMIN)
    }
}
