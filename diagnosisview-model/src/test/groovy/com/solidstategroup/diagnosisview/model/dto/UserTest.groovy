package com.solidstategroup.diagnosisview.model.dto

import com.solidstategroup.diagnosisview.model.User
import spock.lang.Specification

class UserTest extends Specification {
    def "get profile image url"() throws Exception {
        given:

        def user = new User()
            user.username = "testerman"
            user.profileImage = ""
            user.profileImageFileType = "image/png"
        when: "we check the image url"
            def imageUrl = user.getProfileImagePath()
        then: "the image url is not null"
            imageUrl != null
        then: "the image url has username at end"
            imageUrl == "/api/profile/image/testerman"
    }
}
