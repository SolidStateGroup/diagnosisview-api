package com.solidstategroup.diagnosisview.service.impl;

import com.google.api.client.util.Lists;
import com.solidstategroup.diagnosisview.model.CategoryDto;
import com.solidstategroup.diagnosisview.model.CodeDto;
import com.solidstategroup.diagnosisview.model.LinkDto;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.CodeCategory;
import com.solidstategroup.diagnosisview.model.codes.CodeExternalStandard;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.codes.LinkRuleMapping;
import com.solidstategroup.diagnosisview.model.codes.LogoRule;
import com.solidstategroup.diagnosisview.model.codes.Lookup;
import com.solidstategroup.diagnosisview.model.codes.enums.DifficultyLevel;
import com.solidstategroup.diagnosisview.model.codes.enums.Institution;
import com.solidstategroup.diagnosisview.repository.CategoryRepository;
import com.solidstategroup.diagnosisview.repository.CodeCategoryRepository;
import com.solidstategroup.diagnosisview.repository.CodeExternalStandardRepository;
import com.solidstategroup.diagnosisview.repository.CodeRepository;
import com.solidstategroup.diagnosisview.repository.ExternalStandardRepository;
import com.solidstategroup.diagnosisview.repository.LinkRepository;
import com.solidstategroup.diagnosisview.repository.LogoRuleRepository;
import com.solidstategroup.diagnosisview.repository.LookupRepository;
import com.solidstategroup.diagnosisview.repository.LookupTypeRepository;
import com.solidstategroup.diagnosisview.service.CodeService;
import com.solidstategroup.diagnosisview.service.LinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * {@inheritDoc}.
 */
@Slf4j
@Service
public class CodeServiceImpl implements CodeService {

    private final CodeRepository codeRepository;
    private final CategoryRepository categoryRepository;
    private final CodeCategoryRepository codeCategoryRepository;
    private final CodeExternalStandardRepository codeExternalStandardRepository;
    private final ExternalStandardRepository externalStandardRepository;
    private final LookupTypeRepository lookupTypeRepository;
    private final LookupRepository lookupRepository;
    private final LinkService linkService;

    public CodeServiceImpl(CodeRepository codeRepository,
                           CategoryRepository categoryRepository,
                           CodeCategoryRepository codeCategoryRepository,
                           CodeExternalStandardRepository codeExternalStandardRepository,
                           ExternalStandardRepository externalStandardRepository,
                           LinkService linkService,
                           LookupTypeRepository lookupTypeRepository,
                           LookupRepository lookupRepository) {

        this.codeRepository = codeRepository;
        this.categoryRepository = categoryRepository;
        this.codeCategoryRepository = codeCategoryRepository;
        this.codeExternalStandardRepository = codeExternalStandardRepository;
        this.externalStandardRepository = externalStandardRepository;
        this.linkService = linkService;
        this.lookupTypeRepository = lookupTypeRepository;
        this.lookupRepository = lookupRepository;
    }

    private static boolean shouldDisplayLink(Optional<String> linkMapping, Link link) {

        return linkMapping.isPresent() | !link.useTransformationsOnly();
    }

    @Override
    @Cacheable("getAllCategories")
    public List<CategoryDto> getAllCategories() {

        return categoryRepository
                .findAll()
                .stream()
                .map(category -> new CategoryDto(category.getNumber(),
                        category.getIcd10Description(),
                        category.getFriendlyDescription(),
                        category.isHidden()))
                .collect(toList());
    }

    @Override
    @Cacheable("getAllCodes")
    public List<CodeDto> getAllCodes(Institution institution) {

        return codeRepository
                .findAll()
                .parallelStream()
                .map(code -> CodeDto
                        .builder()
                        .code(code.getCode())
                        .links(buildLinkDtos(code, institution))
                        .categories(buildCategories(code))
                        .deleted(shouldBeDeleted(code))
                        .friendlyName(code.getPatientFriendlyName())
                        .build())
                .sorted(Comparator.comparing(CodeDto::getFriendlyName,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .collect(toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Code getCode(String code) {
        return codeRepository.findOneByCode(code);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CacheEvict(value = {"getAllCodes", "getAllCategories"}, allEntries = true)
    public void delete(Code code) {
        codeRepository.delete(code);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CacheEvict(value = {"getAllCodes", "getAllCategories"}, allEntries = true)
    public Code save(Code code) {
        return codeRepository.save(code);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Code upsertCode(Code code, boolean fromSync) {

        // If the code is from dv web, then we append dv_ to the code so its unique.
        if (!fromSync) {
            if (!code.getCode().substring(0, 3).equals("dv_")) {
                code.setCode(String.format("dv_%s", code.getCode()));
            }

            // Set the last update date to now
            code.setLastUpdate(new Date());
        }

        Code existingCode = codeRepository.findOne(code.getId());

        if (!codeRequiresUpdate(existingCode, code)) {
            return null;
        }

        // The following are all items that wont be sent with the web creation
        if (fromSync) {
            saveAdditionalSyncObjects(code);
        }

        Set<Link> links = code.getLinks();
        Set<CodeCategory> codeCategories = code.getCodeCategories();
        Set<CodeExternalStandard> externalStandards = code.getExternalStandards();

        // Remove code related fields, as PV already provides ids if the
        // objects have not already been saved to the repository jpa will thrown
        // an exception because it can't the the non-existent ids in the db.
        code.setLinks(new HashSet<>());
        code.setCodeCategories(new HashSet<>());
        code.setExternalStandards(new HashSet<>());

        codeRepository.save(code);

        codeCategories
                .forEach(cc -> {
                    cc.setCode(code);
                    codeCategoryRepository.save(cc);
                });

        code.setCodeCategories(codeCategories);

        externalStandards
                .forEach(es -> {
                    es.setCode(code);
                    codeExternalStandardRepository.save(es);
                });

        code.setExternalStandards(externalStandards);

        links.forEach(link -> {
            link.setCode(code);
            linkService.upsertLink(link);
        });

        code.setLinks(links);

        return codeRepository.save(code);
    }

    private boolean codeRequiresUpdate(Code currentCode, Code code) {

        //If there is a code, or it has been updated, update
        return currentCode == null || currentCode.getLinks().size() != code.getLinks().size() ||
                currentCode.getExternalStandards().size() != code.getExternalStandards().size() ||
                currentCode.getCodeCategories().size() != code.getCodeCategories().size() ||
                currentCode.getLastUpdate().before(code.getLastUpdate());
    }

    private void saveAdditionalSyncObjects(Code code) {

        lookupTypeRepository.save(code.getStandardType().getLookupType());
        lookupRepository.save(code.getStandardType());

        lookupTypeRepository.save(code.getCodeType().getLookupType());
        lookupRepository.save(code.getCodeType());

        code.getCodeCategories()
                .forEach(cc -> categoryRepository.save(cc.getCategory()));

        code.getExternalStandards()
                .forEach(es -> externalStandardRepository.save(es.getExternalStandard()));
    }

    private boolean shouldBeDeleted(Code code) {
        return code.isRemovedExternally() || code.isHideFromPatients();
    }

    private Set<LinkDto> buildLinkDtos(Code code, Institution institution) {

        return code
                .getLinks()
                .stream()
                .map(link -> {
                    Optional<String> linkMapping = buildLink(link.getMappingLinks(), institution);
                    return new LinkDto(link.getId(), link.getLinkType(),
                            link.getDifficultyLevel(),
                            linkMapping.orElse(link.getLink()),
                            shouldDisplayLink(linkMapping, link),
                            link.getName(), link.getFreeLink());
                })
                .collect(toSet());
    }

    private Optional<String> buildLink(
            Set<LinkRuleMapping> linkRuleMapping,
            Institution institution) {

        return linkRuleMapping
                .stream()
                .filter(r -> r.getInstitution() == institution)
                .findFirst()
                .map(LinkRuleMapping::getReplacementLink);
    }

    private Set<CategoryDto> buildCategories(Code code) {

        return code
                .getCodeCategories()
                .stream()
                .map(cc ->
                        new CategoryDto(cc.getCategory().getNumber(),
                                cc.getCategory().getIcd10Description(),
                                cc.getCategory().getFriendlyDescription(),
                                cc.getCategory().isHidden()))
                .collect(toSet());
    }
}
