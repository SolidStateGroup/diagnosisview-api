package com.solidstategroup.diagnosisview.service.test

import com.solidstategroup.diagnosisview.model.User
import com.solidstategroup.diagnosisview.repository.UserRepository
import com.solidstategroup.diagnosisview.service.UserService
import com.solidstategroup.diagnosisview.service.impl.UserServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = TestServiceConfig)
class UserServiceTest extends Specification {

    @Autowired
    UserRepository userRepository;

    UserService userService;
    User user;


    def setup() {
        userService = new UserServiceImpl(userRepository);
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


    def "Test Android Receipt User"() {
        when:
        def savedUser = userService.ve(user.username)
        then: "should return correct user"
        savedUser != null
        savedUser.username.equals(user.username)
    }
}
