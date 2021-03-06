package com.solidstategroup.diagnosisview.service.impl

import com.solidstategroup.diagnosisview.exceptions.BadRequestException
import com.solidstategroup.diagnosisview.model.codes.Category
import com.solidstategroup.diagnosisview.model.codes.Code
import com.solidstategroup.diagnosisview.model.codes.CodeCategory
import com.solidstategroup.diagnosisview.model.codes.CodeExternalStandard
import com.solidstategroup.diagnosisview.model.codes.ExternalStandard
import com.solidstategroup.diagnosisview.model.codes.Link
import com.solidstategroup.diagnosisview.model.codes.Lookup
import com.solidstategroup.diagnosisview.model.codes.LookupType
import com.solidstategroup.diagnosisview.model.codes.enums.LookupTypes
import com.solidstategroup.diagnosisview.model.codes.enums.CodeSourceTypes
import com.solidstategroup.diagnosisview.repository.CategoryRepository
import com.solidstategroup.diagnosisview.repository.CodeCategoryRepository
import com.solidstategroup.diagnosisview.repository.CodeExternalStandardRepository
import com.solidstategroup.diagnosisview.repository.CodeRepository
import com.solidstategroup.diagnosisview.repository.ExternalStandardRepository
import com.solidstategroup.diagnosisview.repository.LinkRepository
import com.solidstategroup.diagnosisview.repository.LookupRepository
import com.solidstategroup.diagnosisview.repository.LookupTypeRepository
import com.solidstategroup.diagnosisview.service.CodeService
import com.solidstategroup.diagnosisview.service.LinkService
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.Query

class CodeServiceImplTest extends Specification {

    def codeRepository = Mock(CodeRepository)
    def categoryRepository = Mock(CategoryRepository)
    def codeCategoryRepository = Mock(CodeCategoryRepository)
    def codeExternalStandardRepository = Mock(CodeExternalStandardRepository)
    def externalStandardRepository = Mock(ExternalStandardRepository)
    def linkRepository = Mock(LinkRepository)
    def linkService = Mock(LinkService)
    def lookupTypeRepository = Mock(LookupTypeRepository)
    def lookupRepository = Mock(LookupRepository)
    def entityManager = Mock(EntityManager)

    CodeService codeService = new CodeServiceImpl(
            codeRepository,
            categoryRepository,
            codeCategoryRepository,
            codeExternalStandardRepository,
            externalStandardRepository,
            linkRepository,
            linkService,
            lookupTypeRepository,
            lookupRepository,
            entityManager)

    def "should fetch all categories"() {

        given: "a list of categories"

        1 * categoryRepository.findAll() >> [
                buildHeartCategory(),
                buildGynaecologyCategory()
        ]

        when: "all categories are fetch"

        def categories = codeService.getAllCategories()

        then: "correct number of categories are returned"

        categories.size() == 2
    }

    def "should find one code"() {

        given: "a stored code"

        def code = "acne"

        1 * codeRepository.findOneByCode(code) >> new Code(code: code)

        when: "code is fetched by codeName"

        def result = codeService.get(code)

        then: "correct code object is returned"

        result.code == code
    }

    def "should delete code"() {

        given: "a code to delete"

        def codeName = "dv_test_code"
        def code = new Code(code: codeName)

        when: "service is called"

        codeService.delete(code)

        then: "current code is found"

        1 * codeRepository.findOneByCode(codeName) >> new Code(sourceType: CodeSourceTypes.DIAGNOSISVIEW)

        and: "code is deleted"

        1 * codeRepository.delete(_ as Code)
    }

    def "should throw exception when trying to delete non-dv code"() {

        given: "a pv generated code to delete"

        def codeName = "pv_code"
        def code = new Code(code: codeName)


        when: "service is called"

        codeService.delete(code)

        then: "currently stored pv code is found"

        1 * codeRepository.findOneByCode(codeName) >> new Code(sourceType: CodeSourceTypes.PATIENTVIEW)

        and: "exception is thrown"

        thrown BadRequestException
    }

    def "should throw exception when code name not set"() {

        given: "a code without a code name"

        def code = new Code()

        when: "service is called"

        codeService.delete(code)

        then: "exception is thrown"

        thrown BadRequestException
    }

    def "should throw exception when code name not found"() {

        given: "a code that is not saved in database"

        def code = new Code(code: "dv_test")

        when: "service is called"

        codeService.delete(code)

        then: "exception is thrown"

        thrown BadRequestException
    }

    def "should store code"() {

        given: "a new code"

        def code = new Code()

        when: "service is called"

        codeService.save(code)

        then: "code is stored"

        1 * codeRepository.save(code)
    }

    def "should create new code from sync"() {

        given: "a new code"

        def standardType = new LookupType(id: 1, type: LookupTypes.CODE_STANDARD)
        def standardLookup = new Lookup(id: 2, lookupType: standardType)
        def codeType = new LookupType(id: 3, type: LookupTypes.CODE_TYPE)
        def codeLookup = new Lookup(id: 4, lookupType: codeType)
        def heartCategory = buildHeartCategory()
        def gynaecologyCategory = buildGynaecologyCategory()
        def externalStanard = new ExternalStandard(name: "externalStandard")

        def syncCode = new Code(
                links: [new Link(id: 1), new Link(id: 2)],
                standardType: standardLookup,
                codeType: codeLookup)

        def gynaecologyCodeCategory = new CodeCategory(id: 1, code: syncCode, category: gynaecologyCategory)
        def heartCodeCategory = new CodeCategory(id: 2, code: syncCode, category: heartCategory)
        def codeExternalStandard = new CodeExternalStandard(syncCode, externalStanard)

        syncCode.codeCategories.addAll([gynaecologyCodeCategory, heartCodeCategory])
        syncCode.externalStandards.add(codeExternalStandard)

        when: "upsert is called"

        def code = codeService.upsert(syncCode)

        then: "created code is returned"

        code != null

        and: "correct data is stored"

        1 * lookupTypeRepository.save(standardType)
        1 * lookupTypeRepository.save(codeType)

        1 * lookupRepository.save(standardLookup)
        1 * lookupRepository.save(codeLookup)

        1 * categoryRepository.save(gynaecologyCategory)
        1 * categoryRepository.save(heartCategory)

        1 * codeCategoryRepository.save(gynaecologyCodeCategory)
        1 * codeCategoryRepository.save(heartCodeCategory)

        1 * codeExternalStandardRepository.save(codeExternalStandard)

        2 * linkService.upsert(_ as Link) >> { it[0] }

        2 * codeRepository.save(_ as Code) >> { it[0] }
    }

    def "should return null when upsert not required"() {

        given: "codes are the same"

        def id = 1
        def code = new Code(id: id, lastUpdate: new Date(150, 1, 1))
        def currentCode = new Code(id: id, lastUpdate: new Date(150, 1, 1))

        1 * codeRepository.findOne(id) >> currentCode

        when: "upsert is called"

        def result = codeService.upsert(code)

        then: "result is null"

        result == null
    }

    def "should create new code not from sync"() {

        given:

        def codeStr = "liver_disease_(alcoholic)"
        def newCode  = new Code(code: codeStr)
        def query = Mock(Query) {

            setParameter(_, _) >> it
            getSingleResult() >> BigInteger.valueOf(100L)
        }

        when:

        def result = codeService.upsert(newCode)

        then:

        2 * codeRepository.save(_ as Code) >> { it[0] }

        entityManager.createNativeQuery(_ as String) >> query

        with(result) {
            code == "dv_" + codeStr
        }
    }

    def buildHeartCategory() {
        new Category(
                id: 5,
                number: 9,
                hidden: false,
                friendlyDescription: "Heart and blood vessels",
                icd10Description: "Diseases of the circulatory system (I00-I99)")
    }

    def buildGynaecologyCategory() {
        new Category(
                id: 6,
                number: 141,
                hidden: false,
                friendlyDescription: "Breast, gynaecology (N60-98)",
                icd10Description: "Seems sensible to split 14 into kidney/bladder vs female reproductive")
    }
}
