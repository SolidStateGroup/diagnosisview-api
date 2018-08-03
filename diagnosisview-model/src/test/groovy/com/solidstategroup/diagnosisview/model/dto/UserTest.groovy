package com.solidstategroup.diagnosisview.model.dto

import com.solidstategroup.diagnosisview.model.User
import spock.lang.Specification

class UserTest extends Specification {
    def "get profile image url"() throws Exception {
        given:

        def user = new User()
            user.username = "testerman"
    }
}
