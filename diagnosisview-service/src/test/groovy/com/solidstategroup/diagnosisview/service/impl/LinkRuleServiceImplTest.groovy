package com.solidstategroup.diagnosisview.service.impl

import com.solidstategroup.diagnosisview.exceptions.BadRequestException
import com.solidstategroup.diagnosisview.model.LinkRuleDto
import com.solidstategroup.diagnosisview.model.codes.Link
import com.solidstategroup.diagnosisview.model.codes.LinkRule
import com.solidstategroup.diagnosisview.model.codes.LinkRuleMapping
import com.solidstategroup.diagnosisview.model.codes.enums.CriteriaType
import com.solidstategroup.diagnosisview.repository.LinkRepository
import com.solidstategroup.diagnosisview.repository.LinkRuleMappingRepository
import com.solidstategroup.diagnosisview.repository.LinkRuleRepository
import com.solidstategroup.diagnosisview.service.LinkRuleService
import spock.lang.Specification

class LinkRuleServiceImplTest extends Specification {

    LinkRuleService linkRuleService

    def linkRuleRepository = Mock(LinkRuleRepository)
    def linkRuleMappingRepository = Mock(LinkRuleMappingRepository)
    def linkRepository = Mock(LinkRepository)

    def id = "a6924b77-cc96-4bbc-89ed-9e02477fee34"

    void setup() {
        linkRuleService =
                new LinkRuleServiceImpl(
                        linkRuleRepository,
                        linkRuleMappingRepository,
                        linkRepository)
    }

    def "should delete rule"() {
        given: "an id"

        def id = "a6924b77-cc96-4bbc-89ed-9e02477fee34"

        when: "delete is called"

        linkRuleService.deleteLinkRule(id)


        then: "the rule is remove from the repository"

        1 * linkRuleRepository.delete(id)
    }

    def "should get all rules"() {
        given:

        1 * linkRuleRepository.findAll() >> [new LinkRule(), new LinkRule()]

        when: "get all rules is called"

        def rules = linkRuleService.getLinkRules()

        then: "rules are return from repository"

        rules.size() == 2
    }

    def "should retrieve a single link"() {
        given: "rule in repository"

        1 * linkRuleRepository.findOne(id) >> new LinkRule(id: id)

        when: "rule is fetched"

        def rule = linkRuleService.getLinkRule(id)

        then: "correct rule is returned"

        rule.id == id
    }

    def "should throw bad request when rule not found"() {
        given: "rule does not exist in repository"

        1 * linkRuleRepository.findOne(id) >> null

        when: "get link rule is called"

        linkRuleService.getLinkRule(id)

        then:

        thrown BadRequestException
    }

    def "should throw bad request when trying to update non-existent rule"() {
        given: "rule does not exist in repository"

        1 * linkRuleRepository.findOne(id) >> null

        when: "update link rule is called"

        linkRuleService.updateLinkRule(id, new LinkRuleDto())

        then:

        thrown BadRequestException
    }

    def "should throw bad request when criteria type is NONE"() {
        given: "criteria type is NONE"

        def linkRuleDto = new LinkRuleDto(criteriaType: CriteriaType.NONE)

        when: "add link rule is called"

        linkRuleService.add(linkRuleDto)

        then:

        thrown BadRequestException
    }
}
