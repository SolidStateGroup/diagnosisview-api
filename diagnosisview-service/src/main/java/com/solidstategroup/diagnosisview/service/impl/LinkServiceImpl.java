package com.solidstategroup.diagnosisview.service.impl;

import com.solidstategroup.diagnosisview.exceptions.BadRequestException;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.codes.LinkRuleMapping;
import com.solidstategroup.diagnosisview.model.codes.Lookup;
import com.solidstategroup.diagnosisview.model.codes.enums.DifficultyLevel;
import com.solidstategroup.diagnosisview.model.enums.LinkTypes;
import com.solidstategroup.diagnosisview.repository.LinkRepository;
import com.solidstategroup.diagnosisview.repository.LinkRuleMappingRepository;
import com.solidstategroup.diagnosisview.repository.LookupRepository;
import com.solidstategroup.diagnosisview.repository.LookupTypeRepository;
import com.solidstategroup.diagnosisview.service.LinkRuleService;
import com.solidstategroup.diagnosisview.service.LinkService;
import com.solidstategroup.diagnosisview.service.LogoRulesService;
import com.solidstategroup.diagnosisview.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.compareIgnoreCase;

@Service
@Slf4j
public class LinkServiceImpl implements LinkService {

    private static final String LINK_SEQ = "link_seq";
    private static Map<DifficultyLevel, Integer> defaultOrder = new HashMap<>();

    static {
        defaultOrder.put(DifficultyLevel.GREEN, 1);
        defaultOrder.put(DifficultyLevel.AMBER, 11);
        defaultOrder.put(DifficultyLevel.RED, 21);
    }

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
    public Link update(Link link) throws Exception {

        Link existingLink = linkRepository.findById(link.getId())
                .orElseThrow(() -> new BadRequestException("The link does not exist within DiagnosisView."));

        //Currently you can only update certain fields
        if (link.hasFreeLinkSet()) {
            existingLink.setFreeLink(link.getFreeLink());
        }

        if (link.hasTransformationOnly()) {
            existingLink.setTransformationsOnly(link.getTransformationsOnly());
        }

        // when changing difficulty level make sure new order is set
        if (link.hasDifficultyLevelSet()) {
            if (link.getDisplayOrder() == null) {
                throw new Exception("Please set Display Order for the link.");
            }
            validateLinkOrder(link, existingLink.getCode());
            existingLink.setDifficultyLevel(link.getDifficultyLevel());
        }

        // new Order set, validate link order rules
        if (link.getDisplayOrder() != null) {

            if (!link.hasDifficultyLevelSet()) {
                throw new Exception("Please set Difficulty level for the link.");
            }

            validateLinkOrder(link, existingLink.getCode());
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
    public Link upsert(Link link, Set<Link> codeLinks, boolean fromSync) {

        if (StringUtils.isEmpty(link.getLink())) {
            log.error(" Link url is null {}", link.getId());
            return null;
        }
        
        if (!StringUtils.isEmpty(link.getLink()) && !link.getLink().startsWith("http")) {
            log.error(" Link url not formatted correctly {} {} ", link.getId(), link.getLink());
        }

        //Get the NICE lookup if it exists
        populatDVLookups();

        link = checkLink(link, codeLinks, fromSync);

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

    @Override
    public Link addExternalLink(Link link, Code code) throws Exception {

        if (link.getLinkType() == null) {

            throw new Exception("link type must be set");
        }

        if (link.getName() == null) {

            throw new Exception("link must have name set");
        }

        if (link.getLinkType().getValue().equals(LinkTypes.BMJ.name())
                && link.getExternalId() == null) {

            throw new Exception("link must have an external id set");
        }

        if (link.getDifficultyLevel() == null) {

            throw new Exception("link must have difficulty level set");
        }

        link.setOriginalLink(link.getLink());

        logoRulesService
                .matchLinkToRule(link)
                .ifPresent(link::setLogoRule);

        Date now = new Date();

        link.setCode(code);
        link.setDisplayLink(true);
         // adding new Link, set Difficulty level and display order
        setLinkDisplayOrder(link, code.getLinks());
        link.setTransformationsOnly(false);
        link.setFreeLink(false);
        link.setCreated(now);
        link.setLastUpdate(now);
        link.setMappingLinks(new HashSet<>());

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
            l.setLastUpdate(new Date());
            linkRepository.save(l);
        });
    }

    /**
     * Helper to check Links order
     *
     * Rules as follow:
     * order 1-9 for Green
     * order 11-19 for Amber
     * order 21-29 for Red
     *
     * We need to make sure link order is within difficulty range and also is unique
     * per difficulty level.
     *
     * @param links links to check, could be new or existing ones
     *               @param code a code to check links against
     */
    /**
     * {@inheritDoc}
     */
    @Override
    public void checkLinksOrder(Set<Link> links, Code code) throws Exception {
        for (Link link : links) {
            validateLinkOrder(link, code);
        }
    }

    /**
     * Check an existing link and see if it has the difficulty set etc
     *
     * @param link      a link to check
     * @param codeLinks an list of Link objects from the Code this link belongs to
     * @param fromSync  if we executing from code sync
     * @return
     */
    private Link checkLink(Link link, Set<Link> codeLinks, boolean fromSync) {

        Link existingLink = linkRepository.findById(link.getId())
                .orElse(null);

        // Ensure that difficulty is not overwritten when executed from sync
        if (existingLink != null) {

            if (existingLink.hasDifficultyLevelSet() && fromSync) {
                link.setDifficultyLevel(existingLink.getDifficultyLevel());
            }

            if (existingLink.hasFreeLinkSet() && fromSync) {
                link.setFreeLink(existingLink.getFreeLink());
            }

            if (existingLink.getTransformationsOnly() && fromSync) {
                link.setTransformationsOnly(existingLink.getTransformationsOnly());
            }

            if (existingLink.getDisplayOrder() != null && fromSync) {
                link.setDisplayOrder(existingLink.getDisplayOrder());
            }

            if (!CollectionUtils.isEmpty(existingLink.getMappingLinks())) {
                link.setMappingLinks(existingLink.getMappingLinks());
            }

            // For existing Link if link url changed and we have link rules mapping
            // delete mappings first and re save them with new Link
            if (compareIgnoreCase(existingLink.getLink(), link.getLink()) != 0
                    && existingLink.getMappingLinks() != null) {

                Set<LinkRuleMapping> mappings = linkRuleService.matchLinkToRule(link);

                if (mappings.size() > 0) {
                    linkRuleMappingRepository.deleteAll(existingLink.getMappingLinks());
                    link.setMappingLinks(mappings);
                    linkRuleMappingRepository.saveAll(mappings);
                }
            }
        } else {
            // if we have a new Link build link rule mappings based on given link url
            Set<LinkRuleMapping> mappings = linkRuleService.matchLinkToRule(link);
            if (mappings.size() > 0) {
                link.setMappingLinks(mappings);
                // dont persist here, will be created when link saved
            }
        }

        // If the link is a NICE link, we should categorise it as such
        // In the future this maybe extended into its own function
        // if link is new we set difficulty level to AMBER and set Display Order based on rules
        if (link.getLink() != null && link.getLink().contains("nice.org.uk")) {

            link.setLinkType(niceLinksLookup);

            if (existingLink == null || !existingLink.hasDifficultyLevelSet()) {
                link.setDifficultyLevel(DifficultyLevel.AMBER);
                setLinkDisplayOrder(link, codeLinks);
            }
        }

        return link;
    }

    /**
     * Populates the DiagnosisView specific lookup values
     */
    private void populatDVLookups() {

        if (niceLinksLookup == null) {
            niceLinksLookup = lookupRepository.findOneByValue("NICE_CKS").orElse(null);
        }

        if (userLink == null) {
            userLink = lookupRepository.findOneByValue("CUSTOM").orElse(null);
        }
    }

    /**
     * Helper to check Links order is within difficulty range and also that
     * order number is unique per difficulty level.
     *
     * @param link a link to check
     * @param code a code to check links against
     * @throws Exception when link display order is invalid
     */
    private void validateLinkOrder(Link link, Code code) throws Exception {

        if (!linkOrderInRange(link.getDifficultyLevel(), link.getDisplayOrder())) {
            throw new Exception("Invalid order number, must be Green: 1-9, " +
                    "Amber: 11-19, Red: 21-29. Please try again.");
        }

        if (!CollectionUtils.isEmpty(code.getLinks())) {

            // check make sure order is unique per difficulty level
            for (Link codeLink : code.getLinks()) {
                if ((codeLink.getId() != null && !codeLink.getId().equals(link.getId()))
                        && codeLink.getDifficultyLevel().equals(link.getDifficultyLevel())
                        && codeLink.getDisplayOrder().equals(link.getDisplayOrder())) {
                    throw new Exception("Invalid order number, must be unique.");
                }
            }
        }
    }

    /**
     * Check if given link order within difficulty level range
     *
     * @param difficultyLevel a difficulty level
     * @param value           an order number to check
     * @return true if given link order number is in difficulty level range
     */
    private boolean linkOrderInRange(DifficultyLevel difficultyLevel, int value) {
        switch (difficultyLevel) {
            case GREEN:
                if (!CommonUtils.inRange(value, 1, 9)) {
                    log.error("Link Order is incorrect {} for {}", value, difficultyLevel);
                    return false;
                }
                break;

            case AMBER:
                if (!CommonUtils.inRange(value, 11, 19)) {
                    log.error("Link Order is incorrect {} for {}", value, difficultyLevel);
                    return false;
                }
                break;

            case RED:
                if (!CommonUtils.inRange(value, 21, 29)) {
                    log.error("Link Order is incorrect {} for {}", value, difficultyLevel);
                    return false;
                }
                break;

            default:
                return false; // difficulty level not set return false
        }
        // all links validated
        return true;
    }


    /**
     * Based on the difficulty level and existing links in the Code
     * set correct display order.
     *
     * The rule is to use FIRST non-taken number from min to max range
     * per difficulty level eg if we have 11, 14, 15 position taken position
     * order will be set to 12.
     *
     * @param link      a link to set display order for
     * @param codeLinks an list of Link objects from the Code
     */
    private void setLinkDisplayOrder(Link link, Set<Link> codeLinks) {

        // default to min range for the Difficulty level
        int firstAvailable = defaultOrder.get(link.getDifficultyLevel());

        if (!CollectionUtils.isEmpty(codeLinks)) {
            // extract all position numbers for Difficulty level, and sort
            Set<Integer> availableNumbers = codeLinks.stream()
                    .filter(code -> ((code.getId() != null && !code.getId().equals(link.getId()))
                            && code.getDifficultyLevel().equals(link.getDifficultyLevel())))
                    .map(code -> code.getDisplayOrder())
                    .sorted()
                    .collect(Collectors.toCollection(HashSet::new));

            // find first none taken
            while (availableNumbers.contains(firstAvailable)) {
                firstAvailable++;
            }
        }

        link.setDisplayOrder(firstAvailable);
    }


    private long selectIdFrom(String sequence) {
        String sql = "SELECT nextval('" + sequence + "')";
        return ((BigInteger) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
    }
}



