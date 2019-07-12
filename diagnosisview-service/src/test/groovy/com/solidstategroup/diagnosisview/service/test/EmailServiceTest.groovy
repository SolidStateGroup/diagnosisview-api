package com.solidstategroup.diagnosisview.service.implt

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.solidstategroup.diagnosisview.model.SavedUserCode
import com.solidstategroup.diagnosisview.model.User
import com.solidstategroup.diagnosisview.repository.UserRepository
import com.solidstategroup.diagnosisview.service.EmailService
import com.solidstategroup.diagnosisview.service.impl.EmailServiceImpl
import com.solidstategroup.diagnosisview.service.impl.UserServiceImpl
import com.solidstategroup.diagnosisview.utils.AppleReceiptValidation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Shared
import spock.lang.Specification

class EmailServiceTest extends Specification {

    EmailServiceImpl emailService;
    User user = new User(username:'testerman6')

    def setup() {
        emailService = new EmailServiceImpl()
    }

    def 'Generate Reset Email'() {
        when:
            def content = emailService.generateFeedbackEmail(user, 'CODE')
        then: 'should contain the code'
            content.contains('CODE')
    }
}
