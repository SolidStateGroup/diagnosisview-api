package com.solidstategroup.diagnosisview.service.impl;


import com.solidstategroup.diagnosisview.model.CategoryDto;
import com.solidstategroup.diagnosisview.model.CodeDto;
import com.solidstategroup.diagnosisview.model.LinkDto;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.codes.LinkRuleMapping;
import com.solidstategroup.diagnosisview.model.codes.enums.CriteriaType;
import com.solidstategroup.diagnosisview.model.codes.enums.Institution;
import com.solidstategroup.diagnosisview.repository.CategoryRepository;
import com.solidstategroup.diagnosisview.repository.CodeRepository;
import com.solidstategroup.diagnosisview.repository.LinkRepository;
import com.solidstategroup.diagnosisview.service.CodeService;
import lombok.extern.java.Log;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * {@inheritDoc}.
 */
@Log
@Service
public class CodeServiceImpl implements CodeService {

    private final CodeRepository codeRepository;
    private final CategoryRepository categoryRepository;
    private final LinkRepository linkRepository;

    public CodeServiceImpl(CodeRepository codeRepository,
                           CategoryRepository categoryRepository,
                           LinkRepository linkRepository) {

        this.codeRepository = codeRepository;
        this.categoryRepository = categoryRepository;
        this.linkRepository = linkRepository;
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
                .filter(r -> r.getCriteriaType() == CriteriaType.INSTITUTION)
                .filter(r -> Institution.valueOf(r.getCriteria()) == institution)
                .findFirst()
                .map(LinkRuleMapping::getReplacementLink);
    }

    private static boolean shouldDisplayLink(Optional<String> linkMapping, Link link) {
        if (linkMapping.isPresent() | !link.useTransformationsOnly()) {
            return true;
        }

        return false;
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

    @Override
    public Code getCode(String code) {
        return codeRepository.findOneByCode(code);
    }

    @Override
    public Link getLink(Long id) {
        return linkRepository.findOne(id);
    }

    @Override
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public Link saveLink(Link link) {
        Link existingLink = linkRepository.findOne(link.getId());
        //Currently you can only update certain fields
        if (link.hasDifficultyLevelSet()) {
            existingLink.setDifficultyLevel(link.getDifficultyLevel());
        }

        if (link.hasFreeLinkSet()) {
            existingLink.setFreeLink(link.getFreeLink());
        }

        if (link.hasTransformationOnly()) {
            existingLink.setTransformationsOnly(link.useTransformationsOnly());

        }

        existingLink.setLastUpdate(new Date());

        return linkRepository.save(existingLink);
    }

    @Override
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public void delete(Code code) {
        codeRepository.delete(code);
    }

    @Override
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public Code save(Code code) {
        return codeRepository.save(code);
    }
}
