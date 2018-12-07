package com.solidstategroup.diagnosisview.service.impl;


import com.solidstategroup.diagnosisview.model.CategoryDto;
import com.solidstategroup.diagnosisview.model.CodeDto;
import com.solidstategroup.diagnosisview.model.LinkDto;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.CodeCategory;
import com.solidstategroup.diagnosisview.model.codes.CodeExternalStandard;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.codes.LinkRuleMapping;
import com.solidstategroup.diagnosisview.model.codes.Lookup;
import com.solidstategroup.diagnosisview.model.codes.enums.DifficultyLevel;
import com.solidstategroup.diagnosisview.model.codes.enums.Institution;
import com.solidstategroup.diagnosisview.repository.CategoryRepository;
import com.solidstategroup.diagnosisview.repository.CodeCategoryRepository;
import com.solidstategroup.diagnosisview.repository.CodeExternalStandardRepository;
import com.solidstategroup.diagnosisview.repository.CodeRepository;
import com.solidstategroup.diagnosisview.repository.ExternalStandardRepository;
import com.solidstategroup.diagnosisview.repository.LinkRepository;
import com.solidstategroup.diagnosisview.repository.LookupRepository;
import com.solidstategroup.diagnosisview.repository.LookupTypeRepository;
import com.solidstategroup.diagnosisview.service.CodeService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
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
@Log
@Service
public class CodeServiceImpl implements CodeService {

    @Autowired
    private CodeRepository codeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CodeCategoryRepository codeCategoryRepository;

    @Autowired
    private CodeExternalStandardRepository codeExternalStandardRepository;

    @Autowired
    private ExternalStandardRepository externalStandardRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private LookupTypeRepository lookupTypeRepository;

    @Autowired
    private LookupRepository lookupRepository;

    private Lookup niceLinksLookup;
    private Lookup userLink;


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

    @Override
    public Code getCode(String code) {
        return codeRepository.findOneByCode(code);
    }

    @Override
    public Link getLink(Long id) {
        return linkRepository.findOne(id);
    }

    @Override
    @CacheEvict(value = {"getAllCodes", "getAllCategories"}, allEntries = true)
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
    @CacheEvict(value = {"getAllCodes", "getAllCategories"}, allEntries = true)
    public void delete(Code code) {
        codeRepository.delete(code);
    }

    @Override
    @CacheEvict(value = {"getAllCodes", "getAllCategories"}, allEntries = true)
    public Code save(Code code) {
        return codeRepository.save(code);
    }


    /**
     * Create or update a code creating all the requestite data
     *
     * @param code
     */
    @Override
    public Code createOrUpdateCode(Code code, boolean fromSync) {
        //Get the NICE lookup if it exists
        populatDVLookups();

        Code existingCode = codeRepository.findOne(code.getId());

        //If the code is from dv web, then we append dv_ to the code so its unique.
        if (!fromSync) {
            if (!code.getCode().substring(0, 3).equals("dv_")) {
                code.setCode(String.format("dv_%s", code.getCode()));
            }

            //Set the last update date to now
            code.setLastUpdate(new Date());
        }


        //If there is a code, or it has been updated, update
        if (existingCode == null || existingCode.getLinks().size() != code.getLinks().size() ||
                existingCode.getExternalStandards().size() != code.getExternalStandards().size() ||
                existingCode.getCodeCategories().size() != code.getCodeCategories().size() ||
                existingCode.getLastUpdate().before(code.getLastUpdate())) {


            //The following are all items that wont be sent with the web creation
            if (fromSync) {
                lookupTypeRepository.save(code.getStandardType().getLookupType());
                lookupRepository.save(code.getStandardType());

                lookupTypeRepository.save(code.getCodeType().getLookupType());
                lookupRepository.save(code.getCodeType());

                for (CodeCategory codeCategory : code.getCodeCategories()) {
                    categoryRepository.save(codeCategory.getCategory());
                }

                //Check if code category exists
                for (CodeExternalStandard externalStandard : code.getExternalStandards()) {
                    externalStandardRepository.save(externalStandard.getExternalStandard());
                }
            }

            //Check if code category exists
            for (Link link : code.getLinks()) {
                Link existingLink = linkRepository.findOne(link.getId());
                link = checkLink(existingLink, link);

                //If the lookupValue is a DV only value, then dont save as it will overlap
                //In future this may need to be a check against all DV only lookup values
                if (link.getLinkType().getId().equals(niceLinksLookup.getId())) {
                    link.setLinkType(userLink);
                } else {
                    lookupTypeRepository.save(link.getLinkType().getLookupType());
                    lookupRepository.save(link.getLinkType());
                }
            }

            Set<Link> links = code.getLinks();
            Set<CodeCategory> codeCategories = code.getCodeCategories();
            Set<CodeExternalStandard> externalStandards = code.getExternalStandards();

            //Remove code related fields, this stops exceptions being thrown
            code.setLinks(new HashSet<>());
            code.setCodeCategories(new HashSet<>());
            code.setExternalStandards(new HashSet<>());

            codeRepository.save(code);

            //Add in the code categories
            for (CodeCategory codeCategory : codeCategories) {
                codeCategory.setCode(code);
                codeCategoryRepository.save(codeCategory);
            }

            //Add in code related external standards
            for (CodeExternalStandard externalStandard : externalStandards) {
                externalStandard.setCode(code);
                codeExternalStandardRepository.save(externalStandard);
            }

            //Add in code specific links
            for (Link link : links) {
                Link existingLink = linkRepository.findOne(link.getId());

                link = checkLink(existingLink, link);
                link.setCode(code);
                linkRepository.save(link);
            }

            code.setLinks(links);
            code.setCodeCategories(codeCategories);
            code.setExternalStandards(externalStandards);

            codeRepository.save(code);

            return code;
        } else {
            return null;
        }
    }


    /**
     * Check an existing link and see if it has the difficulty set etc
     *
     * @param existingLink
     * @param link
     * @return
     */
    private Link checkLink(Link existingLink, Link link) {
        //Ensure that difficulty is not overwritten
        if (existingLink != null) {
            if (existingLink.hasDifficultyLevelSet()) {
                link.setDifficultyLevel(existingLink.getDifficultyLevel());
            }

            if (existingLink.hasFreeLinkSet()) {
                link.setFreeLink(existingLink.getFreeLink());
            }

            if (existingLink.useTransformationsOnly()) {
                link.setTransformationsOnly(existingLink.useTransformationsOnly());
            }

        }

        if (existingLink == null) {
            link.setFreeLink(false);
        }

        //If the link is a NICE link, we should categorise it as such
        //In the future this maybe extended into its own function
        if (link.getLink() != null && link.getLink().contains("nice.org.uk")) {
            link.setLinkType(niceLinksLookup);
            if (existingLink == null || !existingLink.hasDifficultyLevelSet()) {
                link.setDifficultyLevel(DifficultyLevel.AMBER);
            }
        }

        return link;
    }


    /**
     * Populates the DiagnosisView specific lookup values
     */
    private void populatDVLookups() {
        if (niceLinksLookup == null) {
            niceLinksLookup = lookupRepository.findOneByValue("NICE_CKS");
        }

        if (userLink == null) {
            userLink = lookupRepository.findOneByValue("CUSTOM");
        }
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

}
