package com.solidstategroup.diagnosisview.service.impl;

import com.solidstategroup.diagnosisview.exceptions.BadRequestException;
import com.solidstategroup.diagnosisview.model.LinkRuleDto;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.codes.LinkRule;
import com.solidstategroup.diagnosisview.model.codes.LinkRuleMapping;
import com.solidstategroup.diagnosisview.model.codes.enums.CriteriaType;
import com.solidstategroup.diagnosisview.model.codes.enums.Institution;
import com.solidstategroup.diagnosisview.repository.LinkRepository;
import com.solidstategroup.diagnosisview.repository.LinkRuleMappingRepository;
import com.solidstategroup.diagnosisview.repository.LinkRuleRepository;
import com.solidstategroup.diagnosisview.service.LinkRuleService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Service
public class LinkRuleServiceImpl implements LinkRuleService {

    private static final String LINK_NOT_FOUND = "Link Rule not found";
    private static final String UNKNOWN_CRITERIA_TYPE = "Unknown Criteria Type";
    private final LinkRuleRepository linkRuleRepository;
    private final LinkRuleMappingRepository linkRuleMappingRepository;
    private final LinkRepository linkRepository;

    public LinkRuleServiceImpl(LinkRuleRepository linkRuleRepository,
                               LinkRuleMappingRepository linkRuleMappingRepository,
                               LinkRepository linkRepository) {

        this.linkRuleRepository = linkRuleRepository;
        this.linkRuleMappingRepository = linkRuleMappingRepository;
        this.linkRepository = linkRepository;
    }

    @Override
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public LinkRule add(LinkRuleDto linkRuleDto) {

        // For now we are only handling institution.
        // This could change in the future.
        if (!linkRuleDto.getCriteriaType().equals(CriteriaType.INSTITUTION)) {
            throw new BadRequestException(UNKNOWN_CRITERIA_TYPE);
        }

        Institution institution = Institution.valueOf(linkRuleDto.getCriteria());

        LinkRule linkRule = linkRuleRepository.save(LinkRule
                .builder()
                .transform(linkRuleDto.getTransformation())
                .link(linkRuleDto.getLink())
                .criteriaType(CriteriaType.INSTITUTION)
                .criteria(institution.toString())
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
                                        .criteria(institution.toString())
                                        .replacementLink(transformLink(
                                                link.getLink(), linkRule.getTransform(), linkRule.getLink()))
                                        .build())
                        .collect(toSet());

        linkRuleMappingRepository.save(linkRuleMappings);

        return linkRule;
    }

    @Override
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public LinkRule updateLinkRule(String id, LinkRuleDto linkRuleDto) {

        LinkRule current = linkRuleRepository.findOne(id);

        if (current == null) {
            throw new BadRequestException(LINK_NOT_FOUND);
        }

        Institution institution = Institution.valueOf(linkRuleDto.getCriteria());

        current.setTransform(linkRuleDto.getTransformation());
        current.setCriteria(linkRuleDto.getCriteria());
        current.setCriteriaType(linkRuleDto.getCriteriaType());
        current.setLink(linkRuleDto.getLink());

        linkRuleMappingRepository.delete(current.getMappings());

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
                                        .criteria(institution.toString())
                                        .replacementLink(transformLink(
                                                link.getLink(), linkRuleDto.getTransformation(), linkRuleDto.getLink()))
                                        .build())
                        .collect(toSet());

        current.setMappings(linkRuleMappings);

        linkRuleRepository.save(current);

        linkRuleMappingRepository.save(linkRuleMappings);

        return current;
    }

    @Override
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public void deleteLinkRule(String uuid) {

        linkRuleRepository.delete(uuid);
    }

    @Override
    public List<LinkRule> getLinkRules() {

        return linkRuleRepository.findAll();
    }

    @Override
    public LinkRule getLinkRule(String uuid) {

        LinkRule linkRule = linkRuleRepository.findOne(uuid);

        if (linkRule == null) {
            throw new BadRequestException(LINK_NOT_FOUND);
        }

        return linkRule;
    }

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

    private String transformLink(String original, String transform, String url) {
        return original.replace(url, transform);
    }
}
