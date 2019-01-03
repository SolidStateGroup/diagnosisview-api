package com.solidstategroup.diagnosisview.service.impl;

import com.solidstategroup.diagnosisview.exceptions.BadRequestException;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.codes.Lookup;
import com.solidstategroup.diagnosisview.model.codes.enums.DifficultyLevel;
import com.solidstategroup.diagnosisview.repository.LinkRepository;
import com.solidstategroup.diagnosisview.repository.LookupRepository;
import com.solidstategroup.diagnosisview.repository.LookupTypeRepository;
import com.solidstategroup.diagnosisview.service.LinkService;
import com.solidstategroup.diagnosisview.service.LogoRulesService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;

@Service
public class LinkServiceImpl implements LinkService {

    private final LinkRepository linkRepository;
    private final LookupRepository lookupRepository;
    private final LookupTypeRepository lookupTypeRepository;
    private final LogoRulesService logoRulesService;
    private Lookup niceLinksLookup;
    private Lookup userLink;

    public LinkServiceImpl(
            LinkRepository linkRepository,
            LookupRepository lookupRepository,
            LookupTypeRepository lookupTypeRepository,
            LogoRulesService logoRulesService) {

        this.linkRepository = linkRepository;
        this.lookupRepository = lookupRepository;
        this.lookupTypeRepository = lookupTypeRepository;
        this.logoRulesService = logoRulesService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Link get(Long id) {
        return linkRepository.findOne(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CacheEvict(value = {"getAllCodes", "getAllCategories"}, allEntries = true)
    public Link update(Link link) {

        Link existingLink = linkRepository.findOne(link.getId());

        if (existingLink == null) {

            throw new BadRequestException("The link does not exist within DiagnosisView.");
        }

        //Currently you can only update certain fields
        if (link.hasDifficultyLevelSet()) {

            existingLink.setDifficultyLevel(link.getDifficultyLevel());
        }

        if (link.hasFreeLinkSet()) {

            existingLink.setFreeLink(link.getFreeLink());
        }

        if (link.hasTransformationOnly()) {

            existingLink.setTransformationsOnly(link.getTransformationsOnly());
        }

        if (link.getDisplayOrder() != null) {

            existingLink.setDisplayOrder(link.getDisplayOrder());
        }

        existingLink.setLastUpdate(new Date());

        existingLink.setMappingLinks(new HashSet<>());

        linkRepository.save(existingLink);

        existingLink.setMappingLinks(null);
        existingLink.setLogoRule(null);

        return existingLink;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Link upsert(Link link) {

        //Get the NICE lookup if it exists
        populatDVLookups();

        Link existingLink = linkRepository.findOne(link.getId());
        link = checkLink(existingLink, link);

        // Check if the link matches any urls for logos,
        // if it does, assign it that logo url
        logoRulesService
                .matchLinkToRule(link)
                .ifPresent(link::setLogoRule);

        if (link.getLinkType() == null) {

            link.setLinkType(userLink);
        }

        // If the lookupValue is a DV only value, then don't update as it will overlap
        // In future this may need to be a check against all DV only lookup values
        if (niceLinksLookup != null ||
                link.getLinkType().getId().equals(niceLinksLookup.getId())) {

            link.setLinkType(userLink);

        } else {

            lookupTypeRepository.save(link.getLinkType().getLookupType());
            lookupRepository.save(link.getLinkType());
        }

        return linkRepository.save(link);
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

            if (existingLink.getTransformationsOnly()) {
                link.setTransformationsOnly(existingLink.getTransformationsOnly());
            }
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
}



