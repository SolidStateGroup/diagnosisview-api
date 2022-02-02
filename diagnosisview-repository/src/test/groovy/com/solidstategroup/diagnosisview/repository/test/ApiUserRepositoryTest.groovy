package com.solidstategroup.diagnosisview.repository.test

import com.solidstategroup.diagnosisview.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

@ContextConfiguration(classes = TestPersistenceConfig)
@Transactional
class ApiUserRepositoryTest extends Specification {

    @Autowired
    UserRepository userRepository

    def user1 = new com.solidstategroup.diagnosisview.model.User(username: "testerman")
    def user2 = new com.solidstategroup.diagnosisview.model.User(username: "testerwoman")

    def setup() {
        userRepository.save(user1)
        userRepository.save(user2)
    }

    def "find by username"() {
        when:
            def user = userRepository.findOneByUsernameIgnoreCase(user1.username)
        then: "should return correct user"
            user != null
            user.id.equals(user1.id)
            user.username.equals(user1.username)
    }
}
