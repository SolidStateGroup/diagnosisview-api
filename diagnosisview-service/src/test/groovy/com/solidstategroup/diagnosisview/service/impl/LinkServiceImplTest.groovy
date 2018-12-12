package com.solidstategroup.diagnosisview.service.impl

import com.solidstategroup.diagnosisview.model.codes.Link
import com.solidstategroup.diagnosisview.model.codes.enums.DifficultyLevel
import com.solidstategroup.diagnosisview.repository.LinkRepository
import com.solidstategroup.diagnosisview.repository.LogoRuleRepository
import com.solidstategroup.diagnosisview.repository.LookupRepository
import com.solidstategroup.diagnosisview.repository.LookupTypeRepository
import com.solidstategroup.diagnosisview.service.LinkService
import spock.lang.Specification

class LinkServiceImplTest extends Specification {

    def linkRepository = Mock(LinkRepository)
    def lookupRepository = Mock(LookupRepository)
    def lookupTypeRepository = Mock(LookupTypeRepository)
    def logoRuleRepository = Mock(LogoRuleRepository)

    LinkService linkService = new LinkServiceImpl(
            linkRepository,
            lookupRepository,
            lookupTypeRepository,
            logoRuleRepository
    )

    def "should find one link"() {

        given: "a stored link"

        def id = 21824868

        1 * linkRepository.findOne(id) >> new Link(id: id)

        when: "link is fetched by id"

        def result = linkService.getLink(id)

        then: "correct link object is returned"

        result.id == id
    }

    def "should update link"() {

        given: "a link update"

        def id = 1

        def updatedLink =
                new Link(
                        id: id, difficultyLevel: DifficultyLevel.RED, freeLink: false, transformationsOnly: false)

        def currentLink =
                new Link(difficultyLevel: DifficultyLevel.GREEN, freeLink: true, transformationsOnly: true)

        1 * linkRepository.findOne(id) >> currentLink

        when: "attempt to save link is made"

        def result = linkService.saveLink(updatedLink)

        then: "link is saved"

        1 * linkRepository.save(_ as Link) >> { it[0] }

        and: "saved link contains updated details"

        with(result) {
            difficultyLevel == updatedLink.difficultyLevel
            freeLink == updatedLink.freeLink
            transformationsOnly == updatedLink.transformationsOnly
            lastUpdate != null
        }
    }
}
