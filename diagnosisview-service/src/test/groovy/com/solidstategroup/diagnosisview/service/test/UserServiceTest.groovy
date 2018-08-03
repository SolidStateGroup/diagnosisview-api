package com.solidstategroup.diagnosisview.service.test

import com.solidstategroup.diagnosisview.model.User
import com.solidstategroup.diagnosisview.repository.UserRepository
import com.solidstategroup.diagnosisview.service.UserService
import com.solidstategroup.diagnosisview.service.impl.UserServiceImpl
import com.solidstategroup.diagnosisview.utils.AppleReceiptValidation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = TestServiceConfig)
class UserServiceTest extends Specification {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AppleReceiptValidation appleReceiptValidation;

    UserService userService;
    User user;


    def setup() {
        userService = new UserServiceImpl(userRepository, appleReceiptValidation);
        user = new User()
        user.setId(1234L)
        user.username = "testerman"
        user = userRepository.save(user)
    }

    def "Get User"() {
        when:
            def savedUser = userService.getUser(user.username)
        then: "should return correct user"
            savedUser != null
            savedUser.username.equals(user.username)
    }
}
