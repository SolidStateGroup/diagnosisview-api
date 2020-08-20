package com.solidstategroup.diagnosisview.service.impl;

import com.solidstategroup.diagnosisview.exceptions.BadRequestException;
import com.solidstategroup.diagnosisview.exceptions.ResourceNotFoundException;
import com.solidstategroup.diagnosisview.model.LinkRuleDto;
import com.solidstategroup.diagnosisview.model.codes.Institution;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.codes.LinkRule;
import com.solidstategroup.diagnosisview.model.codes.LinkRuleMapping;
import com.solidstategroup.diagnosisview.model.codes.enums.CriteriaType;
import com.solidstategroup.diagnosisview.repository.LinkRepository;
import com.solidstategroup.diagnosisview.repository.LinkRuleMappingRepository;
import com.solidstategroup.diagnosisview.repository.LinkRuleRepository;
import com.solidstategroup.diagnosisview.service.LinkRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
public class LinkRuleServiceImpl implements LinkRuleService {

    private static final String LINK_NOT_FOUND = "Link Rule not found";
    private static final String UNKNOWN_CRITERIA_TYPE = "Unknown Criteria Type";
    private final LinkRuleRepository linkRuleRepository;
    private final LinkRuleMappingRepository linkRuleMappingRepository;
    private final LinkRepository linkRepository;
    private final InstitutionService institutionService;

    public LinkRuleServiceImpl(LinkRuleRepository linkRuleRepository,
                               LinkRuleMappingRepository linkRuleMappingRepository,
                               LinkRepository linkRepository,
                               final InstitutionService institutionService) {

        this.linkRuleRepository = linkRuleRepository;
        this.linkRuleMappingRepository = linkRuleMappingRepository;
        this.linkRepository = linkRepository;
        this.institutionService = institutionService;
    }

    @Override
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public LinkRule add(LinkRuleDto linkRuleDto) throws ResourceNotFoundException {

        // For now we are only handling institution.
        // This could change in the future.
        if (!linkRuleDto.getCriteriaType().equals(CriteriaType.INSTITUTION)) {
            throw new BadRequestException(UNKNOWN_CRITERIA_TYPE);
        }

        // Criteria is a code (Lookup value) we store against LinkRule
        Institution institution = institutionService.getInstitution(linkRuleDto.getCriteria());

        LinkRule linkRule = linkRuleRepository.save(LinkRule
                .builder()
                .transform(linkRuleDto.getTransformation())
                .link(linkRuleDto.getLink())
                .criteriaType(CriteriaType.INSTITUTION)
                .criteria(institution.getCode())
                .build());

        Set<LinkRuleMapping> linkRuleMappings =
                linkRepository
                        .findLinksByLinkContaining(linkRule.getLink())
                        .stream()
                        .map(link ->
                                LinkRuleMapping
                                        .builder()
                                        .link(link)
                                        .rule(linkRule)
                                        .criteriaType(CriteriaType.INSTITUTION)
                                        .criteria(institution.getCode())
                                        .replacementLink(transformLink(
                                                link.getLink(), linkRule.getTransform(), linkRule.getLink()))
                                        .build())
                        .collect(toSet());

        linkRuleMappingRepository.saveAll(linkRuleMappings);

        return linkRule;
    }

    @Override
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public LinkRule updateLinkRule(String id, LinkRuleDto linkRuleDto) throws ResourceNotFoundException {

        LinkRule current = linkRuleRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(LINK_NOT_FOUND));

        // Criteria is a code (Lookup value) we store against LinkRule
        Institution institution = institutionService.getInstitution(linkRuleDto.getCriteria());

        current.setTransform(linkRuleDto.getTransformation());
        current.setCriteria(linkRuleDto.getCriteria());
        current.setCriteriaType(linkRuleDto.getCriteriaType());
        current.setLink(linkRuleDto.getLink());

        linkRuleMappingRepository.deleteAll(current.getMappings());

        current.setMappings(new HashSet<>());
        linkRuleRepository.save(current);

        Set<LinkRuleMapping> linkRuleMappings =
                linkRepository
                        .findLinksByLinkContaining(linkRuleDto.getLink())
                        .stream()
                        .map(link ->
                                LinkRuleMapping
                                        .builder()
                                        .link(link)
                                        .rule(current)
                                        .criteriaType(CriteriaType.INSTITUTION)
                                        .criteria(institution.getCode())
                                        .replacementLink(transformLink(
                                                link.getLink(), linkRuleDto.getTransformation(), linkRuleDto.getLink()))
                                        .build())
                        .collect(toSet());


        current.setMappings(linkRuleMappings);

        linkRuleMappingRepository.saveAll(linkRuleMappings);

        return current;
    }

    @Override
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public void deleteLinkRule(String uuid) {

        linkRuleRepository.deleteById(uuid);
    }

    @Override
    public List<LinkRule> getLinkRules() {

        return linkRuleRepository.findAll();
    }

    @Override
    public LinkRule getLinkRule(String uuid) {

        LinkRule linkRule = linkRuleRepository.findById(uuid)
                .orElseThrow(() -> new BadRequestException(LINK_NOT_FOUND));

        return linkRule;
    }

    @Override
    public Set<LinkRuleMapping> matchLinkToRule(Link link) {

        Set<LinkRuleMapping> collect = linkRuleRepository
                .findAll()
                .stream()
                .filter(lr -> link.getLink().startsWith(lr.getLink()))
                .map(lr ->
                        LinkRuleMapping
                                .builder()
                                .link(link)
                                .rule(lr)
                                .criteriaType(CriteriaType.INSTITUTION)
                                .criteria(lr.getCriteria())
                                .replacementLink(transformLink(
                                        link.getLink(), lr.getTransform(), lr.getLink()))
                                .build())
                .collect(toSet());

        return collect;
    }

    /**
     * Re sync all the link rule mappings for existing link rule. Used to populate any missing link rule mapping after
     * sync Links job.
     */
    @Override
    public void syncLinkRules() {

        long start = System.currentTimeMillis();
        log.info("Start LinkRules Processing...");
        // for each LinkRule find links and update link rule mappings
        linkRuleRepository.findAll().stream()
                .forEach(r -> {

                    linkRepository
                            .findLinksByLinkContaining(r.getLink())
                            .forEach(link -> {

                                // check if one exist
                                Optional<LinkRuleMapping> optional = linkRuleMappingRepository.
                                        findByRuleAndLink(r, link);

                                if (optional.isPresent()) {
                                    LinkRuleMapping existing = optional.get();
                                    existing.setCriteria(r.getCriteria());
                                    existing.setCriteriaType(r.getCriteriaType());
                                    existing.setReplacementLink(transformLink(
                                            link.getLink(), r.getTransform(), r.getLink()));

                                    linkRuleMappingRepository.save(existing);

                                } else {

                                    // dont have one create
                                    LinkRuleMapping newMapping = LinkRuleMapping
                                            .builder()
                                            .link(link)
                                            .rule(r)
                                            .criteriaType(r.getCriteriaType())
                                            .criteria(r.getCriteria())
                                            .replacementLink(transformLink(
                                                    link.getLink(), r.getTransform(), r.getLink()))
                                            .build();
                                    linkRuleMappingRepository.save(newMapping);
                                }
                            });
                });

        long stop = System.currentTimeMillis();
        log.info("LinkRules Sync DONE, timing {}.", (stop - start));
    }


    private String transformLink(String original, String transform, String url) {
        return original.replace(url, transform);
    }
}
