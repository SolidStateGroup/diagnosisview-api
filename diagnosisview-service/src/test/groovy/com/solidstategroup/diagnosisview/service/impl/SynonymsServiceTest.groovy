package com.solidstategroup.diagnosisview.service.impl

import spock.lang.Specification

class SynonymsServiceTest extends Specification {

    SynonymsService synonymsService = new SynonymsService()

    def "Test Synonyms search"() {

        given: "search term"

            def term = "cardio"

        when: "Search for synonyms"
            def result  = synonymsService.searchSynonyms(term)

        then: "all good"
            result.size() > 1
    }
}
