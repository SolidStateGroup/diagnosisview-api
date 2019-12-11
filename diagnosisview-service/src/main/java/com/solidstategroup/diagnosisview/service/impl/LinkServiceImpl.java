package com.solidstategroup.diagnosisview.service.impl;

import com.solidstategroup.diagnosisview.exceptions.BadRequestException;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.codes.LinkRuleMapping;
import com.solidstategroup.diagnosisview.model.codes.Lookup;
import com.solidstategroup.diagnosisview.model.codes.enums.DifficultyLevel;
import com.solidstategroup.diagnosisview.repository.LinkRepository;
import com.solidstategroup.diagnosisview.repository.LinkRuleMappingRepository;
import com.solidstategroup.diagnosisview.repository.LookupRepository;
import com.solidstategroup.diagnosisview.repository.LookupTypeRepository;
import com.solidstategroup.diagnosisview.service.LinkRuleService;
import com.solidstategroup.diagnosisview.service.LinkService;
import com.solidstategroup.diagnosisview.service.LogoRulesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.compareIgnoreCase;

@Service
@Slf4j
public class LinkServiceImpl implements LinkService {

    private static final String LINK_SEQ = "link_seq";

    private final EntityManager entityManager;
    private final LinkRuleService linkRuleService;
    private final LinkRuleMappingRepository linkRuleMappingRepository;
    private final LinkRepository linkRepository;
    private final LookupRepository lookupRepository;
    private final LookupTypeRepository lookupTypeRepository;
    private final LogoRulesService logoRulesService;
    private Lookup niceLinksLookup;
    private Lookup userLink;

    public LinkServiceImpl(
            EntityManager entityManager,
            LinkRuleService linkRuleService,
            LinkRuleMappingRepository linkRuleMappingRepository,
            LinkRepository linkRepository,
            LookupRepository lookupRepository,
            LookupTypeRepository lookupTypeRepository,
            LogoRulesService logoRulesService) {

        this.entityManager = entityManager;
        this.linkRuleService = linkRuleService;
        this.linkRuleMappingRepository = linkRuleMappingRepository;
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
        return linkRepository.findById(id).orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CacheEvict(value = {"getAllCodes", "getAllCategories"}, allEntries = true)
    public Link update(Link link) {

        Link existingLink = linkRepository.findById(link.getId())
                .orElseThrow(() -> new BadRequestException("The link does not exist within DiagnosisView."));

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
        // existingLink.setMappingLinks(new HashSet<>());

        linkRepository.save(existingLink);

        return existingLink;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Link upsert(Link link) {

        if (!StringUtils.isEmpty(link.getLink()) && !link.getLink().startsWith("http")) {
            log.error(" Link url not formatted correctly {} {} ", link.getId(), link.getLink());
        }

        //Get the NICE lookup if it exists
        populatDVLookups();

        link = checkLink(link);

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
        if (niceLinksLookup != null &&
                link.getLinkType().getId().equals(niceLinksLookup.getId())) {

            link.setLinkType(userLink);

        } else {

            lookupTypeRepository.save(link.getLinkType().getLookupType());
            lookupRepository.save(link.getLinkType());
        }

        return linkRepository.save(link);
    }

    public Link addExternalLink(Link link, Code code) throws Exception {

        if (link.getLinkType() == null) {

            throw new Exception("link type must be set");
        }

        if (link.getName() == null) {

            throw new Exception("link must have name set");
        }

        if (link.getExternalId() == null) {

            throw new Exception("link must have an external id set");
        }

        link.setOriginalLink(link.getLink());

        logoRulesService
                .matchLinkToRule(link)
                .ifPresent(link::setLogoRule);

        Date now = new Date();

        link.setCode(code);
        link.setDifficultyLevel(DifficultyLevel.AMBER);
        link.setDisplayLink(true);
        if(link.getDisplayOrder() == null){
            link.setDisplayOrder(1);
        }
        link.setTransformationsOnly(false);
        link.setFreeLink(false);
        link.setCreated(now);
        link.setLastUpdate(now);

        link.setId(selectIdFrom(LINK_SEQ));

        return linkRepository.save(link);
    }

    @Override
    public Link updateExternalLink(Link link) throws Exception {

        if (link.getExternalId() == null) {

            throw new Exception("link must have an external id set");
        }

        Link savedLink = linkRepository.findLinkByExternalId(link.getExternalId());

        if (savedLink == null) {

            throw new Exception("no link found");
        }

        savedLink.setName(link.getName());
        savedLink.setLink(link.getLink());

        return linkRepository.save(savedLink);
    }

    @Override
    public void updateExternalLinks(Link link) throws Exception {

        if (link.getExternalId() == null) {
            throw new Exception("link must have an external id set");
        }

        // make sure to ignore removed_externally = true
        List<Link> savedLinks = linkRepository.findLinksByExternalId(link.getExternalId());

        if (CollectionUtils.isEmpty(savedLinks)) {
            throw new Exception("no links found");
        }

        savedLinks.forEach(l -> {
            l.setName(link.getName());
            l.setLink(link.getLink());
            linkRepository.save(l);
        });
    }

    /**
     * Check an existing link and see if it has the difficulty set etc
     *
     * @param link
     * @return
     */
    private Link checkLink(Link link) {

        Link existingLink = linkRepository.findById(link.getId())
                .orElse(null);

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

            if (!CollectionUtils.isEmpty(existingLink.getMappingLinks())) {
                link.setMappingLinks(existingLink.getMappingLinks());
            }

            if (compareIgnoreCase(existingLink.getLink(), link.getLink()) != 0
                    && existingLink.getMappingLinks() != null) {

                Set<LinkRuleMapping> mappings = linkRuleService
                        .matchLinkToRule(link);

                if (mappings.size() > 0) {

                    linkRuleMappingRepository.deleteAll(existingLink.getMappingLinks());
                    link.setMappingLinks(mappings);
                    linkRuleMappingRepository.saveAll(mappings);
                }
            }
        }

        //If the link is a NICE link, we should categorise it as such
        //In the future this maybe extended into its own function
        if (link.getLink() != null &&
                link.getLink().contains("nice.org.uk")) {

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

    private long selectIdFrom(String sequence) {
        String sql = "SELECT nextval('" + sequence + "')";
        return ((BigInteger) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
    }
}



