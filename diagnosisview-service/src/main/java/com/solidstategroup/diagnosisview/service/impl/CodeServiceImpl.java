package com.solidstategroup.diagnosisview.service.impl;


import com.solidstategroup.diagnosisview.model.CategoryDto;
import com.solidstategroup.diagnosisview.model.CodeDto;
import com.solidstategroup.diagnosisview.model.LinkDto;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.repository.CategoryRepository;
import com.solidstategroup.diagnosisview.repository.CodeRepository;
import com.solidstategroup.diagnosisview.repository.LinkMappingRepository;
import com.solidstategroup.diagnosisview.repository.LinkRepository;
import com.solidstategroup.diagnosisview.service.CodeService;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import springfox.documentation.annotations.Cacheable;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
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
    private final LinkMappingRepository linkMappingRepository;

    public CodeServiceImpl(CodeRepository codeRepository,
                           CategoryRepository categoryRepository,
                           LinkRepository linkRepository,
                           LinkMappingRepository linkMappingRepository) {

        this.codeRepository = codeRepository;
        this.categoryRepository = categoryRepository;
        this.linkRepository = linkRepository;
        this.linkMappingRepository = linkMappingRepository;
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
    public List<CodeDto> getAllCodes() {

        return codeRepository.findAll()
                .parallelStream().map(code -> CodeDto
                        .builder()
                        .code(code.getCode())
                        .links(buildLinks(code))
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

    private Set<LinkDto> buildLinks(Code code) {
        return code.getLinks().stream().map(link ->
                new LinkDto(link.getId(), link.getLinkType(), link.getDifficultyLevel(),
                        link.getLink(), link.getName(), link.getFreeLink()))
                .collect(toSet());
    }

    private Set<CategoryDto> buildCategories(Code code) {
        return code.getCodeCategories().stream().map(cc ->
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
    public Link saveLink(Link link) {
        Link existingLink = linkRepository.findOne(link.getId());
        //Currently you can only update certain fields
        if (link.hasDifficultyLevelSet()) {
            existingLink.setDifficultyLevel(link.getDifficultyLevel());
        }
        if (link.hasFreeLinkSet()) {
            existingLink.setFreeLink(link.getFreeLink());
        }
        existingLink.setLastUpdate(new Date());

        return linkRepository.save(existingLink);
    }

    @Override
    public void delete(Code code) {
        codeRepository.delete(code);
    }

    @Override
    public Code save(Code code) {
        return codeRepository.save(code);
    }
}
