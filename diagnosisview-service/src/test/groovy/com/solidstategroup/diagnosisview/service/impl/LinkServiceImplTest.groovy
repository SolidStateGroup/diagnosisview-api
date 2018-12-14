package com.solidstategroup.diagnosisview.service.impl

import com.solidstategroup.diagnosisview.model.codes.Link
import com.solidstategroup.diagnosisview.model.codes.enums.DifficultyLevel
import com.solidstategroup.diagnosisview.repository.LinkRepository
import com.solidstategroup.diagnosisview.repository.LookupRepository
import com.solidstategroup.diagnosisview.repository.LookupTypeRepository
import com.solidstategroup.diagnosisview.service.LinkService
import com.solidstategroup.diagnosisview.service.LogoRulesService
import spock.lang.Specification

class LinkServiceImplTest extends Specification {

    def linkRepository = Mock(LinkRepository)
    def lookupRepository = Mock(LookupRepository)
    def lookupTypeRepository = Mock(LookupTypeRepository)
    def logoRuleService = Mock(LogoRulesService)

    LinkService linkService = new LinkServiceImpl(
            linkRepository,
            lookupRepository,
            lookupTypeRepository,
            logoRuleService
    )

    def "should find one link"() {

        given: "a stored link"

        def id = 21824868

        1 * linkRepository.findOne(id) >> new Link(id: id)

        when: "link is fetched by id"

        def result = linkService.get(id)

        then: "correct link object is returned"

        result.id == id
    }

    def "should update link"() {

        given: "a link update"

        def id = 1

        def updatedLink =
                new Link(
                        id: id,
                        difficultyLevel: DifficultyLevel.RED,
                        displayOrder: 1,
                        freeLink: false,
                        transformationsOnly: false)

        def currentLink =
                new Link(
                        displayOrder: 2,
                        difficultyLevel: DifficultyLevel.GREEN,
                        freeLink: true,
                        transformationsOnly: true)

        1 * linkRepository.findOne(id) >> currentLink

        when: "attempt to update link is made"

        def result = linkService.update(updatedLink)

        then: "link is saved"

        1 * linkRepository.save(_ as Link) >> { it[0] }

        and: "saved link contains updated details"

        with(result) {
            difficultyLevel == updatedLink.difficultyLevel
            displayOrder == updatedLink.displayOrder
            freeLink == updatedLink.freeLink
            transformationsOnly == updatedLink.transformationsOnly
            lastUpdate != null
        }
    }
}
