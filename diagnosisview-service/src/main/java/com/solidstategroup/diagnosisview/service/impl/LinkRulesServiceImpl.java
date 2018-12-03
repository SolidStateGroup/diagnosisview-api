package com.solidstategroup.diagnosisview.service.impl;

import com.solidstategroup.diagnosisview.exceptions.BadRequestException;
import com.solidstategroup.diagnosisview.model.LinkRuleDto;
import com.solidstategroup.diagnosisview.model.codes.LinkRule;
import com.solidstategroup.diagnosisview.model.codes.LinkRuleMapping;
import com.solidstategroup.diagnosisview.model.codes.enums.CriteriaType;
import com.solidstategroup.diagnosisview.model.codes.enums.Institution;
import com.solidstategroup.diagnosisview.repository.LinkRepository;
import com.solidstategroup.diagnosisview.repository.LinkRuleMappingRepository;
import com.solidstategroup.diagnosisview.repository.LinkRuleRepository;
import com.solidstategroup.diagnosisview.service.LinkRulesService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Service
public class LinkRulesServiceImpl implements LinkRulesService {

    private final LinkRuleRepository linkRuleRepository;
    private final LinkRuleMappingRepository linkRuleMappingRepository;
    private final LinkRepository linkRepository;

    public LinkRulesServiceImpl(LinkRuleRepository linkRuleRepository,
                                LinkRuleMappingRepository linkRuleMappingRepository,
                                LinkRepository linkRepository) {

        this.linkRuleRepository = linkRuleRepository;
        this.linkRuleMappingRepository = linkRuleMappingRepository;
        this.linkRepository = linkRepository;
    }

    @Override
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public LinkRule addRule(LinkRuleDto linkRuleDto) {

        // For now we are only handling institution. This could change in
        // the future.
        Institution institution;
        if (linkRuleDto.getCriteriaType().equals(CriteriaType.INSTITUTION)) {
            institution = Institution.valueOf(linkRuleDto.getCriteria());
        } else {
            throw new BadRequestException("Unknown Criteria Type");
        }

        LinkRule linkRule = linkRuleRepository.save(LinkRule
                .builder()
                .transform(linkRuleDto.getTransformation())
                .link(linkRuleDto.getLink())
                .criteriaType(CriteriaType.INSTITUTION)
                .criteria(institution.toString())
                .build());

        Set<LinkRuleMapping> linkRuleMappings =
                linkRepository
                        .findLinksByLinkIsLike("%" + linkRule.getLink() + "%")
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
    public LinkRule updateLinkRule(String id, LinkRuleDto linkRuleDto)
            throws Exception {

        LinkRule current = linkRuleRepository.findOne(id);

        if (current == null) {
            throw new Exception();
        }

        current.setTransform(linkRuleDto.getTransformation());
        current.setLink(linkRuleDto.getLink());

        return linkRuleRepository.save(current);
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
            throw new BadRequestException("Link Rule not found");
        }

        return linkRule;
    }

    private String transformLink(String original, String transform, String url) {
        return original.replace(url, transform);
    }
}
