package com.solidstategroup.diagnosisview.api.controller

import com.solidstategroup.diagnosisview.model.FeedbackDto
import com.solidstategroup.diagnosisview.model.PasswordResetDto
import com.solidstategroup.diagnosisview.model.SavedUserCode
import com.solidstategroup.diagnosisview.model.User
import com.solidstategroup.diagnosisview.service.EmailService

import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class UserControllerTest extends AbstractMvcSpec {

    static UPDATE_USER = '/api/user/'
    static FEEDBACK = '/api/user/feedback'
    static RESET_PASSWORD = '/api/user/reset-password'
    static FORGOTTEN_PASSWORD = '/api/user/forgotten-password'
    static FAVOURITE = '/api/user/favourites'
    static SYNC_FAVOURITE = '/api/user/sync/favourites'
    static SYNC_HISTORY = '/api/user/sync/history'
    static HISTORY = '/api/user/history'

    def emailService = Mock(EmailService)

    def user = new User(id: 1,
            username: "test",
            token: DEFAULT_AUTH_HEADER_VALUE)

    void setup() {

        controller = new UserController(userService, emailService)
        buildMvc()
    }

    def "should update user"() {

        given: "a user update"


        when: "update user endpoint is called"

        putAt(UPDATE_USER, user)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.token', is(DEFAULT_AUTH_HEADER_VALUE)))

        then: "user is updated"

        1 * userService.createOrUpdateUser(_ as User, false) >> user

        and: "user is fetched via token"

        1 * userService.getUserByToken(_ as String) >> user
    }

    def "should send feedback"() {

        given: "some feedback"

        def body = "body"
        def feedback = new FeedbackDto(body)

        def user = new User(id: 1,
                username: "test",
                token: DEFAULT_AUTH_HEADER_VALUE)

        when: "feedback endpoint is called"

        postAt(FEEDBACK, feedback)
                .andExpect(status().isOk())

        then: "feedback is sent"

        1 * emailService.sendFeedback(_ as User, body)

        and: "user is fetched via token"

        1 * userService.getUserByToken(DEFAULT_AUTH_HEADER_VALUE) >> user
    }

    def "should reset password"() {

        given: "a user has forgotten their password"

        def reset = new PasswordResetDto(
                "newPassword",
                "resetCode",
                "username"
        )

        when: "reset password is called"

        postAt(RESET_PASSWORD, reset)
                .andExpect(status().isOk())

        then: "password is reset"

        1 * userService.resetPassword(reset)
    }

    def "should send forgotten password"() {

        given: "a user has forgotten their password"

        def username = "test-user"
        def user = new User(username: username)

        when: "forgotten password is called"

        postAt(FORGOTTEN_PASSWORD, user)
            .andExpect(status().isOk())

        then: "user details are retrieved via username"

        1 * userService.getUser(username) >> user

        and: "reset password code is sent"

        1 * userService.sendResetPassword(user)
    }

    def "should save a user's favourite code"() {

        given: "a user has a favourite code"

        def favourite = new SavedUserCode()

        when: "a favourite is processed"

        putAt(FAVOURITE, favourite)
            .andExpect(status().isOk())

        then: "favourite is stored against the user"

        1 * userService.addFavouriteToUser(_ as User, favourite) >> user

        and: "user is fetch from token"

        1 * userService.getUserByToken(DEFAULT_AUTH_HEADER_VALUE) >> user
    }

    def "should sync a user's history"() {

        given: "the device is syncing the user's history"

        def history = [new SavedUserCode() , new SavedUserCode()]

        when: "sync endpoint is called"

        putAt(SYNC_HISTORY, history)
            .andExpect(status().isOk())

        then: "user's history is recorded"

        1 * userService.addMultipleHistoryToUser(_ as User, history) >> user

        and: "user is fetch from token"

        1 * userService.getUserByToken(DEFAULT_AUTH_HEADER_VALUE) >> user
    }

    def "should sync a user's favourite"() {

        given: "the device is syncing the user's favourites"

        def favourites = [new SavedUserCode(), new SavedUserCode()]

        when: "sync endpoint is called"

        putAt(SYNC_FAVOURITE, favourites)
            .andExpect(status().isOk())

        then: "user's favourites are recorded"

        1 * userService.addMultipleFavouritesToUser(user, favourites)

        and: "user is fetch from token"

        1 * userService.getUserByToken(DEFAULT_AUTH_HEADER_VALUE) >> user
    }

    def "should save user's history"() {

        given: "a user's history"

        def history = new SavedUserCode()

        when: "history endpoint is called"

        putAt(HISTORY, history)
            .andExpect(status().isOk())

        then: "history is recorded"

        1 * userService.addHistoryToUser(user, history) >> user

        and: "user is fetch from token"

        1 * userService.getUserByToken(DEFAULT_AUTH_HEADER_VALUE) >> user
    }

    def "should delete a user's favourite"() {

        given: "a user wishes to delete their favourite code"

        def favourite = new SavedUserCode()

        when: "delete favourite code is called"

        deleteAt(FAVOURITE, favourite)
            .andExpect(status().isOk())

        then: "favourite is deleted"

        1 * userService.deleteFavouriteToUser(user, favourite) >> user

        and: "user is fetched from token"

        1 * userService.getUserByToken(DEFAULT_AUTH_HEADER_VALUE) >> user
    }

    def "should delete a user's history item"() {

        given: "a user wishes to delete their history"

        def history = new SavedUserCode()

        when: "delete history is called"

        deleteAt(HISTORY, history)
            .andExpect(status().isOk())


        then: "history is deleted"

        1 * userService.deleteHistoryToUser(user, history) >> user

        and: "user is fetched from token"

        1 * userService.getUserByToken(DEFAULT_AUTH_HEADER_VALUE) >> user
    }
}
