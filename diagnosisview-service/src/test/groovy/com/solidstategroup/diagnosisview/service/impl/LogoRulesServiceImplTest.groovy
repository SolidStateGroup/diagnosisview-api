package com.solidstategroup.diagnosisview.service.impl

import com.google.api.client.util.Base64
import com.solidstategroup.diagnosisview.model.LogoRuleDto
import com.solidstategroup.diagnosisview.model.codes.LogoRule
import com.solidstategroup.diagnosisview.model.codes.enums.DifficultyLevel
import com.solidstategroup.diagnosisview.repository.LinkRepository
import com.solidstategroup.diagnosisview.repository.LogoRuleRepository
import spock.lang.Specification

class LogoRulesServiceImplTest extends Specification {

    def linkRepository = Mock(LinkRepository)
    def logoRuleRepository = Mock(LogoRuleRepository)

    def logoRulesService =
            new LogoRulesServiceImpl(linkRepository, logoRuleRepository)

    def "should add a logo"() {

        given: "a logo rule request"

        def base64 = "base64"
        def imageFormat = "image/png"
        def starts = "https://www.nhs.uk"

        def logoRule = new LogoRuleDto(
                image: base64,
                imageFormat: imageFormat,
                overrideDifficultyLevel: "RED",
                startsWith: starts)

        when: "rule is added"

        def result = logoRulesService.add(logoRule)

        then: "rule is saved"

        1 * logoRuleRepository.saveAndFlush(_ as LogoRule) >> { it[0] }
        1 * linkRepository.addLogoRule(_ as LogoRule)

        and: "correct fields are saved"

        with(result) {
            linkLogo == Base64.decodeBase64(base64.bytes)
            logoFileType == imageFormat
            startsWith == starts
            overrideDifficultyLevel == DifficultyLevel.RED
        }
    }
}
