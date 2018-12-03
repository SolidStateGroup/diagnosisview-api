package com.solidstategroup.diagnosisview.service.impl

import com.solidstategroup.diagnosisview.repository.LinkRepository
import com.solidstategroup.diagnosisview.repository.LinkRuleMappingRepository
import com.solidstategroup.diagnosisview.repository.LinkRuleRepository
import com.solidstategroup.diagnosisview.service.LinkRulesService
import spock.lang.Specification

class LinkRulesServiceImplTest extends Specification {

    LinkRulesService linkRulesService

    def linkRuleRepository = Mock(LinkRuleRepository)
    def linkRuleMappingRepository = Mock(LinkRuleMappingRepository)
    def linkRepository = Mock(LinkRepository)

    void setup() {
        linkRulesService =
                new LinkRulesServiceImpl(linkRuleRepository, linkRuleMappingRepository, linkRepository)
    }

    def "should delete rule"() {
        given: "an id"

        def id = "a6924b77-cc96-4bbc-89ed-9e02477fee34"

        when: "delete is called"

        linkRulesService.deleteLinkRule(id)


        then: "the rule is remove from the repository"

        1 * linkRuleRepository.delete(id)
    }
}
